package it.niedermann.owncloud.notes.persistence.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.niedermann.owncloud.notes.persistence.entity.NoteEntity;
import it.niedermann.owncloud.notes.shared.model.DBNote;
import it.niedermann.owncloud.notes.shared.model.DBStatus;

@Dao
public interface NoteDao {

    @Query("DELETE FROM noteentity WHERE id = :id and status= :forceDBStatus")
    void deleteByCardId(long id, DBStatus forceDBStatus);

    @Query("UPDATE noteentity SET scrollY = :scrollY WHERE id = :id")
    void updateScrollY(long id, int scrollY);

    @Query("SELECT * FROM noteentity WHERE id = :id AND accountId = :accountId AND status != :")
    NoteEntity getNote(long accountId, long id);

    @Insert
    long addNote(NoteEntity noteEntity);

    @Query("UPDATE noteentity SET status = :status WHERE id = :id")
    void updateStatus(long id, DBStatus status);

    /**
     * Gets all the remoteIds of all not deleted notes of an account
     *
     * @param accountId get the remoteIds from all notes of this account
     * @return {@link Set<String>} remoteIds from all notes
     */
    @Query("SELECT remoteId FROM noteentity WHERE accountId = :accountId AND status != \"LOCAL_DELETED\"")
    Set<String> getRemoteIds(long accountId);


    /**
     * Get a single Note by remote Id (aka. nextcloud file id)
     *
     * @param remoteId int - remote ID of the requested Note
     * @return {@link DBNote#getId()}
     */
    @Query("SELECT id FROM noteentity WHERE accountId = :accountId AND remoteId = :remoteId AND status != \"LOCAL_DELETED\"")
    Long getLocalIdByRemoteId(long accountId, long remoteId);

    @Query("SELECT favorite, COUNT(*) FROM noteentity WHERE status != \"LOCAL_DELETED\" AND accountId = :accountId GROUP BY favorite ORDER BY favorite")
    Map<String, Integer> getFavoritesCount(long accountId);
}
