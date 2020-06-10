package it.niedermann.owncloud.notes.persistence.dao;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import it.niedermann.owncloud.notes.persistence.entity.LocalAccountEntity;

@Dao
public interface LocalAccountDao {


    @Query("SELECT * FROM localaccountentity")
    List<LocalAccountEntity> getAccounts();
}
