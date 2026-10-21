/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main

import android.view.View
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
import com.nextcloud.android.sso.AccountImporter
import com.nextcloud.android.sso.exceptions.SSOException
import com.nextcloud.android.sso.helper.SingleAccountHelper
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.branding.BrandedSnackbar
import it.niedermann.owncloud.notes.exception.ExceptionDialogFragment

object SyncErrorSnackbar {
    @JvmStatic
    fun show(activity: FragmentActivity, parent: View, anchor: View, exceptions: Collection<Throwable>) {
        val snackbar = BrandedSnackbar.make(parent, SyncErrorMessage.of(exceptions), Snackbar.LENGTH_LONG)
            .setAnchorView(anchor)
        if (SyncErrorMessage.isTokenMismatch(exceptions)) {
            snackbar.setAction(R.string.error_action_grant_access) { requestAccessAgain(activity) }
        } else {
            snackbar.setAction(R.string.simple_more) { showDetails(activity, exceptions) }
        }
        snackbar.show()
    }

    @JvmStatic
    fun show(activity: FragmentActivity, parent: View, anchor: View, exception: Throwable) =
        show(activity, parent, anchor, listOf(exception))

    private fun requestAccessAgain(activity: FragmentActivity) {
        try {
            val ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(activity)
            AccountImporter.authenticateSingleSignAccount(activity, ssoAccount)
        } catch (e: SSOException) {
            showDetails(activity, listOf(e))
        }
    }

    private fun showDetails(activity: FragmentActivity, exceptions: Collection<Throwable>) {
        ExceptionDialogFragment.newInstance(ArrayList(exceptions))
            .show(activity.supportFragmentManager, ExceptionDialogFragment::class.java.simpleName)
    }
}
