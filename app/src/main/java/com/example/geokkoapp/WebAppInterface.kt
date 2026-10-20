package com.example.geokkoapp
import android.content.Context
import android.webkit.JavascriptInterface

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
}