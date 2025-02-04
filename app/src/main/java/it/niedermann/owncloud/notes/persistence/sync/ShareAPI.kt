package it.niedermann.owncloud.notes.persistence.sync

import com.google.gson.internal.LinkedTreeMap
import com.nextcloud.android.sso.api.EmptyResponse
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.resources.shares.OCShare
import it.niedermann.owncloud.notes.share.model.CreateShareRequest
import it.niedermann.owncloud.notes.share.model.CreateShareResponse
import it.niedermann.owncloud.notes.share.model.UpdateShareInformationRequest
import it.niedermann.owncloud.notes.share.model.UpdateSharePermissionRequest
import it.niedermann.owncloud.notes.share.model.UpdateShareRequest
import it.niedermann.owncloud.notes.shared.model.OcsResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ShareAPI {
    @GET("sharees")
    fun getSharees(
        @Query("format") format: String = "json",
        @Query("itemType") itemType: String = "file",
        @Query("search") search: String,
        @Query("page") page: String,
        @Query("perPage") perPage: String,
        @Query("lookup") lookup: String = "false",
    ): LinkedTreeMap<String, Any?>?

    @GET("shares/{remoteId}?format=json")
    fun getShares(
        @Path("remoteId") remoteId: Long,
        @Query("include_tags") includeTags: Boolean = true,
    ): Call<OcsResponse<List<CreateShareResponse>>>

    @DELETE("shares/{shareId}?format=json")
    fun removeShare(@Path("shareId") shareId: Long): Call<Any>

    @PATCH("shares")
    fun updateShare(@Body request: UpdateShareRequest): Call<OcsResponse<CreateShareResponse>>

    @POST("shares?format=json")
    fun addShare(@Body request: CreateShareRequest): Call<OcsResponse<CreateShareResponse>>

    @PATCH("shares/{shareId}")
    fun updateShareInfo(
        @Path("shareId") shareId: Long,
        @Body request: UpdateShareInformationRequest
    ): Call<OcsResponse<CreateShareResponse>>

    @PATCH("shares/{shareId}")
    fun updateSharePermission(
        @Path("shareId") shareId: Long,
        @Body request: UpdateSharePermissionRequest
    ): Call<OcsResponse<CreateShareResponse>>
}
