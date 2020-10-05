package it.niedermann.owncloud.notes.persistence.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import it.niedermann.owncloud.notes.persistence.entity.NoteEntity;
import it.niedermann.owncloud.notes.shared.model.DBStatus;

@Dao
public interface NoteDao {

    @Query("DELETE FROM noteentity WHERE id = :id and status= :forceDBStatus")
    void deleteByCardId(long id, DBStatus forceDBStatus);

    @Query("UPDATE noteentity SET scrollY = :scrollY WHERE id = :id")
    void updateScrollY(long id, int scrollY);

    @Insert
    long addNote(NoteEntity noteEntity);

    @Query("UPDATE noteentity SET status = :status WHERE id = :id")
    void updateStatus(long id, DBStatus status);
}
