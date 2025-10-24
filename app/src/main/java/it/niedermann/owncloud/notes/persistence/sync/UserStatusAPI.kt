package it.niedermann.owncloud.notes.persistence.sync

import com.owncloud.android.lib.resources.users.PredefinedStatus
import com.owncloud.android.lib.resources.users.Status
import it.niedermann.owncloud.notes.accountswitcher.model.ExposedPredefinedStatus
import it.niedermann.owncloud.notes.shared.model.OcsResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PUT

interface UserStatusAPI {
    @GET("user_status?format=json")
    fun fetchUserStatus(): Call<OcsResponse<Status>>

    @DELETE("user_status/message?format=json")
    fun clearStatusMessage(): Call<OcsResponse<Void>>

    @GET("predefined_statuses?format=json")
    fun fetchPredefinedStatuses(): Call<OcsResponse<ArrayList<ExposedPredefinedStatus>>>

    @PUT("user_status/message/predefined?format=json")
    @Headers("Content-Type: application/json")
    fun setPredefinedStatusMessage(@Body body: Map<String, String>): Call<OcsResponse<Void>>

    @PUT("user_status/message/custom?format=json")
    @Headers("Content-Type: application/json")
    fun setUserDefinedStatusMessage(@Body body: Map<String, String>): Call<OcsResponse<Void>>
}
