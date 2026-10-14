/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence

import android.util.Log
import com.nextcloud.android.sso.api.ParsedResponse
import it.niedermann.owncloud.notes.BuildConfig
import it.niedermann.owncloud.notes.persistence.entity.Account
import it.niedermann.owncloud.notes.persistence.entity.Note
import it.niedermann.owncloud.notes.shared.model.DBStatus
import it.niedermann.owncloud.notes.shared.util.ApiVersionUtil
import it.niedermann.owncloud.notes.shared.util.NoteUtil.generateNoteExcerpt
import java.util.Calendar
import java.util.Date

class RemoteNotesMerger(private val repo: NotesRepository, private val localAccount: Account) {

    companion object {
        private val TAG = RemoteNotesMerger::class.java.simpleName
        private const val HEADER_KEY_X_NOTES_API_VERSIONS = "X-Notes-API-Versions"
        private const val HEADER_KEY_ETAG = "ETag"
        private const val HEADER_KEY_LAST_MODIFIED = "Last-Modified"
    }

    fun merge(fetchResponse: ParsedResponse<List<Note>>, idMap: Map<Long, Long>) {
        val remoteIds = applyRemoteNotes(fetchResponse.response, idMap)
        removeRemotelyDeletedNotes(idMap, remoteIds)
        updateSyncState(fetchResponse.headers)
    }

    private fun applyRemoteNotes(remoteNotes: List<Note>, idMap: Map<Long, Long>): Set<Long?> {
        val remoteIds = HashSet<Long?>()
        for (remoteNote in remoteNotes) {
            Log.v(TAG, "   Process Remote Note: " + if (BuildConfig.DEBUG) remoteNote else remoteNote.title)
            remoteIds.add(remoteNote.remoteId)
            applyRemoteNote(remoteNote, idMap)
        }
        return remoteIds
    }

    private fun applyRemoteNote(remoteNote: Note, idMap: Map<Long, Long>) {
        val modified = remoteNote.modified
        if (modified == null) {
            Log.v(TAG, "   ... unchanged")
            return
        }
        if (!idMap.containsKey(remoteNote.remoteId)) {
            Log.v(TAG, "   ... create")
            repo.addNote(localAccount.id, remoteNote)
            return
        }

        Log.v(TAG, "   ... found → Update")
        val localId = idMap[remoteNote.remoteId]
        if (localId == null) {
            val note = if (BuildConfig.DEBUG) remoteNote else remoteNote.title
            Log.e(TAG, "Tried to update note from server, but local id of note is null. $note")
            return
        }
        repo.updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
            localId,
            modified.timeInMillis,
            remoteNote.title,
            remoteNote.favorite,
            remoteNote.category,
            remoteNote.eTag,
            remoteNote.content,
            generateNoteExcerpt(remoteNote.content, remoteNote.title)
        )
    }

    private fun removeRemotelyDeletedNotes(idMap: Map<Long, Long>, remoteIds: Set<Long?>) {
        Log.d(TAG, "   Remove remotely deleted Notes (only those without local changes)")
        for ((remoteId, localId) in idMap) {
            if (!remoteIds.contains(remoteId)) {
                Log.v(TAG, "   ... remove $localId")
                repo.deleteByNoteId(localId, DBStatus.VOID)
            }
        }
    }

    private fun updateSyncState(headers: Map<String, String>) {
        localAccount.eTag = headers[HEADER_KEY_ETAG]

        val lastModified = parseLastModified(headers[HEADER_KEY_LAST_MODIFIED])
        Log.d(TAG, "ETag: ${headers[HEADER_KEY_ETAG]}; Last-Modified: $lastModified ($lastModified)")
        localAccount.modified = lastModified

        repo.updateETag(localAccount.id, localAccount.eTag)
        repo.updateModified(localAccount.id, lastModified.timeInMillis)

        val newApiVersion = ApiVersionUtil.sanitize(headers[HEADER_KEY_X_NOTES_API_VERSIONS])
        localAccount.apiVersion = newApiVersion
        repo.updateApiVersion(localAccount.id, newApiVersion)
        Log.d(TAG, "ApiVersion: $newApiVersion")
    }

    @Suppress("DEPRECATION")
    private fun parseLastModified(lastModifiedHeader: String?): Calendar = Calendar.getInstance().apply {
        timeInMillis = 0
        if (lastModifiedHeader != null) {
            timeInMillis = Date.parse(lastModifiedHeader)
        }
    }
}
