package it.niedermann.owncloud.notes.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.niedermann.owncloud.notes.shared.model.Capabilities

@Dao
interface CapabilitiesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(capabilities: Capabilities)

    @Query("SELECT * FROM capabilities WHERE id = 1")
    fun getCapabilities(): Capabilities
}
