/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

object KeyboardUtils {
    private const val SHOW_INPUT_DELAY_MILLIS = 100L

    @JvmStatic
    fun showKeyboardForEditText(editText: EditText) {
        editText.requestFocus()
        // needs 100ms delay to account for focus animations
        editText.postDelayed({
            val context = editText.context
            if (context != null) {
                val inputMethodManager =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }
        }, SHOW_INPUT_DELAY_MILLIS)
    }
}
