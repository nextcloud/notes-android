/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.entity;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

import java.util.Objects;

import it.niedermann.owncloud.notes.widget.AbstractWidgetData;

@Entity(
        foreignKeys = {
                @ForeignKey(
                        entity = Account.class,
                        parentColumns = "id",
                        childColumns = "accountId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(name = "IDX_NOTESLISTWIDGETDATA_ACCOUNTID", value = "accountId"),
                @Index(name = "IDX_NOTESLISTWIDGETDATA_CATEGORY", value = "category"),
                @Index(name = "IDX_NOTESLISTWIDGETDATA_ACCOUNT_CATEGORY", value = {"accountId", "category"})
        }
)
public class NotesListWidgetData extends AbstractWidgetData {

    @Ignore
    public static final int MODE_DISPLAY_ALL = 0;
    @Ignore
    public static final int MODE_DISPLAY_STARRED = 1;
    @Ignore
    public static final int MODE_DISPLAY_CATEGORY = 2;

    @IntRange(from = 0, to = 2)
    private int mode;

    @Nullable
    private String category;

    @Nullable
    public String getCategory() {
        return category;
    }

    public void setCategory(@Nullable String category) {
        this.category = category;
    }

    public void setMode(@IntRange(from = 0, to = 2) int mode) {
        this.mode = mode;
    }

    @IntRange(from = 0, to = 2)
    public int getMode() {
        return mode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotesListWidgetData that)) return false;
        if (!super.equals(o)) return false;

        if (mode != that.mode) return false;
        return Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + mode;
        result = 31 * result + (category != null ? category.hashCode() : 0);
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "NotesListWidgetData{" +
                "mode=" + mode +
                ", category='" + category + '\'' +
                '}';
    }
}