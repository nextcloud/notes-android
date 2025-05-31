/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T, val message: String? = null) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
}

fun <T> ApiResult<T>.isSuccess(): Boolean = this is ApiResult.Success<T>
fun <T> ApiResult<T>.isError(): Boolean = this is ApiResult.Error
