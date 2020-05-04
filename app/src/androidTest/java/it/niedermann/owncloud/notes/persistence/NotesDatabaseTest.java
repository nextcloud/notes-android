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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.TimeZone;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NotesDatabaseTest {

    private NotesDatabase db = null;

    private String accountURL = "HelloHowAreYou";
    private String accountUserName = "John Doe";
    private String accountName = accountUserName + "@" + accountURL;
    private LocalAccount account = null;

    // use for single test
    private DBNote thisNote = null;

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
    public void test_01_addNote(){
        db.deleteNote(6, DBStatus.VOID);

        long accountID = account.getId();
        CloudNote cloudNote = new CloudNote(1, Calendar.getInstance(),
                "A Great Day", getCurDate() + " This is a really great day bro.",
                true, "Diary", null);

        // Pre-check
        List<DBNote> notes = db.getNotes(accountID);
        int pre_size = notes.size();
        Log.i("Test_01_addNote_All_Notes_Before_Addition", "Size: " + String.valueOf(pre_size));

        // Add a new note
        long noteID = db.addNote(accountID, cloudNote);
        // Check if this note is added successfully
        DBNote note = db.getNote(accountID, noteID);
        Log.i("Test_01_addNote_Cur_Note", note.toString());
        Log.i("Test_01_addNote_Cur_Note", "Title: " + note.getTitle());
        Log.i("Test_01_addNote_Cur_Note", "Content: " + note.getContent());
        Log.i("Test_01_addNote_Cur_Note", "Category: " + note.getCategory());

        assertEquals("A Great Day", note.getTitle());
        assertEquals(cloudNote.getContent(), note.getContent());
        assertEquals("Diary", note.getCategory());
        assertEquals(accountID, note.getAccountId());

        // Check if this note is in all notes
        notes = db.getNotes(accountID);
        int added_size = notes.size();

        assertEquals(1, added_size - pre_size);

        Log.i("Test_01_addNote_All_Notes_Added", "Size: " + String.valueOf(added_size));
        for (DBNote cnote : notes) {
            Log.i("Test_01_addNote_All_Notes_Added", cnote.toString());
        }

        // pass to next method
        thisNote = note;
    }

    @Test
    public void test_02_searchNotes() {
        long thisAccountID = account.getId();
        List<DBNote> notes = db.searchNotes(thisAccountID, null, null, false);
        Log.i("Test_02_searchNotes_Favorite_false", "Size: " + String.valueOf(notes.size()));
        assertEquals(notes.size(), 0);

        notes = db.searchNotes(thisAccountID, null, "Hello", true);
        Log.i("Test_02_searchNotes_Category_Hello", "Size: " + String.valueOf(notes.size()));
        assertEquals(notes.size(), 0);

        notes = db.searchNotes(thisAccountID, null, "Diary", true);
        Log.i("Test_02_searchNotes_Category_Diary_Favorite_True", "Size: " + String.valueOf(notes.size()));
        assertEquals(notes.size(), 1);

        notes = db.searchNotes(thisAccountID, null, null, null);
        Log.i("Test_02_searchNotes_Three_NULL", "Size: " + String.valueOf(notes.size()));
        assertEquals(notes.size(), 1);
    }

    @Test
    public void test_03_getCategories() {
        // i don't know how to test
    }

    @Test
    public void test_04_searchCategories() {
        // i don't know how to test
    }

    @Test
    public void test_05_deleteNote() {
        long thisAccountID = account.getId();
        List<DBNote> notes = db.getNotes(thisAccountID);
        int added_size = notes.size();

        Log.i("Test_05_deleteNote_All_Before_Deletion", "Size: " + String.valueOf(added_size));
        int counter = 0;
        for (DBNote cnote : notes) {
            Log.i("Test_05_deleteNote_All_Before_Deletion", cnote.toString());
            // Delete the note after testing
            db.deleteNote(cnote.getId(), cnote.getStatus());
            counter ++;
        }

        // Check if the note is deleted successfully
        notes = db.getNotes(thisAccountID);
        int deleted_size = notes.size();
        assertEquals(counter, added_size - deleted_size);
        Log.i("Test_05_deleteNote_All_Notes_After_Deletion", "Size: " + String.valueOf(deleted_size));
    }

    @Test
    public void testAddDeleteMultipleNotes() {

    }

    @Test
    public void setCategory() {
        // Unable to test with SSO
    }

    @Test
    public void updateNoteAndSync() {
        // Unable to test with SSO
    }

    @Test
    public void updateNote() {
        // can not check
        // need remoteNote (note from server)
    }

    public static String getCurDate() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
    }
}