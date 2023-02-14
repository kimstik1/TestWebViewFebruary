package com.visoft.testwebview

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import com.visoft.testwebview.databinding.ActivityViewBinding
import com.visoft.testwebview.repository.PreferenceRepository
import com.visoft.testwebview.repository.PreferenceRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewActivity: AppCompatActivity() {

    private var bind: ActivityViewBinding? = null
    private var preferenceRepository: PreferenceRepository? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityViewBinding.inflate(layoutInflater)
        setContentView(bind?.root)

        preferenceRepository = PreferenceRepositoryImpl(this)

        try {
            if(savedInstanceState != null){
                bind?.wv?.restoreState(savedInstanceState)
                webViewOptions()
            }else {
                CoroutineScope(Dispatchers.IO).launch {
                    val link = preferenceRepository?.getFinalLink()
                    webViewOptions(link)
                }
            }
        }catch(ex: Exception){
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
        }

        val onBackPressed = object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if(bind?.wv?.canGoBack() == true) bind?.wv?.goBack()
                else finish()
            }
        }

        onBackPressedDispatcher.addCallback(this, onBackPressed)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun webViewOptions(finalUrl: String? = null) {
        findViewById<WebView>(R.id.wv).apply {
            finalUrl?.let {loadUrl(finalUrl)}
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            CookieManager.getInstance().flush()
            CookieManager.getInstance().acceptCookie()
            isSaveEnabled = true
            isFocusableInTouchMode = true
            isFocusable = true
            isSaveEnabled = true
            webChromeClient = WebChromeClient()
            webViewClient = object: WebViewClient() {

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().acceptCookie()
                    CookieManager.getInstance().flush()
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().acceptCookie()
                    CookieManager.getInstance().flush()
                }
            }
            settings.apply {
                savePassword = true
                isSaveEnabled = true
                saveFormData = true
                mediaPlaybackRequiresUserGesture = true
                domStorageEnabled = true
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true
                defaultTextEncodingName = "utf-8"
                javaScriptCanOpenWindowsAutomatically = true
                loadWithOverviewMode = true
                javaScriptEnabled = true
                javaScriptCanOpenWindowsAutomatically = true
                loadsImagesAutomatically = true
                cacheMode = WebSettings.LOAD_DEFAULT
                databaseEnabled = true
                javaScriptCanOpenWindowsAutomatically = true
                allowContentAccess = true
                useWideViewPort = true
                mixedContentMode = 0
                allowFileAccess = true
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        bind?.wv?.saveState(outState)
        super.onSaveInstanceState(outState)
    }
}