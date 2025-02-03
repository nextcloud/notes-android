package it.niedermann.owncloud.notes.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.niedermann.owncloud.notes.persistence.entity.ShareEntity

@Dao
interface ShareDao {

    @Insert
    fun addShareEntity(entity: ShareEntity)

    @Query("SELECT * FROM share_table WHERE noteRemoteId = :noteRemoteId AND userName = :userName")
    fun getShareEntities(noteRemoteId: Long, userName: String): List<ShareEntity>
}
