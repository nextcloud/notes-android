/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence

import android.app.Application
import android.content.Context
import com.nextcloud.android.sso.model.SingleSignOnAccount
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import it.niedermann.owncloud.notes.persistence.entity.Note
import it.niedermann.owncloud.notes.shared.model.ApiVersion
import it.niedermann.owncloud.notes.shared.model.directediting.DirectEditingRequestBody

class DirectEditingRepository private constructor(private val applicationContext: Context) {

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
