package it.niedermann.owncloud.notes.persistence.dao;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;
import java.util.Map;
import java.util.Set;

import it.niedermann.owncloud.notes.persistence.entity.NoteEntity;
import it.niedermann.owncloud.notes.shared.model.DBNote;
import it.niedermann.owncloud.notes.shared.model.DBStatus;

@Dao
public interface NoteDao {

    @Insert
    long addNote(NoteEntity noteEntity);

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
    @Query("SELECT DISTINCT remoteId FROM NoteEntity WHERE accountId = :accountId AND status != \"LOCAL_DELETED\"")
    List<Long> getRemoteIds(long accountId);

    @Query("SELECT * FROM NoteEntity WHERE accountId = :accountId AND status != \"LOCAL_DELETED\"")
    List<NoteEntity> getRemoteIdAndId(long accountId);

    /**
     * Get a single Note by remote Id (aka. nextcloud file id)
     *
     * @param remoteId int - remote ID of the requested Note
     * @return {@link DBNote#getId()}
     */
    @Query("SELECT id FROM NoteEntity WHERE accountId = :accountId AND remoteId = :remoteId AND status != \"LOCAL_DELETED\"")
    Long getLocalIdByRemoteId(long accountId, long remoteId);

    @Query("SELECT favorite, COUNT(*) FROM NoteEntity WHERE status != \"LOCAL_DELETED\" AND accountId = :accountId GROUP BY favorite ORDER BY favorite")
    Map<String, Integer> getFavoritesCount(long accountId);

    /**
     * Returns a list of all Notes in the Database which were modified locally
     *
     * @return {@link List<DBNote>}
     */
    @Query("SELECT * FROM NoteEntity WHERE status != \"VOID\" AND accountId = :accountId")
    List<NoteEntity> getLocalModifiedNotes(long accountId);

    @Query("SELECT * FROM NoteEntity WHERE status != \"LOCAL_DELETED\" AND accountId = :accountId ORDER BY modified DESC LIMIT 4")
    List<NoteEntity> getRecentNotes(long accountId);

    @Query("UPDATE NoteEntity SET status = \"LOCAL_EDITED\", favorite = ((favorite | 1) - (favorite & 1)) WHERE id = :id")
    void toggleFavorite(long id);
}
