package com.example.cameraxdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_web_view.*

class WebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        val webView = WebView(applicationContext)
        container.addView(webView)
        val url = intent.getBundleExtra("url").getString("url")
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        webView.loadUrl(url)
        Toast.makeText(this,"$url",Toast.LENGTH_SHORT).show()
    }
}
