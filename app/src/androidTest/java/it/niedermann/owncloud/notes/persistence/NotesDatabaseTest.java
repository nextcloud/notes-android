package it.niedermann.owncloud.notes.persistence;

import it.niedermann.owncloud.notes.model.CloudNote;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.DBStatus;
import it.niedermann.owncloud.notes.model.LocalAccount;
import it.niedermann.owncloud.notes.model.NavigationAdapter;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;

import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.TimeZone;

import static org.junit.Assert.*;


/**
 * WARNING: for all the test case written by order
 * you must run all the test case in the same time
 * or some problem will happens
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NotesDatabaseTest {

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
        assert account != null;
        if (account.getId() == 0) {
            db.addAccount(accountURL, accountUserName, accountName);
            account = db.getLocalAccountByAccountName(accountName);
        }
    }

    @Test
    public void testSetUpEnv() {
        Log.i("Test_DB_Instance", db.toString());
        List<LocalAccount> accounts = db.getAccounts();
        for (LocalAccount account : accounts) {
            Log.i("Test_Get_Accounts", account.toString());
        }
    }

    @Test
    public void test_01_addNote() {

        long accountID = account.getId();
        CloudNote cloudNote = new CloudNote(1, Calendar.getInstance(),
                "A Great Day", getCurDate() + " This is a really great day bro.",
                true, "Diary", null);

        // Pre-check
        List<DBNote> notes = db.getNotes(accountID);
        int pre_size = notes.size();
        Log.i("Test_01_addNote_All_Notes_Before_Addition", "Size: " + pre_size);

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

        Log.i("Test_01_addNote_All_Notes_Added", "Size: " + added_size);
        for (DBNote cnote : notes) {
            Log.i("Test_01_addNote_All_Notes_Added", cnote.toString());
            Log.i("Test_01_addNote_All_Notes_Added", cnote.getTitle());
        }

    }

    @Test
    public void test_02_searchNotes() {
        long thisAccountID = account.getId();
        List<DBNote> notes = db.searchNotes(thisAccountID, null, null, false);
        Log.i("Test_02_searchNotes_Favorite_false", "Size: " + notes.size());
        assertEquals(notes.size(), 0);

        notes = db.searchNotes(thisAccountID, null, "Hello", true);
        Log.i("Test_02_searchNotes_Category_Hello", "Size: " + notes.size());
        assertEquals(notes.size(), 0);

        notes = db.searchNotes(thisAccountID, null, "Diary", true);
        Log.i("Test_02_searchNotes_Category_Diary_Favorite_True", "Size: " + notes.size());
        assertEquals(notes.size(), 1);

        notes = db.searchNotes(thisAccountID, null, null, null);
        Log.i("Test_02_searchNotes_Three_NULL", "Size: " + notes.size());
        assertEquals(notes.size(), 1);
    }

    @Test
    public void test_03_getCategories() {
        List<NavigationAdapter.NavigationItem> categories = db.getCategories(account.getId());
        boolean exitFlag = false;
        for (NavigationAdapter.NavigationItem categoryItem : categories) {
            Log.i("Test_03_getCategories_Item", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
            if (categoryItem.label.equals("Diary")) {
                exitFlag = true;
            }
        }
        assertTrue(exitFlag);
    }

    @Test
    public void test_04_searchCategories() {
        List<NavigationAdapter.NavigationItem> categories = db.searchCategories(account.getId(), "Dia");
        boolean exitFlag = false;
        for (NavigationAdapter.NavigationItem categoryItem : categories) {
            Log.i("Test_04_searchCategories_Dia", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
            if (categoryItem.label.equals("Diary")) {
                exitFlag = true;
            }
        }
        assertTrue(exitFlag);

//        // The second parameter is annotated as @NonNull. This test is invalid. Please remove it
//        categories = db.searchCategories(account.getId(), null);
//        exitFlag = false;
//        for (NavigationAdapter.NavigationItem categoryItem : categories) {
//            Log.i("Test_04_searchCategories_Item_Diary", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
//            if (categoryItem.label.equals("Diary")) {
//                exitFlag = true;
//            }
//        }
//        assertTrue(exitFlag);

        categories = db.searchCategories(account.getId(), "Mike Chester Wang");
        exitFlag = false;
        for (NavigationAdapter.NavigationItem categoryItem : categories) {
            Log.i("Test_04_searchCategories_Item_Mike_Chester_Wang", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
            if (categoryItem.label.equals("Diary")) {
                exitFlag = true;
            }
        }
        assertFalse(exitFlag);
    }

    @Test
    public void test_05_deleteNote() {
        long thisAccountID = account.getId();
        List<DBNote> notes = db.getNotes(thisAccountID);
        int added_size = notes.size();

        Log.i("Test_05_deleteNote_All_Before_Deletion", "Size: " + added_size);
        int counter = 0;
        for (DBNote cnote : notes) {
            Log.i("Test_05_deleteNote_All_Before_Deletion", cnote.toString());
            // Delete the note after testing
            db.deleteNote(cnote.getId(), cnote.getStatus());
            counter++;
        }

        // Check if the note is deleted successfully
        notes = db.getNotes(thisAccountID);
        int deleted_size = notes.size();
        assertEquals(counter, added_size - deleted_size);
        Log.i("Test_05_deleteNote_All_Notes_After_Deletion", "Size: " + deleted_size);
    }

    @Test
    public void test_06_multiAddNote() {
        long thisAccountID = account.getId();
        ArrayList<CloudNote> multiCloudNote = new ArrayList<>();
        multiCloudNote.add(new CloudNote(1, Calendar.getInstance(),
                "Mike is so cool.", "Mike is a cool guy you know",
                true, "The BiBle", null));
        multiCloudNote.add(new CloudNote(1, Calendar.getInstance(),
                "Andy is so cool.", "Andy is a cool guy you know",
                true, "The BiBle", null));
        multiCloudNote.add(new CloudNote(1, Calendar.getInstance(),
                "your backpack", "you backpack by Eason Chan",
                true, "Music", null));
        multiCloudNote.add(new CloudNote(1, Calendar.getInstance(),
                "I Honestly Love You", "I Honestly Love You by Leslie",
                true, "Music", null));
        multiCloudNote.add(new CloudNote(1, Calendar.getInstance(),
                "Monica", "Monica by Leslie",
                true, "Music", null));
        multiCloudNote.add(new CloudNote(1, Calendar.getInstance(),
                "Worksheet", "1 2 3 4 5 6 7 8",
                false, "Work", null));
        multiCloudNote.add(new CloudNote(1, Calendar.getInstance(),
                "PowerPoint.", "8 7 6 5 4 3 2 1",
                false, "Work", null));
        multiCloudNote.add(new CloudNote(1, Calendar.getInstance(),
                "Farewell My Concubine", "a great movie",
                true, "Movie", null));
        multiCloudNote.add(new CloudNote(1, Calendar.getInstance(),
                "Leon", "an amazing movie",
                true, "Movie", null));
        multiCloudNote.add(new CloudNote(1, Calendar.getInstance(),
                "How are you.", "i am fine.",
                false, "Diary", null));

        // Pre-check
        List<DBNote> notes = db.getNotes(thisAccountID);
        int pre_size = notes.size();
        Log.i("Test_06_multiAddNote_All_Notes_Before_Addition", "Size: " + pre_size);

        long[] multiNoteID = new long[10];
        for (int i = 0; i < 10; ++i) {
            multiNoteID[i] = db.addNote(thisAccountID, multiCloudNote.get(i));
        }

        // check if the node added successfully
        for (int i = 0; i < 10; ++i) {
            DBNote nodeTemp = db.getNote(thisAccountID, multiNoteID[i]);
            assertEquals(nodeTemp.getTitle(), multiCloudNote.get(i).getTitle());
            assertEquals(nodeTemp.getCategory(), multiCloudNote.get(i).getCategory());
            assertEquals(nodeTemp.getContent(), multiCloudNote.get(i).getContent());
            assertEquals(nodeTemp.getAccountId(), thisAccountID);
            Log.i("Test_06_multiAddNote_All_Notes_Addition_sucessful", nodeTemp.toString());
        }

        // check if these note is in all notes
        notes = db.getNotes(thisAccountID);
        int add_size = notes.size();
        assertEquals(10, add_size - pre_size);

        Log.i("Test_06_multiAddNote_All_Notes_After_Addition", "Size: " + add_size);
    }

    @Test
    public void test_07_multiSearchNotes() {
        long thisAccountID = account.getId();
        List<DBNote> notes = db.searchNotes(thisAccountID, null, null, null);
        Log.i("Test_07_multiSearchNotes_null_null_null", "Size: " + notes.size());
        assertEquals(notes.size(), 10);

        notes = db.searchNotes(thisAccountID, null, null, true);
        Log.i("Test_07_multiSearchNotes_null_null_true", "Size: " + notes.size());
        assertEquals(notes.size(), 7);

        notes = db.searchNotes(thisAccountID, null, "Music", null);
        Log.i("Test_07_multiSearchNotes_null_Music_null", "Size: " + notes.size());
        assertEquals(notes.size(), 3);

        notes = db.searchNotes(thisAccountID, null, "Work", true);
        Log.i("Test_07_multiSearchNotes_null_Work_true", "Size: " + notes.size());
        assertEquals(notes.size(), 0);

        notes = db.searchNotes(thisAccountID, null, "Diary", null);
        Log.i("Test_07_multiSearchNotes_null_Diary_null", "Size: " + notes.size());
        assertEquals(notes.size(), 1);

        notes = db.searchNotes(thisAccountID, "Mike", null, null);
        Log.i("Test_07_multiSearchNotes_Mike_null_null", "Size: " + notes.size());
        assertEquals(notes.size(), 1);
    }

    @Test
    public void test_08_multiGetCategories() {
        List<NavigationAdapter.NavigationItem> categories = db.getCategories(account.getId());
        int count = 0;
        for (NavigationAdapter.NavigationItem categoryItem : categories) {
            Log.i("Test_08_multiGetCategories_Item", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
            count++;
        }
        Log.i("count count count", "count " + count);
        assertEquals(5, count);
        Log.i("count count count", "count " + count);
    }

    @Test
    public void test_09_multiSearchCategories() {
        List<NavigationAdapter.NavigationItem> categories = db.searchCategories(account.getId(), "M");
        int count = 0;
        for (NavigationAdapter.NavigationItem categoryItem : categories) {
            Log.i("Test_09_multiSearchCategories_Item", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
            count++;
        }
        assertEquals(2, count);

        categories = db.searchCategories(account.getId(), "Mike");
        count = 0;
        for (NavigationAdapter.NavigationItem categoryItem : categories) {
            Log.i("Test_09_multiSearchCategories_Item", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
            count++;
        }
        assertEquals(0, count);

        categories = db.searchCategories(account.getId(), "M");
        boolean exitFlag = false;
        for (NavigationAdapter.NavigationItem categoryItem : categories) {
            Log.i("Test_04_searchCategories_Dia", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
            if (categoryItem.label.equals("Music")) {
                exitFlag = true;
            }
        }
        assertTrue(exitFlag);


        categories = db.searchCategories(account.getId(), "WOk");
        exitFlag = false;
        for (NavigationAdapter.NavigationItem categoryItem : categories) {
            Log.i("Test_04_searchCategories_Dia", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
            if (categoryItem.label.equals("Diary")) {
                exitFlag = true;
            }
        }
        assertFalse(exitFlag);

        categories = db.searchCategories(account.getId(), "hello");
        exitFlag = false;
        for (NavigationAdapter.NavigationItem categoryItem : categories) {
            Log.i("Test_04_searchCategories_Dia", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
            if (categoryItem.label.equals("Diary")) {
                exitFlag = true;
            }
        }
        assertFalse(exitFlag);
    }

    @Test
    public void test_10_multiDeleteNote() {

//        List<NavigationAdapter.NavigationItem> cat = db.getCategories(account.getId());
//        for (NavigationAdapter.NavigationItem categoryItem : cat) {
//            Log.i("12bTest_test_getCategories_Item", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
//        }

        long thisAccountID = account.getId();
        List<DBNote> notes = db.getNotes(thisAccountID);
        int added_size = notes.size();

//        int counter = 0;
        Log.i("Test_10_multiDeleteNote_All_Before_Deletion", "Size: " + added_size);
        for (DBNote e : notes) {
            Log.i("Test_10_multiDeleteNote_All_Before_Deletion", e.toString());
            db.deleteNote(e.getId(), e.getStatus());

//            cat = db.getCategories(account.getId());
//            Log.i("12aTest_test_getCategories_Item", "counter: " + ++counter);
//            for (NavigationAdapter.NavigationItem categoryItem : cat) {
//                Log.i("12aTest_test_getCategories_Item", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
//            }

        }

        // Check if the note is deleted successfully
        notes = db.getNotes(thisAccountID);
        int deleted_size = notes.size();
        assertEquals(10, added_size - deleted_size);
        Log.i("Test_10_multiDeleteNote_All_After_Deletion", "Size: " + deleted_size);

    }

    @Test
    public void test_11_Chinese() {

        long accountID = account.getId();
        CloudNote cloudNote = new CloudNote(1, Calendar.getInstance(),
                "美好的一天", getCurDate() + " 兄弟，这真是美好的一天。",
                true, "日记", null);

        // Pre-check
        List<DBNote> notes = db.getNotes(accountID);
        int pre_size = notes.size();
        Log.i("Test_11_Chinese_All_Notes_Before_Addition", "Size: " + pre_size);

        // Add a new note
        long noteID = db.addNote(accountID, cloudNote);
        // Check if this note is added successfully
        DBNote note = db.getNote(accountID, noteID);
        Log.i("Test_11_Chinese_Cur_Note", note.toString());
        Log.i("Test_11_Chinese_Cur_Note", "Title: " + note.getTitle());
        Log.i("Test_11_Chinese_Cur_Note", "Content: " + note.getContent());
        Log.i("Test_11_Chinese_Cur_Note", "Category: " + note.getCategory());

        assertEquals("美好的一天", note.getTitle());
        assertEquals(cloudNote.getContent(), note.getContent());
        assertEquals("日记", note.getCategory());
        assertEquals(accountID, note.getAccountId());

        // Check if this note is in all notes
        notes = db.getNotes(accountID);
        int added_size = notes.size();

        assertEquals(1, added_size - pre_size);

        Log.i("Test_11_Chinese_All_Notes_Added", "Size: " + added_size);
        for (DBNote cnote : notes) {
            Log.i("Test_11_Chinese_All_Notes_Added", cnote.toString());
        }

        long thisAccountID = account.getId();
        notes = db.searchNotes(thisAccountID, "美好", "日记", true);
        Log.i("Test_11_Chinese", "Size: " + notes.size());
        assertEquals(1, notes.size());

        List<NavigationAdapter.NavigationItem> categories = db.getCategories(account.getId());
        boolean exitFlag = false;
        for (NavigationAdapter.NavigationItem categoryItem : categories) {
            Log.i("Test_11_Chinese_Item", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
            if (categoryItem.label.equals("日记")) {
                exitFlag = true;
            }
        }
        assertTrue(exitFlag);

        categories = db.searchCategories(account.getId(), "记");
        exitFlag = false;
        for (NavigationAdapter.NavigationItem categoryItem : categories) {
            Log.i("Test_11_Chinese_Item", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
            if (categoryItem.label.equals("日记")) {
                exitFlag = true;
            }
        }
        assertTrue(exitFlag);

        notes = db.getNotes(thisAccountID);
        for (DBNote cnote : notes) {
            Log.i("Test_11_Chinese_All_Before_Deletion", cnote.toString());
            // Delete the note after testing
            db.deleteNote(cnote.getId(), cnote.getStatus());
        }

        // Check if the note is deleted successfully
        notes = db.getNotes(thisAccountID);
        int deleted_size = notes.size();
        assertEquals(1, added_size - deleted_size);
        Log.i("Test_11_Chinese_All_Notes_After_Deletion", "Size: " + deleted_size);

    }

    @Test
    public void test_12_getCategoryIdByTitle() {

        try {
            Method method = NotesDatabase.class.getDeclaredMethod("getCategoryIdByTitle",
                    long.class,
                    String.class,
                    boolean.class);
            method.setAccessible(true);

            List<NavigationAdapter.NavigationItem> categories = db.getCategories(account.getId());
            int count = 0;
            for (NavigationAdapter.NavigationItem categoryItem : categories) {
                Log.i("Test_12_getCategoryIdByTitle", String.format("%s | %s | %d | %d",
                        categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
                count++;
            }
            Log.i("Test_12_getCategoryIdByTitle", "count " + count);

            int catID;

            // Find an existing category to test false
            if (count > 0) {
                catID = (int) method.invoke(db, account.getId(), categories.get(0).label, false);
                assertNotEquals(catID, -1);
            }

            // Create a category not existing
            String cur_cat = "Mike Chester Wang's Diary" + getCurDate();
            catID = (int) method.invoke(db, account.getId(), cur_cat, false);
            assertEquals(catID, -1);

            catID = (int) method.invoke(db, account.getId(), cur_cat, true);
            assertNotEquals(catID, -1);
        } catch (Exception e) {
            fail(Arrays.toString(e.getStackTrace()));
            Log.e("Test_12_getCategoryIdByTitle", Arrays.toString(e.getStackTrace()));
        }
    }

    @Test
    public void test_13_getNotesCustom() {
        // TODO: ~
    }

    @Test
    public void test_14_searchCategories() {
        // TODO: ~
    }

    public static String getCurDate() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
    }
}