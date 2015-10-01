package it.niedermann.owncloud.notes.model;

/**
 * Helps to distinguish between different local change types for Server Synchronization.
 * Created by stefan on 19.09.15.
 */
public enum DBStatus {

    VOID(""), LOCAL_CREATED("LOCAL_CREATED"), LOCAL_EDITED("LOCAL_EDITED"), LOCAL_DELETED("LOCAL_DELETED");

    private final String title;

    public String getTitle() {
        return title;
    }

    DBStatus(String title) {
        this.title = title;
    }
}
