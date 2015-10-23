package it.niedermann.owncloud.notes.model;

/**
 * Created by stefan on 23.10.15.
 */
public class SectionItem implements Item {
    String title = "";

    public SectionItem(String title) {
        this.title = title;
    }

    public String geTitle() {
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
