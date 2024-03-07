/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

import it.niedermann.owncloud.notes.widget.AbstractWidgetData;

@Entity(
        foreignKeys = {
                @ForeignKey(
                        entity = Account.class,
                        parentColumns = "id",
                        childColumns = "accountId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Note.class,
                        parentColumns = "id",
                        childColumns = "noteId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(name = "IDX_SINGLENOTEWIDGETDATA_ACCOUNTID", value = "accountId"),
                @Index(name = "IDX_SINGLENOTEWIDGETDATA_NOTEID", value = "noteId"),
        }
)
public class SingleNoteWidgetData extends AbstractWidgetData {
    private long noteId;

    public SingleNoteWidgetData() {
        // Default constructor
    }

    @Ignore
    public SingleNoteWidgetData(int id, long accountId, long noteId, int modeId) {
        super(id, accountId, modeId);
        setNoteId(noteId);
    }

    public long getNoteId() {
        return noteId;
    }

    public void setNoteId(long noteId) {
        this.noteId = noteId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SingleNoteWidgetData that)) return false;

        return noteId == that.noteId;
    }

    @Override
    public int hashCode() {
        return (int) (noteId ^ (noteId >>> 32));
    }
}