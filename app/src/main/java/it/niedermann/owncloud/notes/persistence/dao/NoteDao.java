/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import java.util.Set;

import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.CategoryWithNotesCount;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.DBStatus;

/**
 * Each method starting with <code>search</code> will return only a partial {@link Note} without any
 * {@link Note#eTag}, {@link Note#status}, {@link Note#content} or {@link Note#scrollY} for performance reasons.
 */
@SuppressWarnings("JavadocReference")
@Dao
public interface NoteDao {

    @Insert
    long addNote(Note note);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int updateNote(Note newNote);

    String getNoteById = "SELECT * FROM NOTE WHERE id = :id";
    String count = "SELECT COUNT(*) FROM NOTE WHERE status != 'LOCAL_DELETED' AND accountId = :accountId";
    String countFavorites = "SELECT COUNT(*) FROM NOTE WHERE status != 'LOCAL_DELETED' AND accountId = :accountId AND favorite = 1";
    String searchRecentByModified = "SELECT id, remoteId, accountId, title, favorite, excerpt, modified, category, status, '' as eTag, '' as content, 0 as scrollY FROM NOTE WHERE accountId = :accountId AND status != 'LOCAL_DELETED' AND (title LIKE :query OR content LIKE :query) ORDER BY favorite DESC, modified DESC";
    String searchRecentLexicographically = "SELECT id, remoteId, accountId, title, favorite, excerpt, modified, category, status, '' as eTag, '' as content, 0 as scrollY FROM NOTE WHERE accountId = :accountId AND status != 'LOCAL_DELETED' AND (title LIKE :query OR content LIKE :query) ORDER BY favorite DESC, title COLLATE LOCALIZED ASC";
    String searchFavoritesByModified = "SELECT id, remoteId, accountId, title, favorite, excerpt, modified, category, status, '' as eTag, '' as content, 0 as scrollY FROM NOTE WHERE accountId = :accountId AND status != 'LOCAL_DELETED' AND (title LIKE :query OR content LIKE :query) AND favorite = 1 ORDER BY modified DESC";
    String searchFavoritesLexicographically = "SELECT id, remoteId, accountId, title, favorite, excerpt, modified, category, status, '' as eTag, '' as content, 0 as scrollY FROM NOTE WHERE accountId = :accountId AND status != 'LOCAL_DELETED' AND (title LIKE :query OR content LIKE :query) AND favorite = 1 ORDER BY title COLLATE LOCALIZED ASC";
    String searchUncategorizedByModified = "SELECT id, remoteId, accountId, title, favorite, excerpt, modified, category, status, '' as eTag, '' as content, 0 as scrollY FROM NOTE WHERE accountId = :accountId AND status != 'LOCAL_DELETED' AND (title LIKE :query OR content LIKE :query) AND category = '' ORDER BY favorite DESC, modified DESC";
    String searchUncategorizedLexicographically = "SELECT id, remoteId, accountId, title, favorite, excerpt, modified, category, status, '' as eTag, '' as content, 0 as scrollY FROM NOTE WHERE accountId = :accountId AND status != 'LOCAL_DELETED' AND (title LIKE :query OR content LIKE :query) AND category = '' ORDER BY favorite DESC, title COLLATE LOCALIZED ASC";
    String searchCategoryByModified = "SELECT id, remoteId, accountId, title, favorite, excerpt, modified, category, status, '' as eTag, '' as content, 0 as scrollY FROM NOTE WHERE accountId = :accountId AND status != 'LOCAL_DELETED' AND (title LIKE :query OR content LIKE :query) AND (category = :category OR category LIKE :category || '/%') ORDER BY category, favorite DESC, modified DESC";
    String searchCategoryLexicographically = "SELECT id, remoteId, accountId, title, favorite, excerpt, modified, category, status, '' as eTag, '' as content, 0 as scrollY FROM NOTE WHERE accountId = :accountId AND status != 'LOCAL_DELETED' AND (title LIKE :query OR content LIKE :query) AND (category = :category OR category LIKE :category || '/%') ORDER BY category, favorite DESC, title COLLATE LOCALIZED ASC";

