/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.edit.directeditingwebview

import android.util.Log
import android.webkit.WebView
import org.json.JSONObject

private const val TAG = "WebViewOpenDirectEditing"
private const val JS_RESULT_OK = "ok"

fun WebView.openDirectEditing(url: String) {
    if (this.url == null) {
        loadUrl(url)
        return
    }
    evaluateJavascript(openDirectEditingScript(url)) { result ->
        if (result?.trim('"') == JS_RESULT_OK) return@evaluateJavascript
        Log.w(TAG, "Reusing the editor failed ($result), reloading the page")
        loadUrl(url)
    }
}

// language=js
private fun openDirectEditingScript(url: String) = """
    (function () {
      if (typeof OCA === "undefined" || !OCA.Text || typeof OCA.Text.openDirectEditing !== "function") {
        return "openDirectEditing not available";
      }
      OCA.Text.openDirectEditing(${JSONObject.quote(url)});
      return "$JS_RESULT_OK";
    })();
""".trimIndent()
