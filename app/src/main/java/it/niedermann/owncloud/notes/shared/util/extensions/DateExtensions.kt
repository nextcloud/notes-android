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

fun Date.toExpirationDateString(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(this)

/**
 * Parses an expiration date string in "yyyy-MM-dd 00:00:00" format into a millisecond timestamp
 * representing the start of that day.
 *
 * @return The time in milliseconds since the epoch, or 0 if parsing fails.
 */
fun String.toExpirationDateLong(): Long =
    SimpleDateFormat("yyyy-MM-dd 00:00:00", Locale.getDefault()).parse(this)?.time ?: 0L
