package it.niedermann.owncloud.notes.model;

/**
 * Helps to distinguish between different local change types for Server Synchronization.
 * Created by stefan on 19.09.15.
 */
public enum DBStatus {

    /**
     * VOID means, that the Note was not modified locally
     */
    VOID(""),

    /**
     * LOCAL_CREATED is not used anymore, since a newly created note has REMOTE_ID=0
     */
    @Deprecated
    LOCAL_CREATED("LOCAL_CREATED"),

    /**
     * LOCAL_EDITED means that a Note was created and/or changed since the last successful synchronization.
     * If it was newly created, then REMOTE_ID is 0
     */
    LOCAL_EDITED("LOCAL_EDITED"),

    /**
     * LOCAL_DELETED means that the Note was deleted locally, but this information was not yet synchronized.
     * Therefore, the Note have to be kept locally until the synchronization has succeeded.
     * However, Notes with this status should not be displayed in the UI.
     */
    LOCAL_DELETED("LOCAL_DELETED");

    private final String title;

    public String getTitle() {
        return title;
    }

    DBStatus(String title) {
        this.title = title;
    }

    /**
     * Parse a String an get the appropriate DBStatus enum element.
     *
     * @param str The String containing the DBStatus identifier. Must not null.
     * @return The DBStatus fitting to the String.
     */
    public static DBStatus parse(String str) {
        if (str.isEmpty()) {
            return DBStatus.VOID;
        } else {
            return DBStatus.valueOf(str);
        }
    }
}
