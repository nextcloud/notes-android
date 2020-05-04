package it.niedermann.owncloud.notes.persistence;

import it.niedermann.owncloud.notes.persistence.*;

import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NotesDatabaseTest {
    private NotesDatabase db;

    @Before
    public void createDB() {
        Context context = ApplicationProvider.getApplicationContext();
        db = NotesDatabase.getInstance(context);
        Log.i("TestLog", db.toString());
    }

    @Test
    public void testAddNote() {

    }

    @Test
    public void searchNotes() {
    }

    @Test
    public void getCategories() {
    }

    @Test
    public void searchCategories() {
    }

    @Test
    public void setCategory() {
    }

    @Test
    public void updateNoteAndSync() {
    }

    @Test
    public void updateNote() {
    }
}