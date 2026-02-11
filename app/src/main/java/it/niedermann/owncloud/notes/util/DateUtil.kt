/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.util

import it.niedermann.owncloud.notes.shared.util.extensions.toExpirationDateString
import java.util.Date

/**
 * Utility object for handling date formatting.
 *
 */
object DateUtil {

    /**
     * Converts a timestamp in milliseconds to a formatted expiration date string.
     *
     * @param chosenExpDateInMills The expiration date represented as milliseconds
     * since the Unix epoch (January 1, 1970). If the value is `-1L`, it is treated
     * as "no expiration date" and `null` is returned.
     *
     * @return A formatted expiration date string generated via
     * [toExpirationDateString], or `null` if no expiration date is set.
     */
    fun getExpirationDate(chosenExpDateInMills: Long): String? {
        if (chosenExpDateInMills == -1L) {
            return null
        }

        return Date(chosenExpDateInMills).toExpirationDateString()
    }
}
