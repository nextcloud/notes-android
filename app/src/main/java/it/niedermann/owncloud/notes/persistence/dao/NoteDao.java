package it.niedermann.owncloud.notes.persistence.dao;

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

    /**
     * Gets all the remoteIds of all not deleted notes of an account
     *
     * @param accountId get the remoteIds from all notes of this account
     * @return {@link Set<String>} remoteIds from all notes
     */
    @Query("SELECT DISTINCT remoteId FROM NoteEntity WHERE accountId = :accountId AND status != \"LOCAL_DELETED\"")
    List<Long> getRemoteIds(long accountId);


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
}
