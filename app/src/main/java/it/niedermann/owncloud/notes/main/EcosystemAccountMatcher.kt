/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main

import com.nextcloud.android.common.core.utils.AccountNameUtils
import it.niedermann.owncloud.notes.persistence.entity.Account

object EcosystemAccountMatcher {

    @JvmStatic
    fun find(accounts: List<Account>, receivedAccountName: String): Account? =
        accounts.firstOrNull { it.accountName == receivedAccountName }
            ?: accounts.firstOrNull { it.isSameAccount(receivedAccountName) }

    private fun Account.isSameAccount(receivedAccountName: String): Boolean {
        val receivedUser = AccountNameUtils.getUserPart(receivedAccountName)
        val isSameUser = receivedUser.equals(AccountNameUtils.getUserPart(accountName), ignoreCase = true) ||
            receivedUser.equals(userName, ignoreCase = true)
        return isSameUser && AccountNameUtils.isSameServer(url, AccountNameUtils.getServerPart(receivedAccountName))
    }
}
