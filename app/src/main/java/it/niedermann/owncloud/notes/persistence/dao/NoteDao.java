package it.niedermann.owncloud.notes.persistence.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import java.util.Set;

import it.niedermann.owncloud.notes.persistence.NoteServerSyncHelper;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.CategorySortingMethod;
import it.niedermann.owncloud.notes.shared.model.DBStatus;

@Dao
public interface NoteDao {

    @Insert
    long addNote(Note note);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int updateNote(Note newNote);

    @Query("DELETE FROM NOTE WHERE accountId = :accountId")
    int deleteByAccountId(Long accountId);

    /**
     * Returns a list of all Notes in the Database
     *
     * @return List&lt;Note&gt;
     */
    @Query("SELECT * FROM NOTE WHERE accountId = :accountId AND status != 'LOCAL_DELETED' ORDER BY favorite DESC, modified DESC")
    List<Note> getNotes(long accountId);

    @Query("DELETE FROM NOTE WHERE id = :id and status = :forceDBStatus")
    void deleteByCardId(long id, DBStatus forceDBStatus);

    @Query("UPDATE NOTE SET scrollY = :scrollY WHERE id = :id")
    void updateScrollY(long id, int scrollY);

    @Query("SELECT * FROM NOTE WHERE id = :id AND accountId = :accountId AND status != :accountId")
    Note getNote(long accountId, long id);

    @Query("UPDATE NOTE SET status = :status WHERE id = :id")
    void updateStatus(long id, DBStatus status);

    @Query("UPDATE NOTE SET categoryId = :categoryId WHERE id = :id")
    void updateCategory(long id, long categoryId);

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

    @Query("SELECT COUNT(*) FROM NOTE WHERE status != 'LOCAL_DELETED' AND accountId = :accountId AND favorite = 0")
    Integer getNonFavoritesCount(long accountId);

    /**
     * Returns a list of all Notes in the Database which were modified locally
     *
     * @return {@link List< Note >}
     */
    @Query("SELECT * FROM NOTE WHERE status != '' AND accountId = :accountId")
    List<Note> getLocalModifiedNotes(long accountId);

    @Query("SELECT * FROM NOTE WHERE status != 'LOCAL_DELETED' AND accountId = :accountId ORDER BY modified DESC LIMIT 4")
    List<Note> getRecentNotes(long accountId);

    @Query("UPDATE NOTE SET status = 'LOCAL_EDITED', favorite = ((favorite | 1) - (favorite & 1)) WHERE id = :id")
    void toggleFavorite(long id);

    @Query("SELECT * FROM NOTE WHERE accountId = :accountId AND status != 'LOCAL_DELETED' AND (title LIKE '%' + :query + '%' OR content LIKE '%' + :query + '%' OR categoryId LIKE '%' + :query + '%') AND (categoryId = :category OR title LIKE :category + '/%') AND favorite = :favorite ORDER BY favorite DESC, :sortingMethod")
    List<Note> searchNotes(long accountId, String query, String category, Boolean favorite, CategorySortingMethod sortingMethod);

    /**
     * Needed for subcategories, see https://github.com/stefan-niedermann/nextcloud-notes/issues/902
     */
    @Query("SELECT * FROM NOTE WHERE accountId = :accountId AND status != 'LOCAL_DELETED' AND (title LIKE '%' + :query + '%' OR content LIKE '%' + :query + '%' OR categoryId LIKE '%' + :query + '%') AND (categoryId = :category OR title LIKE :category + '/%') AND favorite = :favorite ORDER BY categoryId, favorite DESC, :sortingMethod")
    List<Note> searchNotesSubcategory(long accountId, String query, String category, Boolean favorite, CategorySortingMethod sortingMethod);

    @Query("UPDATE NOTE SET remoteId = :remoteId WHERE id = :id")
    void updateRemoteId(long id, long remoteId);

    /**
     * used by: {@link NoteServerSyncHelper.SyncTask#pushLocalChanges()} update only, if not modified locally during the synchronization
     * (i.e. all (!) user changeable columns (content, favorite, category) must still have the same value), uses reference value gathered at start of synchronization
     */
    @Query("UPDATE NOTE SET id = :id, title = :title, modified = :modified, title = :title, favorite = :favorite, etag = :eTag, content = :content " +
                    "WHERE id = :id AND content = :content AND favorite = :favorite AND categoryId = :categoryTitle")
    void updateIfModifiedLocallyDuringSync(long id, long modified, String title, Boolean favorite, String categoryTitle, String eTag, String content);


    /**
     * used by: {@link NoteServerSyncHelper.SyncTask#pullRemoteChanges()} update only, if not modified locally (i.e. STATUS="") and if modified remotely (i.e. any (!) column has changed)
     */
    @Query("UPDATE NOTE SET id = :id, title = :title, modified = :modified, title = :title, favorite = :favorite, etag = :eTag, content = :content " +
                    "WHERE id = :id AND status = '' AND (modified != :modified OR favorite != :favorite OR categoryId != :categoryTitle OR (eTag == NULL OR eTag != :eTag) OR content != :content)")
    void updateIfNotModifiedLocallyAndRemoteColumnHasChanged(long id, long modified, String title, Boolean favorite, String categoryTitle, String eTag, String content);
}
