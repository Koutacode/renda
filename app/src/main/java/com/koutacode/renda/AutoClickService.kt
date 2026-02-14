package com.koutacode.renda

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.koutacode.renda.databinding.OverlayControlBinding

class AutoClickService : AccessibilityService() {

    private lateinit var windowManager: WindowManager
    private var controlView: View? = null
    private var targetView: View? = null
    
    private var isRunning = false
    private var tapsPerSecond = 10
    private var lastX = 500f
    private var lastY = 1000f

    private val CHANNEL_ID = "AutoClickServiceChannel"

    private val handler = Handler(Looper.getMainLooper())
    private val clickRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                click(lastX.toInt(), lastY.toInt())
                handler.postDelayed(this, (1000 / tapsPerSecond).toLong())
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(1, createNotification())
        showOverlay()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Auto Click Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("超連打 実行中")
            .setContentText("オートクリッカーがバックグラウンドで動作しています")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            tapsPerSecond = it.getIntExtra("tps", 10)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun showOverlay() {
        // Control Panel
        val controlBinding = OverlayControlBinding.inflate(LayoutInflater.from(this))
        controlView = controlBinding.root

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 300
        }

        controlBinding.playPauseButton.setOnClickListener {
            isRunning = !isRunning
            if (isRunning) {
                controlBinding.playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
                handler.post(clickRunnable)
            } else {
                controlBinding.playPauseButton.setImageResource(android.R.drawable.ic_media_play)
                handler.removeCallbacks(clickRunnable)
            }
        }

        controlBinding.closeButton.setOnClickListener {
            stopSelf()
        }

        // Target Dot (The point where clicks happen)
        targetView = LayoutInflater.from(this).inflate(R.layout.overlay_target, null)
        val targetParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = lastX.toInt() - 50
            y = lastY.toInt() - 50
        }

        targetView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = targetParams.x
                        initialY = targetParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        targetParams.x = initialX + (event.rawX - initialTouchX).toInt()
                        targetParams.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(targetView, targetParams)
                        lastX = targetParams.x + (v.width / 2).toFloat()
                        lastY = targetParams.y + (v.height / 2).toFloat()
                        return true
                    }
                }
                return false
            }
        })

        // Drag functionality for control panel
        controlView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(controlView, params)
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(controlView, params)
        windowManager.addView(targetView, targetParams)
    }

    private fun click(x: Int, y: Int) {
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 10))
            .build()
        dispatchGesture(gesture, null, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        handler.removeCallbacks(clickRunnable)
        controlView?.let { windowManager.removeView(it) }
        targetView?.let { windowManager.removeView(it) }
    }
}
