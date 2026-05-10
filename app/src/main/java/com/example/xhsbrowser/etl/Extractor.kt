package com.example.xhsbrowser.etl

import android.view.accessibility.AccessibilityNodeInfo

class Extractor {

    fun extract(rootNode: AccessibilityNodeInfo): List<RawRecord> {
        val records = mutableListOf<RawRecord>()

        findNoteCards(rootNode).forEach { card ->
            val title = extractTitle(card) ?: return@forEach
            val author = extractAuthor(card) ?: "未知作者"
            val snippet = extractContentSnippet(card) ?: ""
            val time = extractTime(card)

            if (title.isNotBlank()) {
                records.add(RawRecord(title, author, snippet, time))
            }
        }

        return records
    }

    private fun findNoteCards(node: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val cards = mutableListOf<AccessibilityNodeInfo>()

        findCardsByPattern(node, cards)

        if (cards.isEmpty()) {
            findCardsByClassName(node, "android.widget.FrameLayout", cards)
        }

        return cards.take(50)
    }

    private fun findCardsByPattern(
        node: AccessibilityNodeInfo,
        results: MutableList<AccessibilityNodeInfo>
    ) {
        val text = node.text?.toString() ?: ""
        val className = node.className?.toString() ?: ""
        val childCount = node.childCount

        if (childCount in 3..15 &&
            (className.endsWith("FrameLayout") || className.endsWith("LinearLayout") ||
             className.endsWith("RelativeLayout") || className.endsWith("ConstraintLayout")) &&
            hasTextChildren(node)
        ) {
            val titleNode = findTitleInSubtree(node)
            val authorNode = findAuthorInSubtree(node)
            if (titleNode != null && authorNode != null) {
                results.add(node)
                return
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findCardsByPattern(child, results)
        }
    }

    private fun findCardsByClassName(
        node: AccessibilityNodeInfo,
        targetClass: String,
        results: MutableList<AccessibilityNodeInfo>
    ) {
        val className = node.className?.toString() ?: ""

        if (className == targetClass && node.childCount in 3..15 && hasTextChildren(node)) {
            results.add(node)
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findCardsByClassName(child, targetClass, results)
        }
    }

    private fun hasTextChildren(node: AccessibilityNodeInfo): Boolean {
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (!child.text.isNullOrBlank()) return true
        }
        return false
    }

    private fun extractTitle(card: AccessibilityNodeInfo): String? {
        return findTitleInSubtree(card)?.text?.toString()?.trim()
    }

    private fun findTitleInSubtree(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var bestNode: AccessibilityNodeInfo? = null
        var bestLength = 0

        fun search(n: AccessibilityNodeInfo, depth: Int) {
            val className = n.className?.toString() ?: ""
            if (className.endsWith("TextView") && depth <= 5) {
                val text = n.text?.toString() ?: ""
                if (text.length in 4..80 && text.length > bestLength) {
                    bestNode = n
                    bestLength = text.length
                }
            }
            for (i in 0 until n.childCount) {
                n.getChild(i)?.let { search(it, depth + 1) }
            }
        }

        search(node, 0)
        return bestNode
    }

    private fun extractAuthor(card: AccessibilityNodeInfo): String? {
        return findAuthorInSubtree(card)?.text?.toString()?.trim()
    }

    private fun findAuthorInSubtree(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // Author is usually a short (2-15 chars) text, often preceded by "·" or an avatar
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val text = child.text?.toString()?.trim() ?: ""
            val cls = child.className?.toString() ?: ""
            if (cls.endsWith("TextView") && text.length in 2..20 && text != "·") {
                return child
            }
        }
        return null
    }

    private fun extractContentSnippet(card: AccessibilityNodeInfo): String? {
        // Content is medium-length text (20-200 chars), typically after the title
        for (i in 0 until card.childCount) {
            val child = card.getChild(i) ?: continue
            val text = child.text?.toString()?.trim() ?: ""
            val cls = child.className?.toString() ?: ""
            if (cls.endsWith("TextView") && text.length in 20..300) {
                return text
            }
        }
        return null
    }

    private fun extractTime(card: AccessibilityNodeInfo): Long {
        for (i in 0 until card.childCount) {
            val child = card.getChild(i) ?: continue
            val text = child.text?.toString()?.trim() ?: ""
            if (text.matches(Regex(".*(分钟前|小时前|昨天|\\d{1,2}-\\d{1,2}|\\d{4}-\\d{2}-\\d{2}).*"))) {
                return parseRelativeTime(text)
            }
        }
        return System.currentTimeMillis()
    }

    private fun parseRelativeTime(text: String): Long {
        val now = System.currentTimeMillis()
        return when {
            text.contains("分钟前") -> {
                val min = text.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
                now - min * 60_000L
            }
            text.contains("小时前") -> {
                val hour = text.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
                now - hour * 3600_000L
            }
            text.contains("昨天") -> now - 86400_000L
            else -> now
        }
    }
}
