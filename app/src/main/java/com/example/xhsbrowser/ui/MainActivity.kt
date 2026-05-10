package com.example.xhsbrowser.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.xhsbrowser.R
import com.example.xhsbrowser.accessibility.XhsAccessibilityService
import com.example.xhsbrowser.databinding.ActivityMainBinding
import com.example.xhsbrowser.ui.adapter.MainPagerAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val count = intent?.getIntExtra("count", 0) ?: 0
            runOnUiThread {
                binding.statusBadge.text = if (count > 0) "$count" else ""
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupBottomNav()
        registerStatusReceiver()

        if (!isAccessibilityServiceEnabled()) {
            showAccessibilityGuide()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(statusReceiver) } catch (_: Exception) { }
    }

    private fun setupViewPager() {
        val adapter = MainPagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.offscreenPageLimit = 3
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> binding.viewPager.currentItem = 0
                R.id.nav_history -> binding.viewPager.currentItem = 1
                R.id.nav_stats -> binding.viewPager.currentItem = 2
                R.id.nav_settings -> binding.viewPager.currentItem = 3
            }
            true
        }
    }

    private fun registerStatusReceiver() {
        val filter = IntentFilter(XhsAccessibilityService.STATUS_ACTION)
        registerReceiver(statusReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains(packageName)
    }

    private fun showAccessibilityGuide() {
        MaterialAlertDialogBuilder(this)
            .setTitle("开启无障碍服务")
            .setMessage("为了自动采集小红书浏览记录，需要开启无障碍服务。\n\n请前往 设置 > 无障碍 > XHS浏览记录 开启服务。\n\n所有数据纯本地存储，不会上传网络。")
            .setPositiveButton("去设置") { _, _ ->
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            .setNegativeButton("稍后", null)
            .show()
    }

    fun shareFile(file: java.io.File) {
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "分享Excel报表"))
    }
}
