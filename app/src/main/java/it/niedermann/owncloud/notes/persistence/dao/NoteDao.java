package it.niedermann.owncloud.notes.persistence.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import java.util.Set;

import it.niedermann.owncloud.notes.persistence.NoteServerSyncHelper;
import it.niedermann.owncloud.notes.persistence.entity.CategoryWithNotesCount;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.DBStatus;

@Dao
public interface NoteDao {

    @Insert
    long addNote(Note note);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int updateNote(Note newNote);

    @Query("DELETE FROM NOTE WHERE accountId = :accountId")
    int deleteByAccountId(Long accountId);

    @Query("SELECT * FROM NOTE WHERE accountId = :accountId AND status != 'LOCAL_DELETED' AND (title LIKE :query OR content LIKE :query) ORDER BY favorite DESC, modified DESC")
    LiveData<List<Note>> searchRecentByModified(long accountId, String query);

    @Query("SELECT * FROM NOTE WHERE accountId = :accountId AND status != 'LOCAL_DELETED' AND (title LIKE :query OR content LIKE :query) ORDER BY favorite DESC, title COLLATE NOCASE ASC")
    LiveData<List<Note>> searchRecentLexicographically(long accountId, String query);

    @Query("SELECT * FROM NOTE WHERE accountId = :accountId AND status != 'LOCAL_DELETED' AND (title LIKE :query OR content LIKE :query) AND favorite = 1 ORDER BY modified DESC")
    LiveData<List<Note>> searchFavoritesByModified(long accountId, String query);

    @Query("SELECT * FROM NOTE WHERE accountId = :accountId AND status != 'LOCAL_DELETED' AND (title LIKE :query OR content LIKE :query) AND favorite = 1 ORDER BY title COLLATE NOCASE ASC")
    LiveData<List<Note>> searchFavoritesLexicographically(long accountId, String query);

    @Query("SELECT * FROM NOTE WHERE accountId = :accountId AND status != 'LOCAL_DELETED' AND (title LIKE :query OR content LIKE :query) AND category = '' ORDER BY favorite DESC, modified DESC")
    LiveData<List<Note>> searchUncategorizedByModified(long accountId, String query);

    @Query("SELECT * FROM NOTE WHERE accountId = :accountId AND status != 'LOCAL_DELETED' AND (title LIKE :query OR content LIKE :query) AND category = '' ORDER BY favorite DESC, title COLLATE NOCASE ASC")
    LiveData<List<Note>> searchUncategorizedLexicographically(long accountId, String query);

    @Query("SELECT * FROM NOTE WHERE accountId = :accountId AND status != 'LOCAL_DELETED' AND (title LIKE :query OR content LIKE :query) AND (category = :category OR category LIKE :category || '/%') ORDER BY category, favorite DESC, modified DESC")
    LiveData<List<Note>> searchCategoryByModified(long accountId, String query, String category);

    @Query("SELECT * FROM NOTE WHERE accountId = :accountId AND status != 'LOCAL_DELETED' AND (title LIKE :query OR content LIKE :query) AND (category = :category OR category LIKE :category || '/%') ORDER BY category, favorite DESC, title COLLATE NOCASE ASC")
    LiveData<List<Note>> searchCategoryLexicographically(long accountId, String query, String category);

    @Query("DELETE FROM NOTE WHERE id = :id AND status = :forceDBStatus")
    void deleteByNoteId(long id, DBStatus forceDBStatus);

    @Query("UPDATE NOTE SET scrollY = :scrollY WHERE id = :id")
    void updateScrollY(long id, int scrollY);

    @Query("SELECT * FROM NOTE WHERE id = :id")
    Note getNoteById(long id);

    @Query("UPDATE NOTE SET status = :status WHERE id = :id")
    void updateStatus(long id, DBStatus status);

    @Query("UPDATE NOTE SET category = :category WHERE id = :id")
    void updateCategory(long id, String category);

