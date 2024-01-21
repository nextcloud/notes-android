package it.niedermann.owncloud.notes.persistence.entity;

import androidx.room.Ignore;

import java.util.Objects;

public class CategoryWithNotesCount {

    private long accountId;
    private String category;
    private Integer totalNotes;

    public CategoryWithNotesCount() {
        // Default constructor for Room
    }

    @Ignore
    public CategoryWithNotesCount(long accountId, String category, Integer totalNotes) {
        this.accountId = accountId;
        this.category = category;
        this.totalNotes = totalNotes;
    }

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
        if (!(o instanceof CategoryWithNotesCount that)) return false;

        if (accountId != that.accountId) return false;
        if (!Objects.equals(category, that.category))
            return false;
        return Objects.equals(totalNotes, that.totalNotes);
    }

    @Override
    public int hashCode() {
        int result = (int) (accountId ^ (accountId >>> 32));
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (totalNotes != null ? totalNotes.hashCode() : 0);
        return result;
    }
}
