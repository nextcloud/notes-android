package it.niedermann.owncloud.notes.activities.repository

import android.content.Context
import com.nextcloud.android.sso.model.SingleSignOnAccount
import it.niedermann.owncloud.notes.persistence.ApiProvider
import java.net.HttpURLConnection
import kotlin.getValue

class ActivitiesRepository(
    private val context: Context,
    private val ssoAccount: SingleSignOnAccount
) {

    private val api by lazy { ApiProvider.getInstance().getActivitiesAPI(context, ssoAccount) }

    fun fetchActivities(noteId: Long, since: Long? = null): ActivitiesResult {
        val response = api.fetchActivities(noteId, since, PAGE_SIZE).execute()

        if (response.isSuccessful || response.code() == HttpURLConnection.HTTP_NOT_MODIFIED) {
            return ActivitiesResult.Success(
                response.body()?.ocs?.data ?: listOf(),
                response.headers()[HEADER_LAST_GIVEN]?.toLongOrNull()
            )
        }

        // Activity capability not present
        if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
            return ActivitiesResult.Success(listOf(), null)
        }

        return ActivitiesResult.Failed()
    }

    companion object {
        const val PAGE_SIZE = 50
        private const val HEADER_LAST_GIVEN = "X-Activity-Last-Given"
    }

}
