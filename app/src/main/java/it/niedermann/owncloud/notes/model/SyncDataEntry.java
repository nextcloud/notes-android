package it.niedermann.owncloud.notes.model;


/**
 * This bean class is a reduced version from {@link DBNote}.
 * It contains only those attributes that are required for preparing the synchronization.
 */
public class SyncDataEntry {
    private long id;
    private long remoteId;
    private String etag;

    public SyncDataEntry(long id, long remoteId, String etag) {
        this.id = id;
        this.remoteId = remoteId;
        this.etag = etag;
    }

    public long getId() {
        return id;
    }

    public long getRemoteId() {
        return remoteId;
    }

    public String getEtag() {
        return etag;
    }
}
