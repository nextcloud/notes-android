/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.sync

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.HTTP
import retrofit2.http.Path

interface VersionsAPI {
    @HTTP(method = "PROPFIND", path = "versions/{userId}/versions/{fileId}")
    fun fetchVersions(@Path("userId") userId: String, @Path("fileId") fileId: Long): Observable<ResponseBody>
}
