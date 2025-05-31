/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util.extensions

import it.niedermann.owncloud.notes.shared.model.OcsResponse
import org.json.JSONObject
import retrofit2.Response

fun <T> Response<OcsResponse<T>>.getErrorMessage(): String? {
    if (isSuccessful) {
        return null
    }

    val errorBody = errorBody()?.string() ?: return null
    val jsonObject = JSONObject(errorBody)
    return jsonObject.getJSONObject("ocs")
        .getJSONObject("meta")
        .getString("message")
}
