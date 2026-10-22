package com.example.geokkoapp
import android.content.Context
import android.webkit.JavascriptInterface
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.media.AudioAttributes

class WebAppInterface(private val mContext: Context) {

    @JavascriptInterface
    fun requestLocation() {
        if (mContext is MainActivity) {
            mContext.getNativeLocation()
        }
    }

    @JavascriptInterface
    fun requestOrientation() {
        if (mContext is MainActivity) {
            mContext.startNativeSensor()
        }
    }

    @JavascriptInterface
    fun vibrateSuccess() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = mContext.getSystemService(
                    Context.VIBRATOR_MANAGER_SERVICE
                ) as VibratorManager
                manager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                mContext.getSystemService(
                    Context.VIBRATOR_SERVICE
                ) as Vibrator
            }

            if (!vibrator.hasVibrator()) {
                Log.e("HapticDebug", "Device has no vibrator")
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(
                    200,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )

                val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(
                        AudioAttributes.CONTENT_TYPE_SONIFICATION
                    )
                    .build()

                vibrator.vibrate(effect, attributes)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }

            Log.d("HapticDebug", "Notification vibration requested")
        } catch (e: Exception) {
            Log.e("HapticDebug", "Vibration failed", e)
        }
    }
}