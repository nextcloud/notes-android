package it.niedermann.owncloud.notes.share.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.nextcloud.android.sso.model.SingleSignOnAccount
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.shares.OCShare
import com.owncloud.android.lib.resources.shares.ShareType
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.persistence.ApiProvider
import it.niedermann.owncloud.notes.persistence.ApiResult
import it.niedermann.owncloud.notes.persistence.NotesRepository
import it.niedermann.owncloud.notes.persistence.entity.Note
import it.niedermann.owncloud.notes.persistence.entity.ShareEntity
import it.niedermann.owncloud.notes.share.model.CreateShareRequest
import it.niedermann.owncloud.notes.share.model.CreateShareResponse
import it.niedermann.owncloud.notes.share.model.ShareAttributesV1
import it.niedermann.owncloud.notes.share.model.ShareAttributesV2
import it.niedermann.owncloud.notes.share.model.SharePasswordRequest
import it.niedermann.owncloud.notes.share.model.UpdateSharePermissionRequest
import it.niedermann.owncloud.notes.share.model.UpdateShareRequest
import it.niedermann.owncloud.notes.share.model.toOCShareList
import it.niedermann.owncloud.notes.shared.model.ApiVersion
import it.niedermann.owncloud.notes.shared.model.Capabilities
import it.niedermann.owncloud.notes.shared.model.NotesSettings
import it.niedermann.owncloud.notes.shared.model.OcsResponse
import it.niedermann.owncloud.notes.shared.util.StringConstants
import it.niedermann.owncloud.notes.shared.util.extensions.getErrorMessage
import it.niedermann.owncloud.notes.shared.util.extensions.toExpirationDateString
import org.json.JSONObject
import java.util.Date