    @Query(getNoteById)
    LiveData<Note> getNoteById$(long id);

    @Query(getNoteById)
    Note getNoteById(long id);

    @Query("SELECT remoteId FROM NOTE WHERE id = :id")
    Long getRemoteId(long id);

    @Query(count)
    LiveData<Integer> count$(long accountId);

    @Query(count)
    Integer count(long accountId);

    @Query(countFavorites)
    LiveData<Integer> countFavorites$(long accountId);

    @Query(countFavorites)
    Integer countFavorites(long accountId);

    @Query(searchRecentByModified)
    LiveData<List<Note>> searchRecentByModified$(long accountId, String query);

    @Query(searchRecentByModified)
    List<Note> searchRecentByModified(long accountId, String query);

    @Query(searchRecentLexicographically)
    LiveData<List<Note>> searchRecentLexicographically$(long accountId, String query);

    @Query(searchRecentLexicographically)
    List<Note> searchRecentLexicographically(long accountId, String query);

    @Query(searchFavoritesByModified)
    LiveData<List<Note>> searchFavoritesByModified$(long accountId, String query);

    @Query(searchFavoritesByModified)
    List<Note> searchFavoritesByModified(long accountId, String query);

    @Query(searchFavoritesLexicographically)
    LiveData<List<Note>> searchFavoritesLexicographically$(long accountId, String query);

    @Query(searchFavoritesLexicographically)
    List<Note> searchFavoritesLexicographically(long accountId, String query);

    @Query(searchUncategorizedByModified)
    LiveData<List<Note>> searchUncategorizedByModified$(long accountId, String query);

    @Query(searchUncategorizedByModified)
    List<Note> searchUncategorizedByModified(long accountId, String query);

    @Query(searchUncategorizedLexicographically)
    LiveData<List<Note>> searchUncategorizedLexicographically$(long accountId, String query);

    @Query(searchUncategorizedLexicographically)
    List<Note> searchUncategorizedLexicographically(long accountId, String query);

    @Query(searchCategoryByModified)
    LiveData<List<Note>> searchCategoryByModified$(long accountId, String query, String category);

    @Query(searchCategoryByModified)
    List<Note> searchCategoryByModified(long accountId, String query, String category);

    @Query(searchCategoryLexicographically)
    LiveData<List<Note>> searchCategoryLexicographically$(long accountId, String query, String category);

    @Query(searchCategoryLexicographically)
    List<Note> searchCategoryLexicographically(long accountId, String query, String category);

    @Query("DELETE FROM NOTE WHERE id = :id AND status = :forceDBStatus")
    void deleteByNoteId(long id, DBStatus forceDBStatus);

    @Query("UPDATE NOTE SET scrollY = :scrollY WHERE id = :id")
    void updateScrollY(long id, int scrollY);

    @Query("UPDATE NOTE SET status = :status WHERE id = :id")
    void updateStatus(long id, DBStatus status);

    @Query("UPDATE NOTE SET category = :category WHERE id = :id")
    void updateCategory(long id, String category);

    /**
     * Gets all the {@link Note#remoteId}s of all not deleted {@link Note}s of an {@link Account}
     *
     * @param accountId get the {@link Note#remoteId} from all {@link Note}s of this {@link Account}
     * @return {@link Set<String>} {@link Note#remoteId}s from all {@link Note}s
     */
    @Query("SELECT DISTINCT remoteId FROM NOTE WHERE accountId = :accountId AND status != 'LOCAL_DELETED'")
    List<Long> getRemoteIds(long accountId);

    /**
     * Gets a list of {@link Note} objects with filled {@link Note#id} and {@link Note#remoteId},
     * where {@link Note#remoteId} is not <code>null</code>
     */
    @Query("SELECT id, remoteId, 0 as accountId, '' as title, 0 as favorite, '' as excerpt, 0 as modified, '' as eTag, 0 as status, '' as category, '' as content, 0 as scrollY  FROM NOTE WHERE accountId = :accountId AND status != 'LOCAL_DELETED' AND remoteId IS NOT NULL")
    List<Note> getRemoteIdAndId(long accountId);

