/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData;

@Dao
public interface WidgetNotesListDao {

    @Insert
    void createOrUpdateNoteListWidgetData(NotesListWidgetData data);

    @Query("DELETE FROM NOTESLISTWIDGETDATA WHERE id = :appWidgetId")
    void removeNoteListWidget(int appWidgetId);

    @Query("SELECT * FROM NOTESLISTWIDGETDATA WHERE id = :appWidgetId")
    NotesListWidgetData getNoteListWidgetData(int appWidgetId);
}
