package it.niedermann.owncloud.notes.shared.model;

public class SyncResultStatus {
    public boolean pullSuccessful = true;
    public boolean pushSuccessful = true;

    public static final SyncResultStatus FAILED = new SyncResultStatus();

    static {
        FAILED.pullSuccessful = false;
        FAILED.pushSuccessful = false;
    }
}
