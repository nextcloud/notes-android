package it.niedermann.owncloud.notes.model;

import java.util.Calendar;

/**
 * Created by stefan on 23.10.15.
 */
public interface Item {
    boolean isSection();
    Calendar getDate();
}
