package it.niedermann.owncloud.notes.persistence.sync

import com.nextcloud.android.sso.api.EmptyResponse
import com.owncloud.android.lib.resources.shares.OCShare
import com.owncloud.android.lib.resources.shares.ShareType
import it.niedermann.owncloud.notes.share.model.ShareesData
import it.niedermann.owncloud.notes.shared.model.OcsResponse
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface ShareAPI {
    @GET("sharees")
    fun getSharees2(
        @Query("format") format: String = "json",
        @Query("itemType") itemType: String = "note",
        @Query("search") search: String,
        @Query("page") page: Int,
        @Query("perPage") perPage: Int,
        @Query("lookup") lookup: Boolean = true,
    ): Call<Any>

    @GET("sharees")
    fun getSharees(
        @Query("format") format: String = "json",
        @Query("itemType") itemType: String = "note",
        @Query("search") search: String,
        @Query("page") page: Int,
        @Query("perPage") perPage: Int,
        @Query("lookup") lookup: Boolean = true,
    ): Call<OcsResponse<ShareesData>>

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

    @POST("shares")
    fun addShare(
        remoteFilePath: String,
        shareType: ShareType,
        shareWith: String,
        publicUpload: Boolean,
        password: String,
        permissions: Int,
        getShareDetails: Boolean,
        note: String
    ): Call<OcsResponse<List<OCShare>>>
}
