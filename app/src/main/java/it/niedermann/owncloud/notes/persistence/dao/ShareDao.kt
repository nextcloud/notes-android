package it.niedermann.owncloud.notes.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.niedermann.owncloud.notes.persistence.entity.ShareEntity

@Dao
interface ShareDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addShareEntities(entities: List<ShareEntity>)

    @Query("SELECT * FROM share_table WHERE path = :path")
    fun getShareEntities(path: String): List<ShareEntity>
}