class ShareRepository(
    private val applicationContext: Context,
    private val account: SingleSignOnAccount
) {

    private val tag = "ShareRepository"
    private val gson = Gson()
    private val apiProvider: ApiProvider by lazy { ApiProvider.getInstance() }
    private val notesRepository: NotesRepository by lazy {
        NotesRepository.getInstance(
            applicationContext,
        )
    }

    private fun getNotesPathResponseResult(): NotesSettings? {
        val notesPathCall = notesRepository.getServerSettings(account, ApiVersion.API_VERSION_1_0)
        val notesPathResponse = notesPathCall.execute()
        return notesPathResponse.body()
    }

    private fun getNotePath(note: Note): String? {
        val notesPathResponseResult = getNotesPathResponseResult() ?: return null
        val notesPath = notesPathResponseResult.notesPath
        val notesSuffix = notesPathResponseResult.fileSuffix
        return StringConstants.PATH + notesPath + StringConstants.PATH + note.title + notesSuffix
    }

    fun getShareEntitiesForSpecificNote(note: Note): List<ShareEntity> {
        val path = getNotePath(note)
        return notesRepository.getShareEntities(path)
    }

    private fun getExpirationDate(chosenExpDateInMills: Long): String? {
        if (chosenExpDateInMills == -1L) {
            return null
        }

        return Date(chosenExpDateInMills).toExpirationDateString()
    }

    fun getCapabilities(): Capabilities = notesRepository.capabilities

    // region API calls
    fun getSharesForNotesAndSaveShareEntities() {
        val notesPathResponseResult = getNotesPathResponseResult() ?: return
        val notesPath = notesPathResponseResult.notesPath
        val remotePath = "/$notesPath"

        val shareAPI = apiProvider.getShareAPI(applicationContext, account)
        val call = shareAPI.getSharesForSpecificNote(remotePath)
        val entities = arrayListOf<ShareEntity>()

        try {
            if (call != null) {
                val respOCS = call["ocs"] as? LinkedTreeMap<*, *>
                val respData = respOCS?.getList("data")
                respData?.forEach { data ->
                    val map = data as? LinkedTreeMap<*, *>
                    val id = map?.get("id") as? String
                    val note = map?.get("note") as? String
                    val path = map?.get("path") as? String
                    val fileTarget = map?.get("file_target") as? String
                    val shareWith = map?.get("share_with") as? String
                    val shareWithDisplayName = map?.get("share_with_displayname") as? String
                    val uidFileOwner = map?.get("uid_file_owner") as? String
                    val displayNameFileOwner = map?.get("displayname_file_owner") as? String
                    val uidOwner = map?.get("uid_owner") as? String
                    val displayNameOwner = map?.get("displayname_owner") as? String
                    val url = map?.get("url") as? String

                    id?.toInt()?.let {
                        val entity = ShareEntity(
                            id = it,
                            note = note,
                            path = path,
                            file_target = fileTarget,
                            share_with = shareWith,
                            share_with_displayname = shareWithDisplayName,
                            uid_file_owner = uidFileOwner,
                            displayname_file_owner = displayNameFileOwner,
                            uid_owner = uidOwner,
                            displayname_owner = displayNameOwner,
                            url = url
                        )

                        entities.add(entity)
                    }
                }

                notesRepository.addShareEntities(entities)
            }
        } catch (e: Exception) {
            Log_OC.d(tag, "Exception while getSharesForSpecificNote: $e")
        }
    }

    private fun LinkedTreeMap<*, *>.getList(key: String): ArrayList<*>? = this[key] as? ArrayList<*>

    fun getSharees(
        searchString: String,
        page: Int,
        perPage: Int
    ): ArrayList<JSONObject> {
        val shareAPI = apiProvider.getShareAPI(applicationContext, account)
        val call = shareAPI.getSharees(
            search = searchString,
            page = page.toString(),
            perPage = perPage.toString()
        )
        return if (call != null) {
            val respOCS = call["ocs"] as? LinkedTreeMap<*, *>
            val respData = respOCS?.get("data") as? LinkedTreeMap<*, *>
            val respExact = respData?.get("exact") as? LinkedTreeMap<*, *>

            val respExactUsers = respExact?.getList("users")
            val respExactGroups = respExact?.getList("groups")
            val respExactRemotes = respExact?.getList("remotes")
            val respExactEmails = respExact?.getList("emails")
            val respExactCircles =
                respExact?.takeIf { it.containsKey("circles") }?.getList("circles")
            val respExactRooms = respExact?.takeIf { it.containsKey("rooms") }?.getList("rooms")

            val respPartialUsers = respData?.getList("users")
            val respPartialGroups = respData?.getList("groups")
            val respPartialRemotes = respData?.getList("remotes")
            val respPartialCircles =
                respData?.takeIf { it.containsKey("circles") }?.getList("circles")
            val respPartialRooms = respData?.takeIf { it.containsKey("rooms") }?.getList("rooms")

            val jsonResults = listOfNotNull(
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

            return jsonResults.flatMap { jsonResult ->
                jsonResult.map { linkedTreeMap ->
                    JSONObject(gson.toJson(linkedTreeMap))
                }
            }.toCollection(ArrayList())
        } else {
            ArrayList()
        }
    }

    fun getUpdateShareRequest(
        downloadPermission: Boolean,
        share: OCShare?,
        noteText: String,
        password: String,
        sendEmail: Boolean,
        chosenExpDateInMills: Long,
        permission: Int
    ): UpdateShareRequest {
        val capabilities = getCapabilities()
        val shouldUseShareAttributesV2 = (capabilities.nextcloudMajorVersion?.toInt() ?: 0) >= 30

        val shareAttributes = arrayOf(
            if (shouldUseShareAttributesV2) {
                ShareAttributesV2(
                    scope = "permissions",
                    key = "download",
                    value = downloadPermission
                )
            } else {
                ShareAttributesV1(
                    scope = "permissions",
                    key = "download",
                    enabled = downloadPermission
                )
            }
        )

        val attributes = gson.toJson(shareAttributes)

        return UpdateShareRequest(
            share_id = share!!.id.toInt(),
            permissions = if (permission == -1) null else permission,
            password = password,
            publicUpload = "false",
            expireDate = getExpirationDate(chosenExpDateInMills),
            note = noteText,
            attributes = attributes,
            sendMail = sendEmail.toString()
        )
    }

    fun getShares(remoteId: Long): List<OCShare>? {
        val shareAPI = apiProvider.getShareAPI(applicationContext, account)
        val call = shareAPI.getShares(remoteId)
        val response = call.execute()

        return try {
            if (response.isSuccessful) {
                val result =
                    response.body()?.ocs?.data ?: throw RuntimeException("No shares available")
                result.toOCShareList()
            } else {
                Log_OC.d(tag, "Failed to getShares: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log_OC.d(tag, "Exception while getShares: $e")
            null
        }
    }

    fun sendEmail(shareId: Long, requestBody: SharePasswordRequest): Boolean {
        val shareAPI = apiProvider.getShareAPI(applicationContext, account)
        val call = shareAPI.sendEmail(shareId, requestBody)
        val response = call.execute()

        return try {
            if (response.isSuccessful) {
                true
            } else {
                Log_OC.d(tag, "Failed to send-email: ${response.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            Log_OC.d(tag, "Exception while send-email: $e")
            false
        }
    }

    fun getShareFromNote(note: Note): List<OCShare>? {
        val shareAPI = apiProvider.getShareAPI(applicationContext, account)
        val path = getNotePath(note) ?: return null
        val call = shareAPI.getShareFromNote(path)
        val response = call.execute()

        return try {
            if (response.isSuccessful) {
                val body = response.body()
                Log_OC.d(tag, "Response successful: $body")
                body?.ocs?.data?.toOCShareList()
            } else {
                val errorBody = response.errorBody()?.string()
                Log_OC.d(tag, "Response failed: $errorBody")
                null
            }
        } catch (e: Exception) {
            Log_OC.d(tag, "Exception while getting share from note: ", e)
            null
        }
    }

    fun removeShare(share: OCShare, note: Note): Boolean {
        val shareAPI = apiProvider.getShareAPI(applicationContext, account)

        return try {
            val call = shareAPI.removeShare(share.id)
            val response = call.execute()
            if (response.isSuccessful) {

                if (share.shareType != null && share.shareType == ShareType.PUBLIC_LINK) {
                    note.setIsShared(false)
                    updateNote(note)
                }

                Log_OC.d(tag, "Share removed successfully.")
            } else {
                Log_OC.d(tag, "Failed to remove share: ${response.errorBody()?.string()}")
            }
            response.isSuccessful
        } catch (e: Exception) {
            Log_OC.d(tag, "Exception while removing share", e)
            false
        }
    }

    fun updateShare(
        shareId: Long,
        requestBody: UpdateShareRequest
    ): ApiResult<OcsResponse<CreateShareResponse>?> {
        val shareAPI = apiProvider.getShareAPI(applicationContext, account)
        val call = shareAPI.updateShare(shareId, requestBody)
        val response = call.execute()
        return try {
            if (response.isSuccessful) {
                Log_OC.d(tag, "Share updated successfully: ${response.body().toString()}")
                ApiResult.Success(
                    data = response.body(),
                    message = applicationContext.getString(R.string.note_share_created)
                )
            } else {
                Log_OC.d(tag, "Failed to update share: ${response.errorBody()?.string()}")
                ApiResult.Error(message = response.getErrorMessage() ?: "")
            }
        } catch (e: Exception) {
            Log_OC.d(tag, "Exception while updating share", e)
            ApiResult.Error(message = e.message ?: "")
        }
    }

    fun updateNote(note: Note) = notesRepository.updateNote(note)

    fun addShare(
        note: Note,
        shareType: ShareType,
        shareWith: String,
        publicUpload: String = "false",
        password: String = "",
        permissions: Int = 0,
        shareNote: String = ""
    ): ApiResult<OcsResponse<CreateShareResponse>?> {
        val defaultErrorMessage =
            applicationContext.getString(R.string.note_share_activity_cannot_created)
        val notesPathCall = notesRepository.getServerSettings(account, ApiVersion.API_VERSION_1_0)
        val notesPathResponse = notesPathCall.execute()
        val notesPathResponseResult =
            notesPathResponse.body() ?: return ApiResult.Error(message = defaultErrorMessage)
        val notesPath = notesPathResponseResult.notesPath
        val notesSuffix = notesPathResponseResult.fileSuffix

        val requestBody = CreateShareRequest(
            path = StringConstants.PATH + notesPath + StringConstants.PATH + note.title + notesSuffix,
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

        return try {
            if (response.isSuccessful) {
                val createShareResponse = response.body()
                Log_OC.d(tag, "Response successful: $createShareResponse")
                ApiResult.Success(
                    data = createShareResponse,
                    message = applicationContext.getString(R.string.note_share_created)
                )
            } else {
                val errorMessage = response.getErrorMessage()
                if (errorMessage == null) {
                    return ApiResult.Error(message = defaultErrorMessage)
                }
                Log_OC.d(tag, "Response failed: $errorMessage")
                ApiResult.Error(message = errorMessage)
            }
        } catch (e: Exception) {
            Log_OC.d(tag, "Exception while creating share", e)
            ApiResult.Error(message = defaultErrorMessage)
        }
    }

    fun updateSharePermission(
        shareId: Long,
        permissions: Int? = null,
    ): ApiResult<OcsResponse<CreateShareResponse>?> {
        val shareAPI = apiProvider.getShareAPI(applicationContext, account)
        val requestBody = UpdateSharePermissionRequest(permissions = permissions)

        return try {
            val call = shareAPI.updateSharePermission(shareId, requestBody)
            val response = call.execute()
            if (response.isSuccessful) {
                Log_OC.d(tag, "Share updated successfully: ${response.body()}")
                ApiResult.Success(response.body())
            } else {
                Log_OC.d(tag, "Failed to update share: ${response.errorBody()?.string()}")
                ApiResult.Error(message = response.getErrorMessage() ?: "", code = null)
            }
        } catch (e: Exception) {
            Log_OC.d(tag, "Exception while updating share", e)
            ApiResult.Error(message = e.message ?: "", code = null)
        }
    }
    // endregion
}
