package it.niedermann.owncloud.notes.persistence.entity;

public class CategoryWithNotesCount {

    private long accountId;
    private String category;
    private Integer totalNotes;

    public Integer getTotalNotes() {
        return totalNotes;
    }

    public void setTotalNotes(Integer totalNotes) {
        this.totalNotes = totalNotes;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryWithNotesCount)) return false;

        CategoryWithNotesCount that = (CategoryWithNotesCount) o;

        if (accountId != that.accountId) return false;
        if (category != null ? !category.equals(that.category) : that.category != null)
            return false;
        return totalNotes != null ? totalNotes.equals(that.totalNotes) : that.totalNotes == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (accountId ^ (accountId >>> 32));
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (totalNotes != null ? totalNotes.hashCode() : 0);
        return result;
    }
}
