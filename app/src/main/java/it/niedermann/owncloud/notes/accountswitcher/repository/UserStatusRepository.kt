package it.niedermann.owncloud.notes.accountswitcher.repository

import android.content.Context
import com.nextcloud.android.sso.model.SingleSignOnAccount
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.users.PredefinedStatus
import com.owncloud.android.lib.resources.users.Status
import com.owncloud.android.lib.resources.users.StatusType
import it.niedermann.owncloud.notes.persistence.ApiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserStatusRepository(
    private val context: Context,
    private val ssoAccount: SingleSignOnAccount
) {
    companion object {
        private const val TAG = "UserStatusRepository"
    }

    private val api by lazy { ApiProvider.getInstance().getUserStatusAPI(context, ssoAccount) }

    suspend fun fetchPredefinedStatuses(): ArrayList<PredefinedStatus> = withContext(Dispatchers.IO) {
        try {
            val response = api.fetchPredefinedStatuses().execute()
            if (response.isSuccessful) {
                Log_OC.d(TAG, "✅ fetching predefined statuses successfully completed")
                response.body()?.ocs?.data ?: arrayListOf()
            } else {
                Log_OC.e(TAG, "❌ fetching predefined statuses failed")
                arrayListOf()
            }
        } catch (t: Throwable) {
            Log_OC.e(TAG, "❌ fetching predefined statuses failed", t)
            arrayListOf()
        }
    }

    suspend fun clearStatus(): Boolean = withContext(Dispatchers.IO) {
        try {
            val call = api.clearStatusMessage()
            val response = call.execute()
            if (response.isSuccessful) {
                Log_OC.d(TAG, "✅ clearing status successfully completed")
                true
            } else {
                Log_OC.e(TAG, "❌ clearing status failed")
                false
            }
        } catch (t: Throwable) {
            Log_OC.e(TAG, "❌ clearing status failed", t)
            false
        }
    }

    suspend fun setPredefinedStatus(messageId: String, clearAt: Long? = null): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val body = mutableMapOf("messageId" to messageId)
                clearAt?.let { body["clearAt"] = it.toString() }
                val call = api.setPredefinedStatusMessage(body)
                val response = call.execute()
                if (response.isSuccessful) {
                    Log_OC.d(TAG, "✅ predefined status successfully set")
                    true
                } else {
                    Log_OC.e(TAG, "❌ setting predefined status failed")
                    false
                }
            } catch (t: Throwable) {
                Log_OC.e(TAG, "❌ setting predefined status failed", t)
                false
            }
        }

    suspend fun setCustomStatus(
        message: String,
        statusIcon: String,
        clearAt: Long? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val body = mutableMapOf(
                "message" to message,
                "statusIcon" to statusIcon
            )
            clearAt?.let { body["clearAt"] = it.toString() }

            val call = api.setUserDefinedStatusMessage(body)
            val response = call.execute()
            if (response.isSuccessful) {
                Log_OC.d(TAG, "✅ setting custom status successfully completed")
                true
            } else {
                Log_OC.e(TAG, "❌ setting custom status failed")
                false
            }
        } catch (t: Throwable) {
            Log_OC.e(TAG, "❌setting custom status failed", t)
            false
        }
    }

    suspend fun fetchUserStatus(): Status? = withContext(Dispatchers.IO) {
        val offlineStatus = Status(StatusType.OFFLINE, "", "", -1)
        try {
            val call = api.fetchUserStatus()
            val response = call.execute()
            if (response.isSuccessful) {
                Log_OC.d(TAG, "✅ fetching user status successfully completed")
                response.body()?.ocs?.data ?: offlineStatus
            } else {
                Log_OC.e(TAG, "❌ fetching user status failed")
                offlineStatus
            }

        } catch (t: Throwable) {
            Log_OC.e(TAG, "❌ fetching user status failed $t")
            offlineStatus
        }
    }
}
