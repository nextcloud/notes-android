/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;

public class NotesSettings {

    @Expose
    @Nullable
    private String notesPath;
    @Expose
    @Nullable
    private String fileSuffix;

    public NotesSettings(@Nullable String notesPath, @Nullable String fileSuffix) {
        this.notesPath = notesPath;
        this.fileSuffix = fileSuffix;
    }

    @Nullable
    public String getNotesPath() {
        return notesPath;
    }

    public void setNotesPath(@Nullable String notesPath) {
        this.notesPath = notesPath;
    }

    @Nullable
    public String getFileSuffix() {
        return fileSuffix;
    }

    public void setFileSuffix(@Nullable String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }
}
