package it.niedermann.owncloud.notes.shared.model;

import androidx.annotation.Nullable;

public class NotesSettings {

    @Nullable
    private String notesPath;
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