    /**
     * Gets all the remoteIds of all not deleted notes of an account
     *
     * @param accountId get the remoteIds from all notes of this account
     * @return {@link Set<String>} remoteIds from all notes
     */
    @Query("SELECT DISTINCT remoteId FROM NOTE WHERE accountId = :accountId AND status != 'LOCAL_DELETED'")
    List<Long> getRemoteIds(long accountId);

    @Query("SELECT * FROM NOTE WHERE accountId = :accountId AND status != 'LOCAL_DELETED'")
    List<Note> getRemoteIdAndId(long accountId);

    /**
     * Get a single Note by remote Id (aka. nextcloud file id)
     *
     * @param remoteId int - remote ID of the requested Note
     * @return {@link Note#getId()}
     */
    @Query("SELECT id FROM NOTE WHERE accountId = :accountId AND remoteId = :remoteId AND status != 'LOCAL_DELETED'")
    Long getLocalIdByRemoteId(long accountId, long remoteId);

    @Query("SELECT COUNT(*) FROM NOTE WHERE status != 'LOCAL_DELETED' AND accountId = :accountId AND favorite = 1")
    Integer getFavoritesCount(long accountId);

    @Query("SELECT COUNT(*) FROM NOTE WHERE status != 'LOCAL_DELETED' AND accountId = :accountId")
    Integer count(long accountId);

    /**
     * Returns a list of all Notes in the Database which were modified locally
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
     * used by: {@link NoteServerSyncHelper.SyncTask#pushLocalChanges()} update only, if not modified locally during the synchronization
     * (i.e. all (!) user changeable columns (content, favorite, category) must still have the same value), uses reference value gathered at start of synchronization
     */
    @SuppressWarnings("JavadocReference")
    @Query("UPDATE NOTE SET title = :targetTitle, modified = :targetModified, favorite = :targetFavorite, etag = :targetETag, content = :targetContent, status = '', excerpt = :targetExcerpt " +
            "WHERE id = :noteId AND content = :contentBeforeSyncStart AND favorite = :favoriteBeforeSyncStart AND category = :categoryBeforeSyncStart")
    int updateIfNotModifiedLocallyDuringSync(long noteId, Long targetModified, String targetTitle, boolean targetFavorite, String targetETag, String targetContent, String targetExcerpt, String contentBeforeSyncStart, String categoryBeforeSyncStart, boolean favoriteBeforeSyncStart);

    /**
     * used by: {@link NoteServerSyncHelper.SyncTask#pullRemoteChanges()} update only, if not modified locally (i.e. STATUS="") and if modified remotely (i.e. any (!) column has changed)
     */
    @SuppressWarnings("JavadocReference")
    @Query("UPDATE NOTE SET title = :title, modified = :modified, favorite = :favorite, etag = :eTag, content = :content, status = '', excerpt = :excerpt " +
            "WHERE id = :id AND status = '' AND (title != :title OR modified != (:modified / 1000) OR favorite != :favorite OR category != :category OR (eTag IS NULL OR eTag != :eTag) OR content != :content)")
    int updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(long id, Long modified, String title, boolean favorite, String category, String eTag, String content, String excerpt);

    @Query("SELECT content FROM NOTE WHERE id = :id")
    String getContent(Long id);

    /**
     * This method return all of the categories with given accountIdSELECT * FROM NOTE WHERE id = 1 and status = '' and (content != 'My-Content' or category != '')
     *
     * @param accountId The user account Id
     * @return All of the categories with given accountId
     */
    @Query("SELECT accountId, category, COUNT(*) as 'totalNotes' FROM NOTE WHERE STATUS != 'LOCAL_DELETED' AND accountId = :accountId GROUP BY category")
    LiveData<List<CategoryWithNotesCount>> getCategoriesLiveData(Long accountId);

    @Query("SELECT accountId, category, COUNT(*) as 'totalNotes' FROM NOTE WHERE category != '' AND STATUS != 'LOCAL_DELETED' AND accountId = :accountId AND category LIKE :searchTerm GROUP BY category")
    LiveData<List<CategoryWithNotesCount>> searchCategories(Long accountId, String searchTerm);
}
