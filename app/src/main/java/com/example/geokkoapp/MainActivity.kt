package com.example.geokkoapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Giữ màn hình luôn sáng khi dùng map
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 2. Xin quyền vị trí GPS trực tiếp từ điện thoại
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        // 3. Cấu hình WebView
        webView = findViewById(R.id.myWebView)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true

        // Mở link trực tiếp trong app, không bật trình duyệt ngoài
        webView.webViewClient = WebViewClient()

        // Tự động cấp quyền vị trí cho WebView khi trang web yêu cầu GPS
        webView.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {
                callback?.invoke(origin, true, false)
            }
        }

        // 4. Thay link web bản đồ của bạn vào đây
        webView.loadUrl("https://geokko-proto--pr46-feat-support-xyz-url-2u4jil80.web.app/?gpsdebug&debugconfig")
    }
}