package it.niedermann.owncloud.notes.persistence.entity;

public class NoteIdPair {

    private long id;
    private Long remoteId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(Long remoteId) {
        this.remoteId = remoteId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NoteIdPair)) return false;

        NoteIdPair that = (NoteIdPair) o;

        if (id != that.id) return false;
        return remoteId != null ? remoteId.equals(that.remoteId) : that.remoteId == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (remoteId != null ? remoteId.hashCode() : 0);
        return result;
    }
}
