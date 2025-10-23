/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.util

import com.nextcloud.android.sso.api.EmptyResponse

fun Throwable.isEmptyResponseCast(): Boolean {
    return this is ClassCastException &&
            (message?.contains(EmptyResponse::class.simpleName ?: "") == true)
}
