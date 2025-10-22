package it.niedermann.owncloud.notes.accountswitcher.repository

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import com.nextcloud.android.sso.model.SingleSignOnAccount
import it.niedermann.owncloud.notes.persistence.ApiProvider
import it.niedermann.owncloud.notes.shared.model.OcsResponse
import retrofit2.Response
import kotlin.getValue

class UserStatusRepository(
    private val context: Context,
    private val ssoAccount: SingleSignOnAccount
) {
    companion object {
        private const val TAG = "UserStatusRepository"
    }

    private val ocsAPI by lazy { ApiProvider.getInstance().getOcsAPI(context, ssoAccount) }

    @WorkerThread
    fun clearStatus(): Boolean {
        return try {
            val response: Response<OcsResponse<Void>> = ocsAPI.clearStatusMessage().execute()
            response.isSuccessful
        } catch (t: Throwable) {
            Log.e(TAG, "Clearing status failed", t)
            false
        }
    }

    @WorkerThread
    fun setPredefinedStatus(messageId: String, clearAt: Long? = null): Boolean {
        val body = mutableMapOf<String, String>("messageId" to messageId)
        clearAt?.let { body["clearAt"] = it.toString() }

        return try {
            val response: Response<OcsResponse<Void>> = ocsAPI.setPredefinedStatusMessage(body).execute()
            response.isSuccessful
        } catch (t: Throwable) {
            Log.e(TAG, "Setting predefined status failed", t)
            false
        }
    }

    @WorkerThread
    fun setCustomStatus(message: String, statusIcon: String, clearAt: Long? = null): Boolean {
        val body = mutableMapOf(
            "message" to message,
            "statusIcon" to statusIcon
        )
        clearAt?.let { body["clearAt"] = it.toString() }

        return try {
            val response: Response<OcsResponse<Void>> = ocsAPI.setUserDefinedStatusMessage(body).execute()
            response.isSuccessful
        } catch (t: Throwable) {
            Log.e(TAG, "Setting custom status failed", t)
            false
        }
    }
}
