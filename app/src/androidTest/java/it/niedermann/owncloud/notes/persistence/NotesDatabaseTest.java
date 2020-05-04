package it.niedermann.owncloud.notes.persistence;

import it.niedermann.owncloud.notes.model.CloudNote;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.DBStatus;
import it.niedermann.owncloud.notes.model.LocalAccount;
import it.niedermann.owncloud.notes.persistence.*;

import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.TimeZone;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NotesDatabaseTest {
    final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private NotesDatabase db = null;

    private String accountURL = "HelloHowAreYou";
    private String accountUserName = "John Doe";
    private String accountName = accountUserName + "@" + accountURL;
    private LocalAccount account = null;

    @Before
    public void setupDB() {
        Context context = ApplicationProvider.getApplicationContext();
        db = NotesDatabase.getInstance(context);
        // Create a new account if not exist
        account = db.getLocalAccountByAccountName(accountName);
        if (account.getId() == 0) {
            db.addAccount(accountURL, accountUserName, accountName);
            account = db.getLocalAccountByAccountName(accountName);
        }
    }

    // When we try to delete the account for testing, the process crashes. Still try to find out why
//    @After
//    public void deleteAccount() {
//        db.deleteAccount(account);
//    }

    @Test
    public void testSetUpEnv() {
        Log.i("Test_DB_Instance", db.toString());
        List<LocalAccount> accounts = db.getAccounts();
        for (LocalAccount account : accounts) {
            Log.i("Test_Get_Accounts", account.toString());
        }
    }

    @Test
    public void testAddDeleteNote() {
        long accountID = account.getId();
        CloudNote cloudNote = new CloudNote(1, Calendar.getInstance(),
                "A Great Day", getCurDate() + " This is a really great day bro.",
                true, "Diary", null);

        // Pre-check
        List<DBNote> notes = db.getNotes(accountID);
        int pre_size = notes.size();
        Log.i("Test_testAddDeleteNote_All_Notes_Before_Addition", "Size: " + String.valueOf(pre_size));

        // Add a new note
        long noteID = db.addNote(accountID, cloudNote);
        // Check if this note is added successfully
        DBNote note = db.getNote(accountID, noteID);
        Log.i("Test_testAddDeleteNote_Cur_Note", note.toString());
        Log.i("Test_testAddDeleteNote_Cur_Note", "Title: " + note.getTitle());
        Log.i("Test_testAddDeleteNote_Cur_Note", "Content: " + note.getContent());
        Log.i("Test_testAddDeleteNote_Cur_Note", "Category: " + note.getCategory());

        // Check if this note is in all notes
        notes = db.getNotes(accountID);
        int added_size = notes.size();
        assertEquals(1, added_size - pre_size);
        Log.i("Test_testAddDeleteNote_All_Notes_Added", "Size: " + String.valueOf(added_size));
        for (DBNote cnote : notes) {
            Log.i("Test_testAddDeleteNote_All_Notes_Added", cnote.toString());
        }

        // Delete the note after testing
        db.deleteNote(note.getId(), note.getStatus());

        // Check if the note is deleted successfully
        notes = db.getNotes(accountID);
        int deleted_size = notes.size();
        assertEquals(1, added_size - deleted_size);
        Log.i("Test_testAddDeleteNote_All_Notes_After_Deletion", "Size: " + String.valueOf(deleted_size));
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

    public static String getCurDate() {
        TimeZone time = TimeZone.getTimeZone("GMT+8");
        TimeZone.setDefault(time);
        Date curDate = new Date();
        return DATE_FORMAT.format(curDate);
    }
}