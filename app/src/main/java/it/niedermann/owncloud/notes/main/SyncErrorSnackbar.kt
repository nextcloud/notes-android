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
import it.niedermann.owncloud.notes.persistence.SyncFailureReason

class SyncErrorSnackbar(
    private val activity: FragmentActivity,
    private val parent: View,
    private val anchor: View
) {
    fun show(exceptions: Collection<Throwable>) {
        val reason = SyncErrorMessage(exceptions).reasonOf()
        val snackbar = BrandedSnackbar.make(parent, reason.message, Snackbar.LENGTH_LONG)
            .setAnchorView(anchor)
        when (reason) {
            SyncFailureReason.TOKEN_MISMATCH ->
                snackbar.setAction(R.string.error_action_grant_access) { requestAccessAgain() }

            SyncFailureReason.ACCOUNT_REMOVED ->
                snackbar.setAction(R.string.choose_account) { chooseAccount() }

            else -> snackbar.setAction(R.string.simple_more) { showDetails(exceptions) }
        }

        snackbar.show()
    }

    fun show(exception: Throwable) = show(listOf(exception))

    private fun requestAccessAgain() {
        try {
            val ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(activity)
            AccountImporter.authenticateSingleSignAccount(activity, ssoAccount)
        } catch (e: SSOException) {
            showDetails(listOf(e))
        }
    }

    private fun chooseAccount() {
        try {
            AccountImporter.pickNewAccount(activity)
        } catch (e: SSOException) {
            showDetails(listOf(e))
        }
    }

    private fun showDetails(exceptions: Collection<Throwable>) {
        ExceptionDialogFragment.newInstance(ArrayList(exceptions))
            .show(activity.supportFragmentManager, ExceptionDialogFragment::class.java.simpleName)
    }
}
