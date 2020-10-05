package it.niedermann.owncloud.notes.main.items.section;

import it.niedermann.owncloud.notes.shared.model.Item;

public class SectionItem implements Item {

    private String title;

    public SectionItem(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean isSection() {
        return true;
    }
}
