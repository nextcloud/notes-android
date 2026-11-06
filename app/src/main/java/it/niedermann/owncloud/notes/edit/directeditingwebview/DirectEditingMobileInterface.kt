/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.edit.directeditingwebview

import android.webkit.JavascriptInterface

class DirectEditingMobileInterface {
    @Volatile
    var listener: Listener? = null

    @JavascriptInterface
    fun close() {
        listener?.onClose()
    }

    @JavascriptInterface
    fun share() {
        listener?.onShare()
    }

    @JavascriptInterface
    fun loaded() {
        listener?.onLoaded()
    }

    interface Listener {
        fun onClose()
        fun onShare()
        fun onLoaded()
    }
}
