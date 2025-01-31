package it.niedermann.owncloud.notes.share.repository

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.util.Log
import com.nextcloud.android.sso.model.SingleSignOnAccount
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.shares.OCShare
import com.owncloud.android.lib.resources.shares.ShareType
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import it.niedermann.owncloud.notes.persistence.ApiProvider
import it.niedermann.owncloud.notes.persistence.NotesRepository
import it.niedermann.owncloud.notes.persistence.entity.Note
import it.niedermann.owncloud.notes.share.model.CreateShareRequest
import it.niedermann.owncloud.notes.share.model.UpdateShareInformationRequest
import it.niedermann.owncloud.notes.share.model.UpdateSharePermissionRequest
import it.niedermann.owncloud.notes.share.model.UpdateShareRequest
import it.niedermann.owncloud.notes.shared.model.ApiVersion
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date
import java.util.Locale

class ShareRepository(private val applicationContext: Context, private val account: SingleSignOnAccount) {

    private val tag = "ShareRepository"
    private val apiProvider: ApiProvider by lazy { ApiProvider.getInstance() }
    private val notesRepository: NotesRepository by lazy {
        NotesRepository.getInstance(
            applicationContext,
        )
    }

    private fun getNotesPath(): Single<String> {
        return Single.fromCallable {
            val call = notesRepository.getServerSettings(account, ApiVersion.API_VERSION_1_0)
            val response = call.execute()
            response.body()?.notesPath ?: throw RuntimeException("No notes path available")
        }.subscribeOn(Schedulers.io())
    }

    fun getSharees(
        searchString: String,
        page: Int,
        perPage: Int
    ): Single<ArrayList<JSONObject>> {
        return Single.fromCallable {
            val shareAPI = apiProvider.getShareAPI(applicationContext, account)
            val call = shareAPI.getSharees(search = searchString, page = page, perPage = perPage)
            val response = call.execute()

            val respJSON = JSONObject(response.body().toString())
            val respOCS = respJSON.getJSONObject("ocs")
            val respData = respOCS.getJSONObject("data")
            val respExact = respData.getJSONObject("exact")
            val respExactUsers = respExact.getJSONArray("users")
            val respExactGroups = respExact.getJSONArray("groups")
            val respExactRemotes = respExact.getJSONArray("remotes")
            val respExactCircles = if (respExact.has("circles")) {
                respExact.getJSONArray("circles")
            } else {
                JSONArray()
            }
            val respExactRooms = if (respExact.has("rooms")) {
                respExact.getJSONArray("rooms")
            } else {
                JSONArray()
            }

            val respExactEmails = respExact.getJSONArray("emails")
            val respPartialUsers = respData.getJSONArray("users")
            val respPartialGroups = respData.getJSONArray("groups")
            val respPartialRemotes = respData.getJSONArray("remotes")
            val respPartialCircles = if (respData.has("circles")) {
                respData.getJSONArray("circles")
            } else {
                JSONArray()
            }
            val respPartialRooms = if (respData.has("rooms")) {
                respData.getJSONArray("rooms")
            } else {
                JSONArray()
            }

            val jsonResults = arrayOf(
                respExactUsers,
                respExactGroups,
                respExactRemotes,
                respExactRooms,
                respExactEmails,
                respExactCircles,
                respPartialUsers,
                respPartialGroups,
                respPartialRemotes,
                respPartialRooms,
                respPartialCircles
            )
            val data: ArrayList<JSONObject> = ArrayList()
            val var25 = jsonResults
            val var26 = jsonResults.size

            for (var27 in 0 until var26) {
                val jsonResult = var25[var27]

                for (j in 0 until jsonResult.length()) {
                    val jsonObject = jsonResult.getJSONObject(j)
                    data.add(jsonObject)
                }
            }
            data
        }.subscribeOn(Schedulers.io())
    }

    fun getShares(
        remoteId: Long
    ): Single<List<OCShare>> {
        return Single.fromCallable {
            val shareAPI = apiProvider.getShareAPI(applicationContext, account)
            val call = shareAPI.getShares(remoteId)
            val response = call.execute()
            response.body()?.ocs?.data ?: throw RuntimeException("No shares available")
        }.subscribeOn(Schedulers.io())
    }

    fun getSharesForFile(
        note: Note,
        reshares: Boolean = false,
        subfiles: Boolean = false
    ): Single<List<OCShare>> {
        return getNotesPath()
            .flatMap { notesPath ->
                Single.fromCallable {
                    val shareAPI = apiProvider.getShareAPI(applicationContext, account)
                    val call = shareAPI.getSharesForFile(
                        remoteFilePath = notesPath + "/" + note.remoteId,
                        reshares = reshares,
                        subfiles = subfiles
                    )
                    val response = call.execute()
                    response.body()?.ocs?.data ?: throw RuntimeException("No shares available")
                }.subscribeOn(Schedulers.io())
            }
    }

