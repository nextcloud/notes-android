/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.branding

import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.nextcloud.android.sso.AccountImporter
import com.nextcloud.android.sso.exceptions.SSOException
import com.nextcloud.android.sso.helper.SingleAccountHelper
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.exception.ExceptionDialogFragment
import it.niedermann.owncloud.notes.main.SyncErrorMessage
import it.niedermann.owncloud.notes.persistence.SyncFailureReason

object BrandedSnackbar {
    @JvmStatic
    fun make(
        view: View,
        text: CharSequence,
        @BaseTransientBottomBar.Duration duration: Int
    ): Snackbar {
        @ColorInt val color = BrandingUtil.readBrandMainColor(view.context)
        val snackbar = Snackbar.make(view, text, duration)
        val utils = BrandingUtil.of(color, view.context)

        utils.material.themeSnackbar(snackbar)

        return snackbar
    }

    @JvmStatic
    fun make(
        view: View,
        @StringRes resId: Int,
        @BaseTransientBottomBar.Duration duration: Int
    ): Snackbar {
        return make(view, view.resources.getText(resId), duration)
    }

    @JvmStatic
    fun showSyncError(activity: FragmentActivity, view: View, anchor: View, exceptions: Collection<Throwable>) {
        val reason = SyncErrorMessage(exceptions).reasonOf()
        val snackbar = make(view, reason.message, Snackbar.LENGTH_LONG)
            .setAnchorView(anchor)
        when (reason) {
            SyncFailureReason.TOKEN_MISMATCH ->
                snackbar.setAction(R.string.error_action_grant_access) { requestAccessAgain(activity) }

            SyncFailureReason.ACCOUNT_REMOVED ->
                snackbar.setAction(R.string.choose_account) { chooseAccount(activity) }

            SyncFailureReason.NO_NETWORK, SyncFailureReason.MAINTENANCE -> Unit

            else -> snackbar.setAction(R.string.simple_more) { showDetails(activity, exceptions) }
        }

        snackbar.show()
    }

    @JvmStatic
    fun showSyncError(activity: FragmentActivity, view: View, anchor: View, exception: Throwable) =
        showSyncError(activity, view, anchor, listOf(exception))

    private fun requestAccessAgain(activity: FragmentActivity) {
        try {
            val ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(activity)
            AccountImporter.authenticateSingleSignAccount(activity, ssoAccount)
        } catch (e: SSOException) {
            showDetails(activity, listOf(e))
        }
    }

    private fun chooseAccount(activity: FragmentActivity) {
        try {
            AccountImporter.pickNewAccount(activity)
        } catch (e: SSOException) {
            showDetails(activity, listOf(e))
        }
    }

    private fun showDetails(activity: FragmentActivity, exceptions: Collection<Throwable>) {
        ExceptionDialogFragment.newInstance(ArrayList(exceptions))
            .show(activity.supportFragmentManager, ExceptionDialogFragment::class.java.simpleName)
    }
}
