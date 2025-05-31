/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util.extensions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Date.toExpirationDateString(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(this)
}