    fun removeShare(
        shareId: Long
    ): Boolean {
        val shareAPI = apiProvider.getShareAPI(applicationContext, account)

        return try {
            val call = shareAPI.removeShare(shareId)
            val response = call.execute()
            if (response.isSuccessful) {
                Log.d("RemoveShare", "Share removed successfully.")
            } else {
                Log.e("RemoveShare", "Failed to remove share: ${response.errorBody()?.string()}")
            }
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("RemoveShare", "Exception while removing share", e)
            false
        }
    }

    fun updateShare(
        shareId: Long,
        noteText: String
    ): Boolean {
        val shareAPI = apiProvider.getShareAPI(applicationContext, account)
        val requestBody = UpdateShareRequest(shareId.toString(), noteText)
        val call = shareAPI.updateShare(requestBody)
        val response = call.execute()
        if (response.isSuccessful) {
            val updateShareResponse = response.body()
            Log.d("", "Response successful: $updateShareResponse")
        } else {
            val errorBody = response.errorBody()?.string()
            Log.d("", "Response failed:$errorBody")
        }

        return response.isSuccessful
    }

    fun addShare(
        note: Note,
        shareType: ShareType,
        shareWith: String,
        publicUpload: String = "false",
        password: String = "",
        permissions: Int = 0,
        shareNote: String = ""
    ): Boolean {
        val notesPathCall = notesRepository.getServerSettings(account, ApiVersion.API_VERSION_1_0)
        val notesPathResponse = notesPathCall.execute()
        val notesPathResponseResult = notesPathResponse.body() ?: return false
        val notesPath = notesPathResponseResult.notesPath
        val notesSuffix = notesPathResponseResult.fileSuffix

        val requestBody = CreateShareRequest(
            path = "/" + notesPath + "/" + note.title + notesSuffix,
            shareType = shareType.value,
            shareWith = shareWith,
            publicUpload = publicUpload,
            password = password,
            permissions = permissions,
            note = shareNote
        )

        val shareAPI = apiProvider.getShareAPI(applicationContext, account)
        val call = shareAPI.addShare(request = requestBody)
        val response = call.execute()
        if (response.isSuccessful) {
            val createShareResponse = response.body()
            Log.d("", "Response successful: $createShareResponse")
        } else {
            val errorBody = response.errorBody()?.string()
            Log.d("", "Response failed:$errorBody")
        }

        return response.isSuccessful
    }

    fun updateShareInformation(
        shareId: Long,
        password: String? = null,
        expirationDateMillis: Long? = null,
        permissions: Int? = null,
        hideFileDownload: Boolean? = null,
        note: String? = null,
        label: String? = null
    ): Boolean {
        if (shareId <= 0) {
            Log_OC.d(tag, "share id is not valid, updateShareInformation cancelled")
            return false
        }

        val shareAPI = apiProvider.getShareAPI(applicationContext, account)

        val expirationDate = expirationDateMillis?.let {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it))
        }

        val requestBody = UpdateShareInformationRequest(
            shareId = shareId.toString(),
            password = password,
            expireDate = expirationDate,
            permissions = permissions,
            hideDownload = hideFileDownload,
            note = note,
            label = label
        )

        return try {
            val call = shareAPI.updateShareInfo(shareId, requestBody)
            val response = call.execute()
            if (response.isSuccessful) {
                Log.d("UpdateShare", "Share updated successfully: ${response.body()}")
            } else {
                Log.e("UpdateShare", "Failed to update share: ${response.errorBody()?.string()}")
            }
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("UpdateShare", "Exception while updating share", e)
            false
        }
    }

    fun updateSharePermission(
        shareId: Long,
        permissions: Int? = null,
    ): Boolean {
        val shareAPI = apiProvider.getShareAPI(applicationContext, account)
        val requestBody = UpdateSharePermissionRequest(permissions = permissions,)

        return try {
            val call = shareAPI.updateSharePermission(shareId, requestBody)
            val response = call.execute()
            if (response.isSuccessful) {
                Log.d("UpdateShare", "Share updated successfully: ${response.body()}")
            } else {
                Log.e("UpdateShare", "Failed to update share: ${response.errorBody()?.string()}")
            }
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("UpdateShare", "Exception while updating share", e)
            false
        }
    }
}
