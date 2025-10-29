/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.util

import androidx.core.app.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.nextcloud.android.sso.model.SingleSignOnAccount
import com.owncloud.android.lib.common.utils.Log_OC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Retrieves the currently active Single Sign-On (SSO) account associated with this [ComponentActivity].
 *
 * This function runs asynchronously using a coroutine:
 * - The SSO account lookup is performed on the **IO dispatcher** (background thread).
 * - Once the result is available, the [onCompleted] callback is invoked on the **main thread**.
 *
 * If fetching the account fails for any reason (e.g., no account found, SSO error, etc.),
 * the callback will receive `null` and an error will be logged.
 *
 * @param onCompleted A callback that receives the retrieved [SingleSignOnAccount],
 * or `null` if no valid account was found.
 */
fun ComponentActivity.ssoAccount(onCompleted: (SingleSignOnAccount?) -> Unit) {
    lifecycleScope.launch(Dispatchers.IO) {
        val result = try {
            val account = SingleAccountHelper.getCurrentSingleSignOnAccount(this@ssoAccount)
            account
        } catch (t: Throwable) {
            Log_OC.e("ComponentActivityExtension", "cant get sso account: $t")
            null
        }
        withContext(Dispatchers.Main) {
            onCompleted(result)
        }
    }
}
