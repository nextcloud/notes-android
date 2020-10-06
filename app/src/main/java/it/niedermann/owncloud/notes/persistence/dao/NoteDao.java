package it.niedermann.owncloud.notes.persistence.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import java.util.Set;

import it.niedermann.owncloud.notes.persistence.NoteServerSyncHelper;
import it.niedermann.owncloud.notes.persistence.entity.NoteEntity;
import it.niedermann.owncloud.notes.shared.model.CategorySortingMethod;
import it.niedermann.owncloud.notes.shared.model.DBStatus;

@Dao
public interface NoteDao {

    @Insert
    long addNote(NoteEntity noteEntity);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int updateNote(NoteEntity newNote);

    @Query("DELETE FROM noteentity WHERE accountId = :accountId")
    int deleteByAccountId(Long accountId);

    /**
     * Returns a list of all Notes in the Database
     *
     * @return List&lt;Note&gt;
     */
    @Query("SELECT * FROM NoteEntity WHERE accountId = :accountId AND status != 'LOCAL_DELETED' ORDER BY favorite DESC, modified DESC")
    List<NoteEntity> getNotes(long accountId);

    @Query("DELETE FROM NoteEntity WHERE id = :id and status = :forceDBStatus")
    void deleteByCardId(long id, DBStatus forceDBStatus);

    @Query("UPDATE NoteEntity SET scrollY = :scrollY WHERE id = :id")
    void updateScrollY(long id, int scrollY);

    @Query("SELECT * FROM NoteEntity WHERE id = :id AND accountId = :accountId AND status != :accountId")
    NoteEntity getNote(long accountId, long id);

    @Query("UPDATE NoteEntity SET status = :status WHERE id = :id")
    void updateStatus(long id, DBStatus status);

    @Query("UPDATE NoteEntity SET category_id = :categoryId WHERE id = :id")
    void updateCategory(long id, long categoryId);

    /**
     * Gets all the remoteIds of all not deleted notes of an account
     *
     * @param accountId get the remoteIds from all notes of this account
     * @return {@link Set<String>} remoteIds from all notes
     */
    @Query("SELECT DISTINCT remoteId FROM NoteEntity WHERE accountId = :accountId AND status != 'LOCAL_DELETED'")
    List<Long> getRemoteIds(long accountId);

    @Query("SELECT * FROM NoteEntity WHERE accountId = :accountId AND status != 'LOCAL_DELETED'")
    List<NoteEntity> getRemoteIdAndId(long accountId);

    /**
     * Get a single Note by remote Id (aka. nextcloud file id)
     *
     * @param remoteId int - remote ID of the requested Note
     * @return {@link NoteEntity#getId()}
     */
    @Query("SELECT id FROM NoteEntity WHERE accountId = :accountId AND remoteId = :remoteId AND status != 'LOCAL_DELETED'")
    Long getLocalIdByRemoteId(long accountId, long remoteId);

    @Query("SELECT COUNT(*) FROM NoteEntity WHERE status != 'LOCAL_DELETED' AND accountId = :accountId AND favorite = 1")
    Integer getFavoritesCount(long accountId);

    @Query("SELECT COUNT(*) FROM NoteEntity WHERE status != 'LOCAL_DELETED' AND accountId = :accountId AND favorite = 0")
    Integer getNonFavoritesCount(long accountId);

    /**
     * Returns a list of all Notes in the Database which were modified locally
     *
     * @return {@link List<NoteEntity>}
     */
    @Query("SELECT * FROM NoteEntity WHERE status != '' AND accountId = :accountId")
    List<NoteEntity> getLocalModifiedNotes(long accountId);

    @Query("SELECT * FROM NoteEntity WHERE status != 'LOCAL_DELETED' AND accountId = :accountId ORDER BY modified DESC LIMIT 4")
    List<NoteEntity> getRecentNotes(long accountId);

    @Query("UPDATE NoteEntity SET status = 'LOCAL_EDITED', favorite = ((favorite | 1) - (favorite & 1)) WHERE id = :id")
    void toggleFavorite(long id);

    @Query("SELECT * FROM NoteEntity WHERE accountId = :accountId AND status != 'LOCAL_DELETED' AND (title LIKE '%' + :query + '%' OR content LIKE '%' + :query + '%' OR category_title LIKE '%' + :query + '%') AND (category_title = :category OR title LIKE :category + '/%') AND favorite = :favorite ORDER BY favorite DESC, :sortingMethod")
    List<NoteEntity> searchNotes(long accountId, String query, String category, Boolean favorite, CategorySortingMethod sortingMethod);

    /**
     * Needed for subcategories, see https://github.com/stefan-niedermann/nextcloud-notes/issues/902
     */
    @Query("SELECT * FROM NoteEntity WHERE accountId = :accountId AND status != 'LOCAL_DELETED' AND (title LIKE '%' + :query + '%' OR content LIKE '%' + :query + '%' OR category_title LIKE '%' + :query + '%') AND (category_title = :category OR title LIKE :category + '/%') AND favorite = :favorite ORDER BY category_title, favorite DESC, :sortingMethod")
    List<NoteEntity> searchNotesSubcategory(long accountId, String query, String category, Boolean favorite, CategorySortingMethod sortingMethod);

    @Query("UPDATE NoteEntity SET remoteId = :remoteId WHERE id = :id")
    void updateRemoteId(long id, long remoteId);

    /**
     * used by: {@link NoteServerSyncHelper.SyncTask#pushLocalChanges()} update only, if not modified locally during the synchronization
     * (i.e. all (!) user changeable columns (content, favorite, category) must still have the same value), uses reference value gathered at start of synchronization
     */
    @Query("UPDATE NoteEntity SET id = :id, title = :title, modified = :modified, title = :title, favorite = :favorite, etag = :eTag, content = :content " +
                    "WHERE id = :id AND content = :content AND favorite = :favorite AND category_title = :categoryTitle")
    void updateIfModifiedLocallyDuringSync(long id, long modified, String title, Boolean favorite, String categoryTitle, String eTag, String content);


    /**
     * used by: {@link NoteServerSyncHelper.SyncTask#pullRemoteChanges()} update only, if not modified locally (i.e. STATUS="") and if modified remotely (i.e. any (!) column has changed)
     */
    @Query("UPDATE NoteEntity SET id = :id, title = :title, modified = :modified, title = :title, favorite = :favorite, etag = :eTag, content = :content " +
                    "WHERE id = :id AND status = '' AND (modified != :modified OR favorite != :favorite OR category_title != :categoryTitle OR (eTag == NULL OR eTag != :eTag) OR content != :content)")
    void updateIfNotModifiedLocallyAndRemoteColumnHasChanged(long id, long modified, String title, Boolean favorite, String categoryTitle, String eTag, String content);
}
