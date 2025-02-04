package it.niedermann.owncloud.notes.persistence.sync

import com.google.gson.internal.LinkedTreeMap
import it.niedermann.owncloud.notes.share.model.CreateShareRequest
import it.niedermann.owncloud.notes.share.model.CreateShareResponse
import it.niedermann.owncloud.notes.share.model.SharePasswordRequest
import it.niedermann.owncloud.notes.share.model.UpdateSharePermissionRequest
import it.niedermann.owncloud.notes.share.model.UpdateShareRequest
import it.niedermann.owncloud.notes.shared.model.OcsResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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

    @POST("shares?format=json")
    fun addShare(@Body request: CreateShareRequest): Call<OcsResponse<CreateShareResponse>>

    @POST("shares/{shareId}/send-email?format=json")
    fun sendEmail(@Path("shareId") shareId: Long, @Body password: SharePasswordRequest?): Call<Any>

    @PUT("shares/{shareId}?format=json")
    fun updateShare(@Path("shareId") shareId: Long, @Body request: UpdateShareRequest): Call<OcsResponse<CreateShareResponse>>

    @PUT("shares/{shareId}")
    fun updateSharePermission(
        @Path("shareId") shareId: Long,
        @Body request: UpdateSharePermissionRequest
    ): Call<OcsResponse<CreateShareResponse>>
}
