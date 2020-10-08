package it.niedermann.owncloud.notes.main.slots;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.main.items.section.SectionItem;
import it.niedermann.owncloud.notes.persistence.entity.NoteWithCategory;
import it.niedermann.owncloud.notes.shared.model.Item;
import it.niedermann.owncloud.notes.shared.util.NoteUtil;

public class SlotterUtil {

    private SlotterUtil() {
        // Util class
    }

    @NonNull
    public static List<Item> fillListByCategory(@NonNull List<NoteWithCategory> noteList, @Nullable String currentCategory) {
        List<Item> itemList = new ArrayList<>();
        for (NoteWithCategory note : noteList) {
            if (currentCategory != null && !currentCategory.equals(note.getCategory())) {
                itemList.add(new SectionItem(NoteUtil.extendCategory(note.getCategory())));
            }

            itemList.add(note);
            currentCategory = note.getCategory();
        }
        return itemList;
    }

    @NonNull
    public static List<Item> fillListByTime(@NonNull Context context, @NonNull List<NoteWithCategory> noteList) {
        List<Item> itemList = new ArrayList<>();
        Timeslotter timeslotter = new Timeslotter(context);
        String lastTimeslot = null;
        for (int i = 0; i < noteList.size(); i++) {
            NoteWithCategory currentNote = noteList.get(i);
            String timeslot = timeslotter.getTimeslot(currentNote.getNote());
            if (i > 0 && !timeslot.equals(lastTimeslot)) {
                itemList.add(new SectionItem(timeslot));
            }
            itemList.add(currentNote);
            lastTimeslot = timeslot;
        }

        return itemList;
    }

    @NonNull
    public static List<Item> fillListByInitials(@NonNull Context context, @NonNull List<NoteWithCategory> noteList) {
        List<Item> itemList = new ArrayList<>();
        String lastInitials = null;
        for (int i = 0; i < noteList.size(); i++) {
            NoteWithCategory currentNote = noteList.get(i);
            String initials = currentNote.getNote().getTitle().substring(0, 1).toUpperCase();
            if (!initials.matches("[A-Z\\u00C0-\\u00DF]")) {
                initials = initials.matches("[\\u0250-\\uFFFF]") ? context.getString(R.string.simple_other) : "#";
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
