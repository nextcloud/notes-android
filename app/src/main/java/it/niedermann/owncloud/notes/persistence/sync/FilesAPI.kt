/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.sync

import it.niedermann.owncloud.notes.shared.model.OcsResponse
import it.niedermann.owncloud.notes.shared.model.OcsUrl
import it.niedermann.owncloud.notes.shared.model.directediting.DirectEditingInfo
import it.niedermann.owncloud.notes.shared.model.directediting.DirectEditingRequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FilesAPI {
    @GET("directEditing?format=json")
    fun getDirectEditingInfo(): Call<OcsResponse<DirectEditingInfo>>

    @POST("directEditing/open?format=json")
    fun getDirectEditingUrl(@Body body: DirectEditingRequestBody): Call<OcsResponse<OcsUrl>>
}
