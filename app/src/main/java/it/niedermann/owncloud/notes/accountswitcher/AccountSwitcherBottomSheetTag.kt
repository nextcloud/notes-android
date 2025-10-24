/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.accountswitcher

import com.owncloud.android.lib.resources.users.Status
import it.niedermann.owncloud.notes.accountswitcher.repository.UserStatusRepository
import it.niedermann.owncloud.notes.branding.BrandedBottomSheetDialogFragment

enum class AccountSwitcherBottomSheetTag(tag: String) {
    ONLINE_STATUS("fragment_set_status"),
    MESSAGE_STATUS("fragment_set_status_message");

    fun fragment(
        repository: UserStatusRepository,
        currentStatus: Status
    ): BrandedBottomSheetDialogFragment {
        return when (this) {
            ONLINE_STATUS -> {
                SetOnlineStatusBottomSheet(repository, currentStatus)
            }

            MESSAGE_STATUS -> {
                SetStatusMessageBottomSheet(repository, currentStatus)
            }
        }
    }
}
