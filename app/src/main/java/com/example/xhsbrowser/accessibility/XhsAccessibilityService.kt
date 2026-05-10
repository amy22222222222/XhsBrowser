package com.example.xhsbrowser.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.xhsbrowser.etl.EtlPipeline
import com.example.xhsbrowser.etl.Extractor
import com.example.xhsbrowser.etl.RawRecord
import kotlinx.coroutines.*

class XhsAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isCollecting = false
    private var autoScrollEnabled = true
    private var scrollDelayMs = 2000L
    private var lastScrollTime = 0L

    private val extractor = Extractor()
    private var pendingRecords = mutableListOf<RawRecord>()
    private var etlPipeline: EtlPipeline? = null
    private var collectedCount = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        instance = this
        etlPipeline = ServiceLocator.getEtlPipeline(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        if (packageName != XHS_PACKAGE) return

        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_VIEW_SCROLLED
        ) return

        if (isCollecting) return

        val rootNode = rootInActiveWindow ?: return
        isCollecting = true

        try {
            if (isOnHistoryPage(rootNode)) {
                val records = extractor.extract(rootNode)
                if (records.isNotEmpty()) {
                    pendingRecords.addAll(records)
                    collectedCount += records.size
                    broadcastStatus(collectedCount)
                }

                if (autoScrollEnabled) {
                    scheduleScroll(rootNode)
                }

                flushIfNeeded()
            }
        } finally {
            rootNode.recycle()
            isCollecting = false
        }
    }

    override fun onInterrupt() {
        isCollecting = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        instance = null
        serviceScope.cancel()
        flushRemaining()
    }

    private fun isOnHistoryPage(node: AccessibilityNodeInfo): Boolean {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(node)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val text = current.text?.toString() ?: ""
            if (text.contains("浏览记录") || text.contains("观看历史") ||
                text.contains("看过") || text.contains("浏览历史")
            ) return true

            for (i in 0 until current.childCount) {
                current.getChild(i)?.let { queue.add(it) }
            }
        }
        return false
    }

    private fun scheduleScroll(rootNode: AccessibilityNodeInfo) {
        val now = System.currentTimeMillis()
        if (now - lastScrollTime < scrollDelayMs) return
        lastScrollTime = now

        mainHandler.postDelayed({
            val node = rootInActiveWindow ?: return@postDelayed
            performScroll(node)
            node.recycle()
        }, scrollDelayMs)
    }

    private fun performScroll(node: AccessibilityNodeInfo) {
        val scrollable = findScrollableNode(node) ?: return
        scrollable.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        scrollable.recycle()
    }

    private fun findScrollableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable) return AccessibilityNodeInfo.obtain(node)

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findScrollableNode(child)
            child.recycle()
            if (result != null) return result
        }
        return null
    }

    private fun flushIfNeeded() {
        if (pendingRecords.size >= 20) {
            flushRecords()
        }
    }

    private fun flushRemaining() {
        if (pendingRecords.isNotEmpty()) {
            flushRecords()
        }
    }

    private fun flushRecords() {
        val records = pendingRecords.toList()
        pendingRecords.clear()

        val pipeline = etlPipeline ?: return
        serviceScope.launch {
            try {
                val result = pipeline.process(records)
                if (result.loaded > 0) {
                    broadcastStatus(collectedCount)
                }
            } catch (_: Exception) { }
        }
    }

    fun setAutoScroll(enabled: Boolean) {
        autoScrollEnabled = enabled
    }

    fun setScrollInterval(ms: Long) {
        scrollDelayMs = ms
    }

    private fun broadcastStatus(count: Int) {
        val intent = Intent(STATUS_ACTION).apply {
            putExtra("count", count)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    companion object {
        const val XHS_PACKAGE = "com.xingin.xhs"
        const val STATUS_ACTION = "com.example.xhsbrowser.COLLECTION_STATUS"

        var isRunning = false
            private set

        private var instance: XhsAccessibilityService? = null
        fun getInstance(): XhsAccessibilityService? = instance
    }
}
