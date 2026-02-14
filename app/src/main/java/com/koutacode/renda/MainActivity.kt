package com.koutacode.renda

import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.koutacode.renda.databinding.ActivityMainBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var taps: Long = 0
    private var tapsPerSecond: Int = 10
    private var autoJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tpsSeek.max = 59 // 1..60 taps/sec
        binding.tpsSeek.progress = tapsPerSecond - 1

        updateCount()
        updateTpsUi()

        binding.tapButton.setOnClickListener { doTap() }
        binding.resetButton.setOnClickListener { resetCount() }

        binding.tpsSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tapsPerSecond = (progress + 1).coerceIn(1, 60)
                updateTpsUi()
                if (binding.autoSwitch.isChecked) {
                    startAutoTap()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        binding.autoSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startAutoTap()
            } else {
                stopAutoTap()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Don't keep auto running when the app is not visible.
        binding.autoSwitch.isChecked = false
        stopAutoTap()
    }

    private fun doTap() {
        taps += 1
        updateCount()
    }

    private fun resetCount() {
        taps = 0
        updateCount()
    }

    private fun updateCount() {
        binding.countText.text = taps.toString()
    }

    private fun updateTpsUi() {
        binding.tpsValue.text = getString(R.string.tps_value, tapsPerSecond)
    }

    private fun startAutoTap() {
        stopAutoTap()

        val intervalMs = (1000.0 / tapsPerSecond.toDouble()).roundToLong().coerceAtLeast(1)
        autoJob = lifecycleScope.launch {
            while (isActive) {
                doTap()
                delay(intervalMs)
            }
        }
    }

    private fun stopAutoTap() {
        autoJob?.cancel()
        autoJob = null
    }
}

