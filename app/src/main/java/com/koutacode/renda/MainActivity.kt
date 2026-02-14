package com.koutacode.renda

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
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
    private var dotAlphaToggle: Boolean = false

    // Target location within the app's tap area (local coordinates).
    private var targetX: Float? = null
    private var targetY: Float? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tpsSeek.max = 59 // 1..60 taps/sec
        binding.tpsSeek.progress = tapsPerSecond - 1

        updateCount()
        updateTpsUi()

        binding.resetButton.setOnClickListener { resetCount() }

        binding.tapArea.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                setTarget(event.x, event.y)
                doTap()
            }
            true
        }

        // Initialize target to the center so auto-tap works immediately.
        binding.tapArea.post {
            if (targetX == null || targetY == null) {
                setTarget(binding.tapArea.width / 2f, binding.tapArea.height / 2f, fromUser = false)
            }
        }

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
        flashTargetDot()
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

    private fun setTarget(x: Float, y: Float, fromUser: Boolean = true) {
        val area = binding.tapArea
        if (area.width <= 0 || area.height <= 0) {
            area.post { setTarget(x, y, fromUser) }
            return
        }

        val minX = area.paddingLeft.toFloat()
        val maxX = (area.width - area.paddingRight).toFloat()
        val minY = area.paddingTop.toFloat()
        val maxY = (area.height - area.paddingBottom).toFloat()

        targetX = x.coerceIn(minX, maxX)
        targetY = y.coerceIn(minY, maxY)

        binding.tapAreaHint.visibility = View.GONE
        binding.targetDot.visibility = View.VISIBLE

        positionTargetDot()
    }

    private fun positionTargetDot() {
        val area = binding.tapArea
        val dot = binding.targetDot

        val tx = targetX ?: return
        val ty = targetY ?: return

        if (dot.width <= 0 || dot.height <= 0) {
            dot.post { positionTargetDot() }
            return
        }

        val minX = area.paddingLeft.toFloat()
        val maxX = (area.width - area.paddingRight).toFloat()
        val minY = area.paddingTop.toFloat()
        val maxY = (area.height - area.paddingBottom).toFloat()

        val left = (tx - dot.width / 2f).coerceIn(minX, maxX - dot.width.toFloat())
        val top = (ty - dot.height / 2f).coerceIn(minY, maxY - dot.height.toFloat())

        dot.x = left
        dot.y = top
    }

    private fun flashTargetDot() {
        val dot = binding.targetDot
        if (dot.visibility != View.VISIBLE) return

        dotAlphaToggle = !dotAlphaToggle
        dot.alpha = if (dotAlphaToggle) 1.0f else 0.55f
    }
}
