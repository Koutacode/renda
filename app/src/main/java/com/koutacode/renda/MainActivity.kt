package com.koutacode.renda

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.koutacode.renda.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var tapsPerSecond: Int = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tpsSeek.max = 49 // 1..50 taps/sec
        binding.tpsSeek.progress = tapsPerSecond - 1
        updateTpsUi()

        binding.tpsSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tapsPerSecond = (progress + 1).coerceIn(1, 50)
                updateTpsUi()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        binding.resetButton.text = getString(R.string.start_service)
        binding.resetButton.setOnClickListener {
            if (checkPermissions()) {
                startAutoClickService()
            }
        }

        binding.autoSwitch.visibility = View.GONE
        binding.autoLabel.visibility = View.GONE
        binding.tapAreaCard.visibility = View.GONE
        binding.countText.visibility = View.GONE
    }

    private fun updateTpsUi() {
        binding.tpsValue.text = getString(R.string.tps_value, tapsPerSecond)
    }

    private fun checkPermissions(): Boolean {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
            Toast.makeText(this, "「他のアプリの上に重ねて表示」を有効にしてください", Toast.LENGTH_LONG).show()
            return false
        }
        if (!isAccessibilityServiceEnabled()) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "「超連打」をユーザー補助で有効にしてください", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val componentName = ComponentName(this, AutoClickService::class.java).flattenToString()
        return enabledServices?.contains(componentName) == true
    }

    private fun startAutoClickService() {
        val intent = Intent(this, AutoClickService::class.java)
        intent.putExtra("tps", tapsPerSecond)
        startService(intent)
        // もしサービスが既に動いている場合はIntentで設定更新も可能
        Toast.makeText(this, "サービスを開始しました。画面上のパネルを操作してください。", Toast.LENGTH_SHORT).show()
    }
}
