/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util.clipboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/**
 * Activity copying the text of the received Intent into the system clipboard.
 */
class CopyToClipboardActivity : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ClipboardUtil.copyToClipboard(this, intent.getCharSequenceExtra(Intent.EXTRA_TEXT).toString())
        finish()
    }
}
