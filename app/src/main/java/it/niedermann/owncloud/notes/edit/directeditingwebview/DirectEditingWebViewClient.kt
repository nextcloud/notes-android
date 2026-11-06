/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.edit.directeditingwebview

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import it.niedermann.owncloud.notes.BuildConfig

class DirectEditingWebViewClient(
    private val serverUrl: String?,
    private val onMainFrameError: () -> Unit,
) : WebViewClient() {

    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame == true) {
            onMainFrameError()
        }
    }

    @SuppressLint("WebViewClientOnReceivedSslError")
    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        if (BuildConfig.DEBUG) {
            handler?.proceed()
        } else {
            super.onReceivedSslError(view, handler, error)
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url ?: return false
        if (staysOnServer(url)) return false
        view?.let { openExternally(it, url) }
        return true
    }

    private fun staysOnServer(url: Uri): Boolean {
        val scheme = url.scheme?.lowercase()
        return (scheme == "http" || scheme == "https") &&
            serverUrl != null && url.toString().startsWith(serverUrl)
    }

    private fun openExternally(view: WebView, url: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            view.context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "No app available to open $url", e)
        }
    }

    companion object {
        private const val TAG = "DirectEditingWebViewClient"
    }
}
