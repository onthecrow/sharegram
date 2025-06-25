package com.onthecrow.sharegram.ui.instagram

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun InstagramWebView(
    url: String,
    modifier: Modifier = Modifier,
    onPageLoaded: (String) -> Unit = {},
) {
    AndroidView(
        modifier = modifier,
        factory = { context -> InstagramWebViewWrapper(context) },
        update = { webView ->
            webView.setOnPageLoadedListener(onPageLoaded)
            webView.loadUrl(url)
        }
    )
}