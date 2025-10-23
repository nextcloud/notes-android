/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.util

import com.nextcloud.android.sso.api.EmptyResponse

/**
 * Extension function to detect a specific ClassCastException caused by an empty API response.
 *
 * This is used to identify cases where the API returns an empty response body,
 * which leads to a `ClassCastException` when attempting to deserialize it into a non-nullable type.
 *
 * In particular, when the API processes an empty Reader (e.g., no changes detected),
 * it may result in a `JsonSyntaxException` or similar parsing error.
 * This function helps to safely ignore such cases by checking if the exception
 * is a `ClassCastException` involving the `EmptyResponse` class.
 *
 * @return `true` if the Throwable is a ClassCastException referencing `EmptyResponse`, otherwise `false`.
 */
fun Throwable.isEmptyResponseCast(): Boolean {
    return this is ClassCastException &&
            (message?.contains(EmptyResponse::class.simpleName ?: "") == true)
}
