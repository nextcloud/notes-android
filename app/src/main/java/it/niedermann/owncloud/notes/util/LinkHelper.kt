/*
 * Nextcloud Android Common Library
 *
 * SPDX-FileCopyrightText: 2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package it.niedermann.owncloud.notes.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

/**
 * Helper class for opening Nextcloud apps if present
 * or falling back to opening the app store
 * in case the app is not yet installed on the device.
 */
object LinkHelper {
    private const val TAG = "LinkHelper"

    /**
     * Open app store page of specified app or search for specified string. Will attempt to open browser when no app
     * store is available.
     *
     * @param string packageName or url-encoded search string
     * @param search false -> show app corresponding to packageName; true -> open search for string
     */
    fun openAppStore(
        string: String,
        search: Boolean = false,
        context: Context,
    ) {
        var suffix = (if (search) "search?q=" else "details?id=") + string
        val intent = Intent(Intent.ACTION_VIEW, "market://$suffix".toUri())
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            // all is lost: open google play store web page for app
            if (!search) {
                suffix = "apps/$suffix"
            }
            intent.setData("https://play.google.com/store/$suffix".toUri())
            context.startActivity(intent)
        }
    }
}
