package it.niedermann.owncloud.notes.persistence.entity;

import androidx.room.Embedded;

public class CategoryWithNotesCount {

    @Embedded
    private CategoryEntity category;
    private Integer totalNotes;

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }

    public Integer getTotalNotes() {
        return totalNotes;
    }

    public void setTotalNotes(Integer totalNotes) {
        this.totalNotes = totalNotes;
    }
}
