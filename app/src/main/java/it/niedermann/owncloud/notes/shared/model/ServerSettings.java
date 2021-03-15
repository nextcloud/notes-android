package it.niedermann.owncloud.notes.shared.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import static it.niedermann.owncloud.notes.persistence.NotesClient.JSON_SETTINGS_FILE_SUFFIX;
import static it.niedermann.owncloud.notes.persistence.NotesClient.JSON_SETTINGS_NOTES_PATH;

public class ServerSettings implements Serializable {
    private String notesPath = "";
    private String fileSuffix = "";

    public ServerSettings(String notesPath, String fileSuffix) {
        setNotesPath(notesPath);
        setFileSuffix(fileSuffix);
    }

    public static ServerSettings from(JSONObject settings) throws JSONException {
        String notesPath = "";
        if (settings.has(JSON_SETTINGS_NOTES_PATH)) {
            notesPath = settings.getString(JSON_SETTINGS_NOTES_PATH);
        }
        String fileSuffix = "";
        if (settings.has(JSON_SETTINGS_FILE_SUFFIX)) {
            fileSuffix = settings.getString(JSON_SETTINGS_FILE_SUFFIX);
        }
        return new ServerSettings(notesPath, fileSuffix);
    }

    public String getNotesPath() {
        return notesPath;
    }

    public void setNotesPath(String notesPath) {
        this.notesPath = notesPath;
    }

    public String getFileSuffix() {
        return fileSuffix;
    }

    public void setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }
}