package it.niedermann.owncloud.notes.activities.repository

import android.content.Context
import android.util.Log
import com.nextcloud.android.sso.model.SingleSignOnAccount
import it.niedermann.owncloud.notes.persistence.ApiProvider
import it.niedermann.owncloud.notes.persistence.entity.Note

class ActivitiesRepository(
    private val context: Context,
    private val ssoAccount: SingleSignOnAccount
)  {

    private val api by lazy { ApiProvider.getInstance().getActivitiesAPI(context, ssoAccount) }

    suspend fun fetchActivities(note: Note) {
        val lastGiven = 0L  // TODO what's this?
        val response = api.fetch(note.id, lastGiven)
        if(response.isSuccess) {
            Log.d("DEBUG", response.resultData.toString())
            return
        }
        Log.e("DEBUG", "Unable to fetch activities")

    }




    companion object {
        private const val TAG = "ActivitiesRepository"
    }
}