    /**
     * Get a single {@link Note} by {@link Note#remoteId} (aka. Nextcloud file id)
     *
     * @param remoteId int - {@link Note#remoteId} of the requested {@link Note}
     * @return {@link Note#id}
     */
    @Query("SELECT id FROM NOTE WHERE accountId = :accountId AND remoteId = :remoteId AND status != 'LOCAL_DELETED'")
    Long getLocalIdByRemoteId(long accountId, long remoteId);

    /**
     * Returns a list of all {@link Note}s in the Database which were modified locally
     *
     * @return {@link List<Note>}
     */
    @Query("SELECT * FROM NOTE WHERE status != '' AND accountId = :accountId")
    List<Note> getLocalModifiedNotes(long accountId);

    @Query("SELECT * FROM NOTE WHERE status != 'LOCAL_DELETED' AND accountId = :accountId ORDER BY modified DESC LIMIT 4")
    List<Note> getRecentNotes(long accountId);

    @Query("UPDATE NOTE SET status = 'LOCAL_EDITED', favorite = ((favorite | 1) - (favorite & 1)) WHERE id = :id")
    void toggleFavorite(long id);

    @Query("UPDATE NOTE SET remoteId = :remoteId WHERE id = :id")
    void updateRemoteId(long id, Long remoteId);

    /**
     * used by: {@link it.niedermann.owncloud.notes.persistence.NotesServerSyncTask#pushLocalChanges()} update only, if not modified locally during the synchronization
     * (i.e. all (!) user changeable columns (content, favorite, category) must still have the same value), uses reference value gathered at start of synchronization
     */
    @Query("UPDATE NOTE SET title = :targetTitle, modified = :targetModified, favorite = :targetFavorite, etag = :targetETag, content = :targetContent, status = '', excerpt = :targetExcerpt " +
            "WHERE id = :noteId AND content = :contentBeforeSyncStart AND favorite = :favoriteBeforeSyncStart AND category = :categoryBeforeSyncStart")
    int updateIfNotModifiedLocallyDuringSync(long noteId, Long targetModified, String targetTitle, boolean targetFavorite, String targetETag, String targetContent, String targetExcerpt, String contentBeforeSyncStart, String categoryBeforeSyncStart, boolean favoriteBeforeSyncStart);

    /**
     * used by: {@link it.niedermann.owncloud.notes.persistence.NotesServerSyncTask#pullRemoteChanges()} update only, if not modified locally (i.e. STATUS="") and if modified remotely (i.e. any (!) column has changed)
     */
    @Query("UPDATE NOTE SET title = :title, modified = :modified, favorite = :favorite, etag = :eTag, content = :content, status = '', excerpt = :excerpt, category = :category " +
            "WHERE id = :id AND status = '' AND (title != :title OR modified != :modified OR favorite != :favorite OR category != :category OR (eTag IS NULL OR eTag != :eTag) OR content != :content)")
    int updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(long id, Long modified, String title, boolean favorite, String category, String eTag, String content, String excerpt);

    /**
     * This method return all of the categories with given {@param accountId}
     *
     * @param accountId The user account Id
     * @return All of the categories with given accountId
     */
    @Query("SELECT accountId, category, COUNT(*) as 'totalNotes' FROM NOTE WHERE STATUS != 'LOCAL_DELETED' AND accountId = :accountId GROUP BY category")
    LiveData<List<CategoryWithNotesCount>> getCategories$(Long accountId);

    @Query("SELECT accountId, category, COUNT(*) as 'totalNotes' FROM NOTE WHERE STATUS != 'LOCAL_DELETED' AND accountId = :accountId AND category != '' AND category LIKE :searchTerm GROUP BY category")
    LiveData<List<CategoryWithNotesCount>> searchCategories$(Long accountId, String searchTerm);

    @Query("SELECT COUNT(*) FROM NOTE WHERE STATUS != '' AND accountId = :accountId")
    Long countUnsynchronizedNotes(long accountId);
}
