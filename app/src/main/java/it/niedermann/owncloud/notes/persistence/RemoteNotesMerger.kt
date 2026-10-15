/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence

import android.util.Log
import com.nextcloud.android.sso.api.ParsedResponse
import it.niedermann.owncloud.notes.persistence.entity.Account
import it.niedermann.owncloud.notes.persistence.entity.Note
import it.niedermann.owncloud.notes.shared.model.DBStatus
import it.niedermann.owncloud.notes.shared.util.ApiVersionUtil
import it.niedermann.owncloud.notes.shared.util.NoteUtil.generateNoteExcerpt
import java.util.Calendar
import java.util.Date

class RemoteNotesMerger(private val repo: NotesRepository, private val localAccount: Account) {

    companion object {
        private val TAG = NotesServerSyncTask::class.java.simpleName
        private const val HEADER_KEY_X_NOTES_API_VERSIONS = "X-Notes-API-Versions"
        private const val HEADER_KEY_ETAG = "ETag"
        private const val HEADER_KEY_LAST_MODIFIED = "Last-Modified"
    }

    fun merge(fetchResponse: ParsedResponse<List<Note>>, idMap: Map<Long, Long>) {
        Log.d(TAG, "Pull: received ${fetchResponse.response.size} remote note(s)")
        val remoteIds = applyRemoteNotes(fetchResponse.response, idMap)
        removeRemotelyDeletedNotes(idMap, remoteIds)
        updateSyncState(fetchResponse.headers)
    }

    private fun applyRemoteNotes(remoteNotes: List<Note>, idMap: Map<Long, Long>): Set<Long?> {
        val remoteIds = HashSet<Long?>()
        for (remoteNote in remoteNotes) {
            remoteIds.add(remoteNote.remoteId)
            applyRemoteNote(remoteNote, idMap)
        }
        return remoteIds
    }

    private fun applyRemoteNote(remoteNote: Note, idMap: Map<Long, Long>) {
        val modified = remoteNote.modified
        if (modified == null) {
            Log.v(TAG, "  → remote note ${remoteNote.remoteId} unchanged")
            return
        }
        val label = "remote note ${remoteNote.remoteId} \"${remoteNote.title}\""
        if (!idMap.containsKey(remoteNote.remoteId)) {
            Log.d(TAG, "  → $label is new, creating local note")
            repo.addNote(localAccount.id, remoteNote)
            return
        }

        val localId = idMap[remoteNote.remoteId]
        if (localId == null) {
            Log.e(TAG, "  → $label has no local id, cannot update it")
            return
        }
        Log.d(TAG, "  → $label changed, updating local note #$localId")
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
        for ((remoteId, localId) in idMap) {
            if (!remoteIds.contains(remoteId)) {
                Log.d(TAG, "  → remote note $remoteId was deleted on server, removing local note #$localId")
                repo.deleteByNoteId(localId, DBStatus.VOID)
            }
        }
    }

    private fun updateSyncState(headers: Map<String, String>) {
        localAccount.eTag = headers[HEADER_KEY_ETAG]

        val lastModified = parseLastModified(headers[HEADER_KEY_LAST_MODIFIED])
        localAccount.modified = lastModified

        repo.updateETag(localAccount.id, localAccount.eTag)
        repo.updateModified(localAccount.id, lastModified.timeInMillis)

        val newApiVersion = ApiVersionUtil.sanitize(headers[HEADER_KEY_X_NOTES_API_VERSIONS])
        localAccount.apiVersion = newApiVersion
        repo.updateApiVersion(localAccount.id, newApiVersion)
        Log.d(TAG, "Pull: saved ETag ${localAccount.eTag}, last modified ${lastModified.time}, API $newApiVersion")
    }

    @Suppress("DEPRECATION")
    private fun parseLastModified(lastModifiedHeader: String?): Calendar = Calendar.getInstance().apply {
        timeInMillis = 0
        if (lastModifiedHeader != null) {
            timeInMillis = Date.parse(lastModifiedHeader)
        }
    }
}
