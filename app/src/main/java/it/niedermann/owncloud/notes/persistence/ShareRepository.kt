package it.niedermann.owncloud.notes.persistence

import android.app.Application
import android.content.Context
import com.nextcloud.android.sso.api.EmptyResponse
import com.nextcloud.android.sso.model.SingleSignOnAccount
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.shares.GetShareesRemoteOperation
import com.owncloud.android.lib.resources.shares.OCShare
import com.owncloud.android.lib.resources.shares.ShareType
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import it.niedermann.owncloud.notes.persistence.entity.Note
import it.niedermann.owncloud.notes.shared.model.ApiVersion
import org.json.JSONArray
import org.json.JSONObject

class ShareRepository private constructor(private val applicationContext: Context) {

    private val apiProvider: ApiProvider by lazy { ApiProvider.getInstance() }
    private val notesRepository: NotesRepository by lazy {
        NotesRepository.getInstance(
            applicationContext,
        )
    }

    private fun getNotesPath(account: SingleSignOnAccount): Single<String> {
        return Single.fromCallable {
            val call = notesRepository.getServerSettings(account, ApiVersion.API_VERSION_1_0)
            val response = call.execute()
            response.body()?.notesPath ?: throw RuntimeException("No notes path available")
        }.subscribeOn(Schedulers.io())
    }

    fun getSharees(
        account: SingleSignOnAccount,
        searchString: String,
        page: Int,
        perPage: Int
    ): Single<ArrayList<JSONObject>> {
        return Single.fromCallable {
            val shareAPI = apiProvider.getShareAPI(applicationContext, account)
            val call2 = shareAPI.getSharees2(search = searchString, page = page, perPage = perPage)
            val response2 = call2.execute()

            val respJSON = JSONObject(response2.body().toString())
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
        account: SingleSignOnAccount,
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
        account: SingleSignOnAccount,
        note: Note,
        reshares: Boolean = false,
        subfiles: Boolean = false
    ): Single<List<OCShare>> {
        return getNotesPath(account)
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

    fun deleteShare(
        account: SingleSignOnAccount,
        remoteShareId: Long
    ): Single<EmptyResponse> {
        return Single.fromCallable {
            val shareAPI = apiProvider.getShareAPI(applicationContext, account)
            val call = shareAPI.deleteShare(remoteShareId)
            val response = call.execute()
            response.body() ?: throw RuntimeException("No shares available")
        }.subscribeOn(Schedulers.io())
    }

    fun updateShare(
        account: SingleSignOnAccount,
        remoteShareId: Long
    ): Single<List<OCShare>> {
        return Single.fromCallable {
            val shareAPI = apiProvider.getShareAPI(applicationContext, account)
            val call = shareAPI.updateShare(remoteShareId)
            val response = call.execute()
            response.body()?.ocs?.data ?: throw RuntimeException("Share update failed")
        }.subscribeOn(Schedulers.io())
    }

    fun addShare(
        account: SingleSignOnAccount,
        note: Note,
        shareType: ShareType,
        shareWith: String,
        publicUpload: Boolean = false,
        password: String = "",
        permissions: Int = 0,
        getShareDetails: Boolean = true,
        shareNote: String = ""
    ): Single<List<OCShare>> {
        return getNotesPath(account)
            .flatMap { notesPath ->
                Single.fromCallable {
                    val shareAPI = apiProvider.getShareAPI(applicationContext, account)
                    val call = shareAPI.addShare(
                        remoteFilePath = notesPath + "/" + note.remoteId,
                        shareType = shareType,
                        shareWith = shareWith,
                        publicUpload = publicUpload,
                        password = password,
                        permissions = permissions,
                        getShareDetails = getShareDetails,
                        note = shareNote
                    )
                    val response = call.execute()
                    response.body()?.ocs?.data ?: throw RuntimeException("Share creation failed")
                }.subscribeOn(Schedulers.io())
            }
    }

    companion object {
        private var instance: ShareRepository? = null

        @JvmStatic
        fun getInstance(applicationContext: Context): ShareRepository {
            require(applicationContext is Application)
            if (instance == null) {
                instance = ShareRepository(applicationContext)
            }
            return instance!!
        }
    }
}
