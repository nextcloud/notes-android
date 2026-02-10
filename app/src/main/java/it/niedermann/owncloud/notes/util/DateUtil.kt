/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.util

import it.niedermann.owncloud.notes.shared.util.extensions.toExpirationDateString
import java.util.Date

object DateUtil {
    fun getExpirationDate(chosenExpDateInMills: Long): String? {
        if (chosenExpDateInMills == -1L) {
            return null
        }

        return Date(chosenExpDateInMills).toExpirationDateString()
    }
}
