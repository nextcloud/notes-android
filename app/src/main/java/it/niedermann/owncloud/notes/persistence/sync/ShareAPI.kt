package it.niedermann.owncloud.notes.persistence.sync

import com.nextcloud.android.sso.api.EmptyResponse
import com.owncloud.android.lib.resources.shares.OCShare
import com.owncloud.android.lib.resources.shares.ShareType
import it.niedermann.owncloud.notes.shared.model.OcsResponse
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface ShareAPI {
    @GET("sharees")
    fun getSharees(
        @Query("format") format: String = "json",
        @Query("itemType") itemType: String = "file",
        @Query("search") search: String,
        @Query("page") page: Int,
        @Query("perPage") perPage: Int,
        @Query("lookup") lookup: Boolean = true,
    ): Call<Any>

    @GET("shares")
    fun getShares(remoteId: Long): Call<OcsResponse<List<OCShare>>>

    @GET("shares")
    fun getSharesForFile(
        remoteFilePath: String,
        reshares: Boolean,
        subfiles: Boolean
    ): Call<OcsResponse<MutableList<OCShare>>>

    @DELETE("shares")
    fun deleteShare(remoteShareId: Long): Call<EmptyResponse>

    @PATCH("shares")
    fun updateShare(remoteShareId: Long): Call<OcsResponse<List<OCShare>>>

    @FormUrlEncoded
    @POST("shares")
    fun addShare(
        @Field("remoteFilePath") remoteFilePath: String,
        @Field("shareType") shareType: ShareType,
        @Field("shareWith") shareWith: String,
        @Field("publicUpload") publicUpload: Boolean,
        @Field("password") password: String,
        @Field("permissions") permissions: Int,
        @Field("getShareDetails") getShareDetails: Boolean,
        @Field("note") note: String
    ): Call<OcsResponse<List<OCShare>>>
}
