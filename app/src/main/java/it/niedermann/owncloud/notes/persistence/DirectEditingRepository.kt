package it.niedermann.owncloud.notes.persistence

import android.app.Application
import android.content.Context
import com.nextcloud.android.sso.model.SingleSignOnAccount
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import it.niedermann.owncloud.notes.persistence.entity.Note
import it.niedermann.owncloud.notes.shared.model.ApiVersion
import it.niedermann.owncloud.notes.shared.model.directediting.DirectEditingInfo
import it.niedermann.owncloud.notes.shared.model.directediting.DirectEditingRequestBody

// TODO better error handling
class DirectEditingRepository private constructor(private val applicationContext: Context) {

    private val apiProvider: ApiProvider by lazy { ApiProvider.getInstance() }
    private val notesRepository: NotesRepository by lazy {
        NotesRepository.getInstance(
            applicationContext,
        )
    }

    // TODO check for internet connection, check for directEditing supporting fileId as argument
    fun isDirectEditingSupportedByServer(account: SingleSignOnAccount): Single<Boolean> {
        val pathSingle = getNotesPath(account)
        val textAvailableSingle = getDirectEditingInfo(account)
            .map { it.editors.containsKey(SUPPORTED_EDITOR_ID) }

        return Single.zip(pathSingle, textAvailableSingle) { path, textAvailable ->
            path.isNotEmpty() && textAvailable
        }
    }

    private fun getDirectEditingInfo(account: SingleSignOnAccount): Single<DirectEditingInfo> {
        val filesAPI = apiProvider.getFilesAPI(applicationContext, account)
        return Single.fromCallable {
            val call = filesAPI.getDirectEditingInfo()
            val response = call.execute()
            response.body()?.ocs?.data
                ?: throw RuntimeException("No DirectEditingInfo available")
        }.subscribeOn(Schedulers.io())
    }

    private fun getNotesPath(account: SingleSignOnAccount): Single<String> {
        return Single.fromCallable {
            val call = notesRepository.getServerSettings(account, ApiVersion.API_VERSION_1_0)
            val response = call.execute()
            response.body()?.notesPath ?: throw RuntimeException("No notes path available")
        }.subscribeOn(Schedulers.io())
    }

    fun getDirectEditingUrl(
        account: SingleSignOnAccount,
        note: Note,
    ): Single<String> {
        return getNotesPath(account)
            .flatMap { notesPath ->
                val filesAPI = apiProvider.getFilesAPI(applicationContext, account)
                Single.fromCallable {
                    val call =
                        filesAPI.getDirectEditingUrl(
                            DirectEditingRequestBody(
                                path = notesPath,
                                editorId = SUPPORTED_EDITOR_ID,
                                fileId = note.remoteId!!,
                            ),
                        )
                    val response = call.execute()
                    response.body()?.ocs?.data?.url
                        ?: throw RuntimeException("No url available")
                }.subscribeOn(Schedulers.io())
            }
    }

    companion object {
        private const val SUPPORTED_EDITOR_ID = "text"

        private var instance: DirectEditingRepository? = null

        /**
         * @param applicationContext The application context. Do NOT use a view context to prevent leaks.
         */
        @JvmStatic
        fun getInstance(applicationContext: Context): DirectEditingRepository {
            require(applicationContext is Application)
            if (instance == null) {
                instance = DirectEditingRepository(applicationContext)
            }
            return instance!!
        }
    }
}
