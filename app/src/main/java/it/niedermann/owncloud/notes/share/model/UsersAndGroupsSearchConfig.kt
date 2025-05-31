/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.share.model

object UsersAndGroupsSearchConfig {
    var searchOnlyUsers: Boolean = false

    fun reset() {
        searchOnlyUsers = false
    }
}
