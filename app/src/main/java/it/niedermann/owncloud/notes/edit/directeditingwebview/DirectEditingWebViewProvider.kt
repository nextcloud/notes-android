/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.edit.directeditingwebview

import android.annotation.SuppressLint
import android.content.Context
import android.content.MutableContextWrapper
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import it.niedermann.owncloud.notes.BuildConfig
import it.niedermann.owncloud.notes.R

@SuppressLint("StaticFieldLeak")
object DirectEditingWebViewProvider {
    private const val JS_INTERFACE_NAME = "DirectEditingMobileInterface"

    private val mobileInterface = DirectEditingMobileInterface()
    private var webView: WebView? = null

    fun attach(
        container: ViewGroup,
        client: WebViewClient,
        listener: DirectEditingMobileInterface.Listener,
    ): WebView {
        val webView = webView ?: create(container.context.applicationContext).also { webView = it }
        (webView.parent as? ViewGroup)?.removeView(webView)
        (webView.context as MutableContextWrapper).baseContext = container.context
        webView.webViewClient = client
        mobileInterface.listener = listener
        container.addView(webView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return webView
    }

    @SuppressLint("ClickableViewAccessibility")
    fun detach(container: ViewGroup) {
        val webView = webView ?: return
        if (webView.parent !== container) return
        container.removeView(webView)
        webView.setOnTouchListener(null)
        webView.webViewClient = WebViewClient()
        mobileInterface.listener = null
        (webView.context as MutableContextWrapper).baseContext = container.context.applicationContext
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun create(appContext: Context): WebView = WebView(MutableContextWrapper(appContext)).apply {
        settings.run {
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            userAgentString = appContext.getString(
                R.string.user_agent,
                appContext.getString(R.string.app_name),
                BuildConfig.VERSION_NAME,
            )
            savePassword = false
            saveFormData = false
            allowFileAccess = false
            javaScriptEnabled = true
            domStorageEnabled = true
            if (BuildConfig.DEBUG) {
                cacheMode = WebSettings.LOAD_NO_CACHE
            }
        }
        addJavascriptInterface(mobileInterface, JS_INTERFACE_NAME)
    }
}
