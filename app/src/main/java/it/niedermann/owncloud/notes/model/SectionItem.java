package it.niedermann.owncloud.notes.model;

import java.util.Calendar;

/**
 * Created by stefan on 23.10.15.
 */
public class SectionItem implements Item {
    private String title = "";
    private Calendar date;

    // TODO: generate the title, only store the date
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

    @Override
    public Calendar getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && (o == this || o instanceof SectionItem && ((SectionItem) o).getDate().equals(getDate()));
    }
}
