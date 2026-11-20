/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
@file:JvmName("HeaderExtensions")

package it.niedermann.owncloud.notes.shared.util.extensions

fun Map<String, String>.getHeader(name: String): String? =
    entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value
