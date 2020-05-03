package it.niedermann.owncloud.notes.persistence;

import junit.framework.TestCase;

import it.niedermann.owncloud.notes.android.activity.NotesListViewActivity;

public class TestNotesDatabase extends TestCase {
    public void testSearchCategoryId() {
        NotesDatabase notesDatabase = NotesDatabase.getInstance(new NotesListViewActivity());
        notesDatabase.getCategoryIdByTitle(1, "helloworld");
    }
}
