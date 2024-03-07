/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.slots;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.main.items.section.SectionItem;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.Item;
import it.niedermann.owncloud.notes.shared.util.NoteUtil;

public class SlotterUtil {

    private SlotterUtil() {
        // Util class
    }

    @NonNull
    public static List<Item> fillListByCategory(@NonNull List<Note> noteList, @Nullable String currentCategory) {
        final var itemList = new ArrayList<Item>();
        for (final var note : noteList) {
            if (currentCategory != null && !currentCategory.equals(note.getCategory())) {
                itemList.add(new SectionItem(NoteUtil.extendCategory(note.getCategory())));
            }

            itemList.add(note);
            currentCategory = note.getCategory();
        }
        return itemList;
    }

    @NonNull
    public static List<Item> fillListByTime(@NonNull Context context, @NonNull List<Note> noteList) {
        final var itemList = new ArrayList<Item>();
        final var timeslotter = new Timeslotter(context);
        String lastTimeslot = null;
        for (int i = 0; i < noteList.size(); i++) {
            final var currentNote = noteList.get(i);
            String timeslot = timeslotter.getTimeslot(currentNote);
            if (i > 0 && !timeslot.equals(lastTimeslot)) {
                itemList.add(new SectionItem(timeslot));
            }
            itemList.add(currentNote);
            lastTimeslot = timeslot;
        }

        return itemList;
    }

    @NonNull
    public static List<Item> fillListByInitials(@NonNull Context context, @NonNull List<Note> noteList) {
        final var itemList = new ArrayList<Item>();
        String lastInitials = null;
        for (int i = 0; i < noteList.size(); i++) {
            final var currentNote = noteList.get(i);
            final var title = currentNote.getTitle();
            String initials = "";
            if(!TextUtils.isEmpty(title)) {
                initials = title.substring(0, 1).toUpperCase();
                if (!initials.matches("[A-Z\\u00C0-\\u00DF]")) {
                    initials = initials.matches("[\\u0250-\\uFFFF]") ? context.getString(R.string.simple_other) : "#";
                }
            }
            if (i > 0 && !initials.equals(lastInitials)) {
                itemList.add(new SectionItem(initials));
            }
            itemList.add(currentNote);
            lastInitials = initials;
        }

        return itemList;
    }
}
