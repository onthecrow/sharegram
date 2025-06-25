package com.onthecrow.sharegram.ui.instagram

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import java.lang.ref.WeakReference

class InstagramWebViewWrapper : WebView {

    private val cookieManager = CookieManager.getInstance()
    private var onPageLoadedListener: WeakReference<(String) -> Unit>? = null

    fun setOnPageLoadedListener(listener: (String) -> Unit) {
        onPageLoadedListener = WeakReference(listener)
    }

    private fun init() {
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(this, true)
        applySettings()
        setWebViewClient(getClient())
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun applySettings(): WebSettings {
        return getSettings().apply {
            javaScriptEnabled = true // VERY IMPORTANT for Instagram
            domStorageEnabled = true // For local storage, cookies, etc.
            databaseEnabled = true // Also for various web app storage
            allowContentAccess = true // Allow access to content URLs
            allowFileAccess = true // Allow access to file system
            loadWithOverviewMode = true // Make the content fill the screen
            useWideViewPort = true // For responsive design
            javaScriptCanOpenWindowsAutomatically = true // For pop-ups/redirects
            userAgentString = USER_AGENT
            setSupportMultipleWindows(true) // Important for OAuth flows or other pop-ups
        }
    }

    private fun getClient(): WebViewClient {
        return object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest
            ): WebResourceResponse? {
                val url = request.url.toString()
                Log.d("%%%%%", "Loading resource: $url")

                // You can add logic here to filter for specific file types
                if (url.endsWith(".js") || url.endsWith(".css") || url.endsWith(
                        ".png"
                    ) || url.endsWith(".jpg") || url.endsWith(".gif")
                ) {
                    Log.d("%%%%%", "Intercepted file: $url")
                    // You could potentially read the content of the file if it's accessible
                    // or perform other actions based on the file type.
                }

                // Return null to allow the WebView to load the resource normally.
                // If you return a WebResourceResponse, you are effectively providing the content
                // and the WebView will not make the network request for this resource.
                return super.shouldInterceptRequest(view, request)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                view.loadUrl(request.url.toString())
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)

                evaluateJavascript(SCRIPT_GET_FULL_PAGE) { html ->
                    if (html != null) {
                        val unescapedHtml = html.trim('"').replace("\\u003C", "<")
                            .replace("\\n", "\n").replace("\\t", "\t")
                            .replace("\\\"", "\"")

                        Log.d(
                            this.javaClass.simpleName,
                            "HTML content received, preparing to save."
                        )
                        onPageLoadedListener?.get()?.invoke(unescapedHtml)
                    } else {
                        Log.e(this.javaClass.simpleName, "Failed to get HTML content.")
                    }
                }
                cookieManager.flush()
            }
        }
    }

    companion object Companion {
        private const val SCRIPT_GET_FULL_PAGE =
            "(function() { return document.documentElement.outerHTML; })();"
        private const val USER_AGENT = "Android"
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }
}