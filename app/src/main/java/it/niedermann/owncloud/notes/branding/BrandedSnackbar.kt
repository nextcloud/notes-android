/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.branding

import android.content.Context
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
import it.niedermann.owncloud.notes.persistence.FilesAppAccount
import it.niedermann.owncloud.notes.persistence.SyncFailureReason
import it.niedermann.owncloud.notes.persistence.entity.Account
import java.lang.ref.WeakReference

object BrandedSnackbar {
    private const val SYNC_ERROR_MAX_LINES = 4

    private var syncErrorSnackbar: WeakReference<Snackbar>? = null
    private var currentAccount: Account? = null

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
        if (reason == SyncFailureReason.ACCOUNT_REMOVED && isCurrentAccountAvailable(activity)) return

        val snackbar = make(view, reason.text(view.context), reason.snackbarDuration())
            .setTextMaxLines(SYNC_ERROR_MAX_LINES)
            .setAnchorView(anchor)
        when (reason) {
            SyncFailureReason.TOKEN_MISMATCH ->
                snackbar.setAction(R.string.error_action_grant_access) { requestAccessAgain(activity) }

            SyncFailureReason.ACCOUNT_REMOVED ->
                snackbar.setAction(R.string.choose_account) { chooseAccount(activity) }

            SyncFailureReason.NO_NETWORK, SyncFailureReason.MAINTENANCE -> Unit

            else -> snackbar.setAction(R.string.simple_more) { showDetails(activity, exceptions) }
        }

        syncErrorSnackbar = WeakReference(snackbar)
        snackbar.show()
    }

    @JvmStatic
    fun onCurrentAccountChanged(account: Account) {
        currentAccount = account
        syncErrorSnackbar?.get()?.dismiss()
        syncErrorSnackbar = null
    }

    @JvmStatic
    fun showSyncError(activity: FragmentActivity, view: View, anchor: View, exception: Throwable) =
        showSyncError(activity, view, anchor, listOf(exception))

    private fun SyncFailureReason.text(context: Context): CharSequence = when (this) {
        SyncFailureReason.ACCOUNT_REMOVED -> context.getString(message, currentAccount?.userName)
        else -> context.getText(message)
    }

    private fun SyncFailureReason.snackbarDuration(): Int = when (this) {
        SyncFailureReason.TOKEN_MISMATCH, SyncFailureReason.ACCOUNT_REMOVED -> Snackbar.LENGTH_INDEFINITE
        else -> Snackbar.LENGTH_LONG
    }

    private fun isCurrentAccountAvailable(activity: FragmentActivity): Boolean =
        currentAccount?.let { FilesAppAccount.exists(activity, it.accountName) } == true

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
