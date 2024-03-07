/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import it.niedermann.owncloud.notes.persistence.entity.SingleNoteWidgetData;

@Dao
public interface WidgetSingleNoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void createOrUpdateSingleNoteWidgetData(SingleNoteWidgetData data);

    @Query("DELETE FROM SINGLENOTEWIDGETDATA WHERE id = :id")
    void removeSingleNoteWidget(int id);

    @Query("SELECT * FROM SINGLENOTEWIDGETDATA WHERE id = :id")
    SingleNoteWidgetData getSingleNoteWidgetData(int id);
}
