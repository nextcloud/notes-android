package it.niedermann.owncloud.notes.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import it.niedermann.owncloud.notes.main.navigation.NavigationItem;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.persistence.entity.NoteWithCategory;
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import it.niedermann.owncloud.notes.shared.model.CategorySortingMethod;
import it.niedermann.owncloud.notes.shared.model.DBStatus;
import it.niedermann.owncloud.notes.shared.model.NavigationCategory;
import it.niedermann.owncloud.notes.shared.util.NoteUtil;

import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.FAVORITES;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.RECENT;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.UNCATEGORIZED;
import static it.niedermann.owncloud.notes.shared.util.DisplayUtils.convertToCategoryNavigationItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * WARNING: for all the test case written by order
 * you must run all the test case in the same time
 * or some problem will happens
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NotesNotesDatabaseTest {

    @NonNull
    private Context context;
    private NotesDatabase db = null;

    private String accountURL = "HelloHowAreYou";
    private String accountUserName = "John Doe";
    private String accountName = accountUserName + "@" + accountURL;
    private Account account = null;

    @Before
    public void setupDB() throws NextcloudHttpRequestFailedException {
        context = ApplicationProvider.getApplicationContext();
        db = NotesDatabase.getInstance(context);
        // Create a new account if not exist
        account = db.getAccountDao().getLocalAccountByAccountName(accountName);
        if (account == null) {
            final String response = "{\"ocs\":{\"meta\":{\"status\":\"ok\",\"statuscode\":200,\"message\":\"OK\"},\"data\":{\"version\":{\"major\":18,\"minor\":0,\"micro\":4,\"string\":\"18.0.4\",\"edition\":\"\",\"extendedSupport\":false},\"capabilities\":{\"core\":{\"pollinterval\":60,\"webdav-root\":\"remote.php\\/webdav\"},\"bruteforce\":{\"delay\":0},\"files\":{\"bigfilechunking\":true,\"blacklisted_files\":[\".htaccess\"],\"directEditing\":{\"url\":\"https:\\/\\/efss.qloud.my\\/ocs\\/v2.php\\/apps\\/files\\/api\\/v1\\/directEditing\",\"etag\":\"ed2b141af2a39b0e42666952ba60988d\"},\"versioning\":true,\"undelete\":true},\"activity\":{\"apiv2\":[\"filters\",\"filters-api\",\"previews\",\"rich-strings\"]},\"ocm\":{\"enabled\":true,\"apiVersion\":\"1.0-proposal1\",\"endPoint\":\"https:\\/\\/efss.qloud.my\\/index.php\\/ocm\",\"resourceTypes\":[{\"name\":\"file\",\"shareTypes\":[\"user\",\"group\"],\"protocols\":{\"webdav\":\"\\/public.php\\/webdav\\/\"}}]},\"deck\":{\"version\":\"0.8.2\"},\"richdocuments\":{\"mimetypes\":[\"application\\/vnd.oasis.opendocument.text\",\"application\\/vnd.oasis.opendocument.spreadsheet\",\"application\\/vnd.oasis.opendocument.graphics\",\"application\\/vnd.oasis.opendocument.presentation\",\"application\\/vnd.lotus-wordpro\",\"application\\/vnd.visio\",\"application\\/vnd.wordperfect\",\"application\\/msonenote\",\"application\\/msword\",\"application\\/rtf\",\"text\\/rtf\",\"application\\/vnd.openxmlformats-officedocument.wordprocessingml.document\",\"application\\/vnd.openxmlformats-officedocument.wordprocessingml.template\",\"application\\/vnd.ms-word.document.macroEnabled.12\",\"application\\/vnd.ms-word.template.macroEnabled.12\",\"application\\/vnd.ms-excel\",\"application\\/vnd.openxmlformats-officedocument.spreadsheetml.sheet\",\"application\\/vnd.openxmlformats-officedocument.spreadsheetml.template\",\"application\\/vnd.ms-excel.sheet.macroEnabled.12\",\"application\\/vnd.ms-excel.template.macroEnabled.12\",\"application\\/vnd.ms-excel.addin.macroEnabled.12\",\"application\\/vnd.ms-excel.sheet.binary.macroEnabled.12\",\"application\\/vnd.ms-powerpoint\",\"application\\/vnd.openxmlformats-officedocument.presentationml.presentation\",\"application\\/vnd.openxmlformats-officedocument.presentationml.template\",\"application\\/vnd.openxmlformats-officedocument.presentationml.slideshow\",\"application\\/vnd.ms-powerpoint.addin.macroEnabled.12\",\"application\\/vnd.ms-powerpoint.presentation.macroEnabled.12\",\"application\\/vnd.ms-powerpoint.template.macroEnabled.12\",\"application\\/vnd.ms-powerpoint.slideshow.macroEnabled.12\",\"text\\/csv\"],\"mimetypesNoDefaultOpen\":[\"image\\/svg+xml\",\"application\\/pdf\",\"text\\/plain\",\"text\\/spreadsheet\"],\"collabora\":[],\"direct_editing\":false,\"templates\":false,\"productName\":\"\\u5728\\u7ebf\\u534f\\u4f5c\"},\"dav\":{\"chunking\":\"1.0\"},\"files_sharing\":{\"api_enabled\":true,\"public\":{\"enabled\":true,\"password\":{\"enforced\":true,\"askForOptionalPassword\":false},\"expire_date\":{\"enabled\":true,\"days\":\"7\",\"enforced\":false},\"multiple_links\":true,\"expire_date_internal\":{\"enabled\":false},\"send_mail\":false,\"upload\":true,\"upload_files_drop\":true},\"resharing\":true,\"user\":{\"send_mail\":false,\"expire_date\":{\"enabled\":true}},\"group_sharing\":true,\"group\":{\"enabled\":true,\"expire_date\":{\"enabled\":true}},\"default_permissions\":31,\"federation\":{\"outgoing\":false,\"incoming\":false,\"expire_date\":{\"enabled\":true}},\"sharee\":{\"query_lookup_default\":false},\"sharebymail\":{\"enabled\":true,\"upload_files_drop\":{\"enabled\":true},\"password\":{\"enabled\":true},\"expire_date\":{\"enabled\":true}}},\"external\":{\"v1\":[\"sites\",\"device\",\"groups\",\"redirect\"]},\"notifications\":{\"ocs-endpoints\":[\"list\",\"get\",\"delete\",\"delete-all\",\"icons\",\"rich-strings\",\"action-web\"],\"push\":[\"devices\",\"object-data\",\"delete\"],\"admin-notifications\":[\"ocs\",\"cli\"]},\"password_policy\":{\"minLength\":8,\"enforceNonCommonPassword\":true,\"enforceNumericCharacters\":false,\"enforceSpecialCharacters\":false,\"enforceUpperLowerCase\":false,\"api\":{\"generate\":\"https:\\/\\/efss.qloud.my\\/ocs\\/v2.php\\/apps\\/password_policy\\/api\\/v1\\/generate\",\"validate\":\"https:\\/\\/efss.qloud.my\\/ocs\\/v2.php\\/apps\\/password_policy\\/api\\/v1\\/validate\"}},\"theming\":{\"name\":\"QloudData\",\"url\":\"https:\\/\\/www.qloud.my\\/qloud-data\\/\",\"slogan\":\"Powered by NextCloud\",\"color\":\"#1E4164\",\"color-text\":\"#ffffff\",\"color-element\":\"#1E4164\",\"logo\":\"https:\\/\\/efss.qloud.my\\/index.php\\/apps\\/theming\\/image\\/logo?useSvg=1&v=47\",\"background\":\"https:\\/\\/efss.qloud.my\\/core\\/img\\/background.png?v=47\",\"background-plain\":false,\"background-default\":true,\"logoheader\":\"https:\\/\\/efss.qloud.my\\/index.php\\/apps\\/theming\\/image\\/logo?useSvg=1&v=47\",\"favicon\":\"https:\\/\\/efss.qloud.my\\/index.php\\/apps\\/theming\\/image\\/logo?useSvg=1&v=47\"},\"registration\":{\"enabled\":true,\"apiRoot\":\"\\/ocs\\/v2.php\\/apps\\/registration\\/api\\/v1\\/\",\"apiLevel\":\"v1\"}}}}}";
            Capabilities capabilities = new Capabilities(response, null);
            assertNotNull(capabilities);
            db.addAccount(accountURL, accountUserName, accountName, capabilities);
            account = db.getAccountDao().getLocalAccountByAccountName(accountName);
        }
    }

    @Test
    public void testSetUpEnv() {
        Log.i("Test_DB_Instance", db.toString());
        List<Account> accounts = db.getAccountDao().getAccounts();
        for (Account account : accounts) {
            Log.i("Test_Get_Accounts", account.toString());
        }
    }

    @Test
    public void test_01_addNote_CloudNote() {
        long accountID = account.getId();   // retrieve account id
        // Create a cloud note for argument passing
        NoteWithCategory cloudNote = new NoteWithCategory(new Note(1L, Calendar.getInstance(),
                "A Great Day", getCurDate() + " This is a really great day bro.",
                true, null), "Diary");

        // Pre-check
        List<Note> notes = db.getNoteDao().getNotes(accountID);
        int pre_size = notes.size();
        Log.i("Test_01_addNote_All_Notes_Before_Addition", "Size: " + pre_size);

        // Add a new note
        long noteID = db.addNote(accountID, cloudNote);
        // Check if this note is added successfully
        NoteWithCategory note = db.getNoteDao().getNoteWithCategory(accountID, noteID);
        Log.i("Test_01_addNote_Cur_Note", note.toString());
        Log.i("Test_01_addNote_Cur_Note", "Title: " + note.getTitle());
        Log.i("Test_01_addNote_Cur_Note", "Content: " + note.getContent());
        Log.i("Test_01_addNote_Cur_Note", "Category: " + note.getCategory());

        assertEquals("A Great Day", note.getTitle());
        assertEquals(cloudNote.getContent(), note.getContent());
        assertEquals("Diary", note.getCategory());
        assertEquals(accountID, note.getAccountId().longValue());

        // Check if this note is in all notes
        notes = db.getNoteDao().getNotes(accountID);
        int added_size = notes.size();
        assertEquals(1, added_size - pre_size);

        Log.i("Test_01_addNote_All_Notes_Added", "Size: " + added_size);
        for (Note cnote : notes) {
            Log.i("Test_01_addNote_All_Notes_Added", cnote.toString());
            Log.i("Test_01_addNote_All_Notes_Added", cnote.getTitle());
        }

        NoteWithCategory cloudNote_re0 = new NoteWithCategory(new Note(0L, Calendar.getInstance(),
                "A Bad Day", getCurDate() + " You're faking a smile with just a coffee to go (Daniel Powter).",
                true, null), "A Nice Song");
        noteID = db.addNote(accountID, cloudNote_re0);
        note = db.getNoteDao().getNoteWithCategory(accountID, noteID);
        // Check
        assertEquals("A Bad Day", note.getTitle());
        assertEquals(cloudNote_re0.getContent(), note.getContent());
        assertEquals("A Nice Song", note.getCategory());
        assertEquals(accountID, note.getAccountId().longValue());
    }

    @Test
    @Ignore
    public void test_02_addNote_and_getNotesCustom_DBNote() {
        try {
            long accountID = account.getId();   // retrieve account id

            // get a new note id to avoid UNIQUE Note_ID constraint
            // getNotesCustom also tested here
            Method getNC = NotesDatabase.class.getDeclaredMethod("getNotesCustom", long.class, String.class, String[].class, String.class, boolean.class);
            getNC.setAccessible(true);
            List<NoteWithCategory> notes = (List<NoteWithCategory>) getNC.invoke(db, accountID, "status != ? AND accountId = ?",
                    new String[]{DBStatus.LOCAL_DELETED.getTitle(), "" + accountID}, "id ASC", false);
            long newNoteID = notes.get(notes.size() - 1).getId() + 1;   // avoid UNIQUE Note_ID constraint

            // Create a DBNote for argument passing
            String newContent = getCurDate() + " This is a even greater day my friend.";
            NoteWithCategory dbNote = new NoteWithCategory(new Note(newNoteID, 1L, Calendar.getInstance(), "A Greater Day",
                    newContent, true, null, DBStatus.VOID,
                    accountID, NoteUtil.generateNoteExcerpt(newContent, "Test-Title"), 0), "Best Friend's Record");

            // Add a new note
            long noteID = db.addNote(accountID, dbNote);
            // Check if this note is added successfully
            NoteWithCategory note = db.getNoteDao().getNoteWithCategory(accountID, noteID);
            assertEquals(dbNote.getTitle(), note.getTitle());
            assertEquals(dbNote.getContent(), note.getContent());
            assertEquals(dbNote.getCategory(), note.getCategory());
            assertEquals(dbNote.getAccountId(), note.getAccountId());

            // Another DBNote for argument passing
            newContent = getCurDate() + " This is a even greater day my friend.";
            dbNote = new NoteWithCategory(new Note(0, 1L, Calendar.getInstance(), "An Even Greater Day",
                    newContent, true, null, DBStatus.VOID,
                    accountID, NoteUtil.generateNoteExcerpt(newContent, "Test-Title"), 0), "Sincere Friend's Record");
            // Add a new note
            noteID = db.addNote(accountID, dbNote);
            // Check if this note is added successfully
            note = db.getNoteDao().getNoteWithCategory(accountID, noteID);
            assertEquals(dbNote.getTitle(), note.getTitle());
            assertEquals(dbNote.getContent(), note.getContent());
            assertEquals(dbNote.getCategory(), note.getCategory());
            assertEquals(dbNote.getAccountId(), note.getAccountId());

            // Test the rest case of getNotesCustom - ORDER BY ~ null, LIMIT ~ not null
            Method getNCWOW = NotesDatabase.class.getDeclaredMethod("getNotesCustom", long.class, String.class, String[].class, String.class, String.class, boolean.class);
            getNCWOW.setAccessible(true);
            int aSize = 1;
            notes = (List<NoteWithCategory>) getNCWOW.invoke(db, accountID, " status != ? AND accountId = ?",
                    new String[]{DBStatus.LOCAL_DELETED.getTitle(), "" + accountID}, null, String.valueOf(aSize), false);
            assertEquals(aSize, notes.size());
        } catch (Exception e) {
            fail(Arrays.toString(e.getStackTrace()));
            Log.e("Test_02_addNote_DBNote", Arrays.toString(e.getStackTrace()));
        }
    }

    @Test
    @Ignore
    public void test_03_searchNotes() {
        long thisAccountID = account.getId();
        List<Note> notes = db.getNoteDao().searchNotes(thisAccountID, null, null, false, null);  // All three added notes are marked as favorite
        Log.i("Test_03_searchNotes_Favorite_false", "Size: " + notes.size());
        assertEquals(notes.size(), 0);

        notes = db.getNoteDao().searchNotes(thisAccountID, null, "Hello", true, null); // There is no category named "Hello"
        Log.i("Test_03_searchNotes_Category_Hello", "Size: " + notes.size());
        assertEquals(notes.size(), 0);

        notes = db.getNoteDao().searchNotes(thisAccountID, null, "Diary", true, null); // There is one category named "Diary"
        Log.i("Test_03_searchNotes_Category_Diary_Favorite_True", "Size: " + notes.size());
        assertEquals(notes.size(), 1);

        notes = db.getNoteDao().searchNotes(thisAccountID, null, null, null, null);    // Fetch all notes
        Log.i("Test_03_searchNotes_Three_NULL", "Size: " + notes.size());
        assertEquals(notes.size(), 4);  // We've added three test notes by now
    }

    @Test
    public void test_04_getCategories() {
        List<NavigationItem.CategoryNavigationItem> categories = convertToCategoryNavigationItem(context, db.getCategoryDao().getCategories(account.getId()));
        boolean exitFlag = false;
        for (NavigationItem categoryItem : categories) {
            Log.i("Test_04_getCategories_Item", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
            if (categoryItem.label.equals("Diary")) {
                exitFlag = true;
            }
        }
        assertTrue(exitFlag);
    }

    @Test
    @Ignore
    public void test_05_searchCategories() {
        List<NavigationItem.CategoryNavigationItem> categories = convertToCategoryNavigationItem(context, db.getCategoryDao().searchCategories(account.getId(), "Dia"));
        boolean exitFlag = false;
        for (NavigationItem categoryItem : categories) {
            Log.i("Test_05_searchCategories_Dia", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
            if (categoryItem.label.equals("Diary")) {
                exitFlag = true;
            }
        }
        assertTrue(exitFlag);

        categories = convertToCategoryNavigationItem(context, db.getCategoryDao().searchCategories(account.getId(), "Mike Chester Wang"));
        exitFlag = false;
        for (NavigationItem categoryItem : categories) {
            Log.i("Test_05_searchCategories_Item_Mike_Chester_Wang", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
            if (categoryItem.label.equals("Diary")) {
                exitFlag = true;
            }
        }
        assertFalse(exitFlag);
    }

    @Test
    public void test_06_deleteNote() {
        long thisAccountID = account.getId();
        List<Note> notes = db.getNoteDao().getNotes(thisAccountID);
        int added_size = notes.size();

        Log.i("Test_06_deleteNote_All_Before_Deletion", "Size: " + added_size);
        int counter = 0;
        for (Note cnote : notes) {
            Log.i("Test_06_deleteNote_All_Before_Deletion", cnote.toString());
            // Delete the note after testing
            db.deleteNote(cnote.getId(), cnote.getStatus());
            counter++;
        }

        // Check if the note is deleted successfully
        notes = db.getNoteDao().getNotes(thisAccountID);
        int deleted_size = notes.size();
        assertEquals(counter, added_size - deleted_size);
        Log.i("Test_06_deleteNote_All_Notes_After_Deletion", "Size: " + deleted_size);
    }

    @Test
    public void test_07_multiAddNote() {
        long thisAccountID = account.getId();
        ArrayList<NoteWithCategory> multiCloudNote = new ArrayList<>();
        multiCloudNote.add(new NoteWithCategory(new Note(1L, Calendar.getInstance(),
                "Mike is so cool.", "Mike is a cool guy you know",
                true, null), "The BiBle"));
        multiCloudNote.add(new NoteWithCategory(new Note(1L, Calendar.getInstance(),
                "Andy is so cool.", "Andy is a cool guy you know",
                true, null), "The BiBle"));
        multiCloudNote.add(new NoteWithCategory(new Note(1L, Calendar.getInstance(),
                "I Honestly Love You", "I Honestly Love You by Leslie",
                true, null), "Music"));
        multiCloudNote.add(new NoteWithCategory(new Note(1L, Calendar.getInstance(),
                "Monica", "Monica by Leslie",
                true, null), "Music"));
        multiCloudNote.add(new NoteWithCategory(new Note(1L, Calendar.getInstance(),
                "Worksheet", "1 2 3 4 5 6 7 8",
                false, null), "Work"));
        multiCloudNote.add(new NoteWithCategory(new Note(1L, Calendar.getInstance(),
                "PowerPoint.", "8 7 6 5 4 3 2 1",
                false, null), "Work"));
        multiCloudNote.add(new NoteWithCategory(new Note(1L, Calendar.getInstance(),
                "Farewell My Concubine", "a great movie",
                true, null), "Movie"));
        multiCloudNote.add(new NoteWithCategory(new Note(1L, Calendar.getInstance(),
                "Leon", "an amazing movie",
                true, null), "Movie"));
        multiCloudNote.add(new NoteWithCategory(new Note(1L, Calendar.getInstance(),
                "The Dark Knight", "another amazing movie",
                true, null), "Movies"));
        multiCloudNote.add(new NoteWithCategory(new Note(1L, Calendar.getInstance(),
                "How are you.", "i am fine.",
                false, null), "Diary"));

        // Pre-check
        List<Note> notes = db.getNoteDao().getNotes(thisAccountID);
        int pre_size = notes.size();
        Log.i("Test_07_multiAddNote_All_Notes_Before_Addition", "Size: " + pre_size);

        long[] multiNoteID = new long[10];
        for (int i = 0; i < 10; ++i) {
            multiNoteID[i] = db.addNote(thisAccountID, multiCloudNote.get(i));
        }

        // check if the node added successfully
        for (int i = 0; i < 10; ++i) {
            NoteWithCategory nodeTemp = db.getNoteDao().getNoteWithCategory(thisAccountID, multiNoteID[i]);
            assertEquals(nodeTemp.getTitle(), multiCloudNote.get(i).getTitle());
            assertEquals(nodeTemp.getCategory(), multiCloudNote.get(i).getCategory());
            assertEquals(nodeTemp.getContent(), multiCloudNote.get(i).getContent());
            assertEquals(nodeTemp.getAccountId().longValue(), thisAccountID);
            Log.i("Test_07_multiAddNote_All_Notes_Addition_sucessful", nodeTemp.toString());
        }

        // check if these note is in all notes
        notes = db.getNoteDao().getNotes(thisAccountID);
        int add_size = notes.size();
        assertEquals(10, add_size - pre_size);

        Log.i("Test_07_multiAddNote_All_Notes_After_Addition", "Size: " + add_size);
    }

    @Test
    @Ignore
    public void test_08_multiSearchNotes() {
        long thisAccountID = account.getId();
        List<Note> notes = db.getNoteDao().searchNotes(thisAccountID, null, null, null, null);
        Log.i("Test_08_multiSearchNotes_null_null_null", "Size: " + notes.size());
        assertEquals(notes.size(), 10);

        notes = db.getNoteDao().searchNotes(thisAccountID, null, null, true, null);
        Log.i("Test_08_multiSearchNotes_null_null_true", "Size: " + notes.size());
        assertEquals(notes.size(), 7);

        notes = db.getNoteDao().searchNotes(thisAccountID, null, "Music", null, null);
        Log.i("Test_08_multiSearchNotes_null_Music_null", "Size: " + notes.size());
        assertEquals(notes.size(), 2);

        notes = db.getNoteDao().searchNotes(thisAccountID, null, "Work", true, null);
        Log.i("Test_08_multiSearchNotes_null_Work_true", "Size: " + notes.size());
        assertEquals(notes.size(), 0);

        notes = db.getNoteDao().searchNotes(thisAccountID, null, "Diary", null, null);
        Log.i("Test_08_multiSearchNotes_null_Diary_null", "Size: " + notes.size());
        assertEquals(notes.size(), 1);

        notes = db.getNoteDao().searchNotes(thisAccountID, "Mike", null, null, null);
        Log.i("Test_08_multiSearchNotes_Mike_null_null", "Size: " + notes.size());
        assertEquals(notes.size(), 1);
    }

    @Test
    public void test_09_multiGetCategories() {
        List<NavigationItem.CategoryNavigationItem> categories = convertToCategoryNavigationItem(context, db.getCategoryDao().getCategories(account.getId()));
        int count = 0;
        for (NavigationItem categoryItem : categories) {
            Log.i("Test_09_multiGetCategories_Item", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
            count++;
        }
        Log.i("count count count", "count " + count);
        assertEquals(6, count);
        Log.i("count count count", "count " + count);
    }

    @Test
    @Ignore
    public void test_10_multiSearchCategories() {
        List<NavigationItem.CategoryNavigationItem> categories = convertToCategoryNavigationItem(context, db.getCategoryDao().searchCategories(account.getId(), "M"));
        int count = 0;
        for (NavigationItem categoryItem : categories) {
            Log.i("Test_10_multiSearchCategories_Item", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
            count++;
        }
        assertEquals(3, count);

        categories = convertToCategoryNavigationItem(context, db.getCategoryDao().searchCategories(account.getId(), "Mike"));
        count = 0;
        for (NavigationItem categoryItem : categories) {
            Log.i("Test_10_multiSearchCategories_Item", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
            count++;
        }
        assertEquals(0, count);

        categories = convertToCategoryNavigationItem(context, db.getCategoryDao().searchCategories(account.getId(), "M"));
        boolean exitFlag = false;
        for (NavigationItem categoryItem : categories) {
            Log.i("Test_10_multiSearchCategories_Item", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
            if (categoryItem.label.equals("Music")) {
                exitFlag = true;
            }
        }
        assertTrue(exitFlag);


        categories = convertToCategoryNavigationItem(context, db.getCategoryDao().searchCategories(account.getId(), "WOk"));
        exitFlag = false;
        for (NavigationItem categoryItem : categories) {
            Log.i("Test_10_multiSearchCategories_Item", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
            if (categoryItem.label.equals("Diary")) {
                exitFlag = true;
            }
        }
        assertFalse(exitFlag);

        categories = convertToCategoryNavigationItem(context, db.getCategoryDao().searchCategories(account.getId(), "hello"));
        exitFlag = false;
        for (NavigationItem categoryItem : categories) {
            Log.i("Test_10_multiSearchCategories_Item", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
            if (categoryItem.label.equals("Diary")) {
                exitFlag = true;
            }
        }
        assertFalse(exitFlag);
    }

    @Test
    public void test_11_multiDeleteNote() {
        long thisAccountID = account.getId();
        List<Note> notes = db.getNoteDao().getNotes(thisAccountID);
        int added_size = notes.size();

        Log.i("Test_11_multiDeleteNote_All_Before_Deletion", "Size: " + added_size);
        for (Note e : notes) {
            Log.i("Test_11_multiDeleteNote_All_Before_Deletion", e.toString());
            db.deleteNote(e.getId(), e.getStatus());
        }

        // Check if the note is deleted successfully
        notes = db.getNoteDao().getNotes(thisAccountID);
        int deleted_size = notes.size();
        assertEquals(10, added_size - deleted_size);
        Log.i("Test_11_multiDeleteNote_All_After_Deletion", "Size: " + deleted_size);
    }

    @Test
    @Ignore
    public void test_12_Chinese() {
        long accountID = account.getId();
        NoteWithCategory cloudNote = new NoteWithCategory(new Note(1L, Calendar.getInstance(),
                "美好的一天", getCurDate() + " 兄弟，这真是美好的一天。",
                true, null), "日记");

        // Pre-check
        List<Note> notes = db.getNoteDao().getNotes(accountID);
        int pre_size = notes.size();
        Log.i("Test_12_Chinese_All_Notes_Before_Addition", "Size: " + pre_size);

        // Add a new note
        long noteID = db.addNote(accountID, cloudNote);
        // Check if this note is added successfully
        NoteWithCategory note = db.getNoteDao().getNoteWithCategory(accountID, noteID);
        Log.i("Test_12_Chinese_Cur_Note", note.toString());
        Log.i("Test_12_Chinese_Cur_Note", "Title: " + note.getTitle());
        Log.i("Test_12_Chinese_Cur_Note", "Content: " + note.getContent());
        Log.i("Test_12_Chinese_Cur_Note", "Category: " + note.getCategory());

        assertEquals("美好的一天", note.getTitle());
        assertEquals(cloudNote.getContent(), note.getContent());
        assertEquals("日记", note.getCategory());
        assertEquals(accountID, note.getAccountId().longValue());

        // Check if this note is in all notes
        notes = db.getNoteDao().getNotes(accountID);
        int added_size = notes.size();

        assertEquals(1, added_size - pre_size);

        Log.i("Test_12_Chinese_All_Notes_Added", "Size: " + added_size);
        for (Note cnote : notes) {
            Log.i("Test_12_Chinese_All_Notes_Added", cnote.toString());
        }

        long thisAccountID = account.getId();
        notes = db.getNoteDao().searchNotes(thisAccountID, "美好", "日记", true, null);
        Log.i("Test_12_Chinese", "Size: " + notes.size());
        assertEquals(1, notes.size());

        List<NavigationItem.CategoryNavigationItem> categories = convertToCategoryNavigationItem(context, db.getCategoryDao().getCategories(account.getId()));
        boolean exitFlag = false;
        for (NavigationItem categoryItem : categories) {
            Log.i("Test_12_Chinese_Item", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
            if (categoryItem.label.equals("日记")) {
                exitFlag = true;
            }
        }
        assertTrue(exitFlag);

        categories = convertToCategoryNavigationItem(context, db.getCategoryDao().searchCategories(account.getId(), "记"));
        exitFlag = false;
        for (NavigationItem categoryItem : categories) {
            Log.i("Test_12_Chinese_Item", String.format("%s | %s | %d | %d", categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
            if (categoryItem.label.equals("日记")) {
                exitFlag = true;
            }
        }
        assertTrue(exitFlag);

        notes = db.getNoteDao().getNotes(thisAccountID);
        for (Note cnote : notes) {
            Log.i("Test_12_Chinese_All_Before_Deletion", cnote.toString());
            // Delete the note after testing
            db.deleteNote(cnote.getId(), cnote.getStatus());
        }

        // Check if the note is deleted successfully
        notes = db.getNoteDao().getNotes(thisAccountID);
        int deleted_size = notes.size();
        assertEquals(1, added_size - deleted_size);
        Log.i("Test_12_Chinese_All_Notes_After_Deletion", "Size: " + deleted_size);
    }

    @Test
    @Ignore
    public void test_13_getCategoryIdByTitle() {
        try {
            List<NavigationItem.CategoryNavigationItem> categories = convertToCategoryNavigationItem(context, db.getCategoryDao().getCategories(account.getId()));
            int count = 0;
            for (NavigationItem categoryItem : categories) {
                Log.i("Test_13_getCategoryIdByTitle", String.format("%s | %s | %d | %d",
                        categoryItem.id, categoryItem.label, categoryItem.count, categoryItem.icon));
                count++;
            }
            Log.i("Test_13_getCategoryIdByTitle", "count " + count);

            long catID;

            // Find an existing category to test false
            if (count > 0) {
                catID = db.getCategoryDao().getCategoryIdByTitle(account.getId(), categories.get(0).label);
                assertNotEquals(-1, catID);
            }

            // Create a category not existing
            String cur_cat = "Mike Chester Wang's Diary" + getCurDate();
            catID = db.getCategoryDao().getCategoryIdByTitle(account.getId(), cur_cat);
            assertNotEquals(-1, catID);
        } catch (Exception e) {
            fail(Arrays.toString(e.getStackTrace()));
            Log.e("Test_13_getCategoryIdByTitle", Arrays.toString(e.getStackTrace()));
        }
    }

//    @Test
//    public void test_14_upgrade() {
//        SQLiteDatabase sqlite_db = db.getReadableDatabase();
//        Cursor cursor = sqlite_db.rawQuery("SELECT * FROM " + AbstractNotesDatabase.table_category, null);
//        cursor.moveToNext();
//        int sorting_method = cursor.getInt(3);
//        Log.i("TEST_14_UPGRADE", "sorting method index: " + sorting_method);
//        assertEquals(0, sorting_method);
//    }

    @Test
    @Ignore
    public void test_15_getAndModifyCategoryOrderByTitle() {
        // add a note to database
        NoteWithCategory cloudNote = new NoteWithCategory(new Note(1L, Calendar.getInstance(),
                "A Coding Day", "This is a day which is very suitable to code.",
                true, null), "CodingDiary");
        long noteID = db.addNote(account.getId(), cloudNote);

        // check the default value of ordering_method
        CategorySortingMethod defaultMethod = db.getCategoryDao().getCategoryOrder(1L);
        assertEquals(defaultMethod, CategorySortingMethod.getCSM(0));

        // modify the value of ordering_method and check
        db.getCategoryDao().modifyCategoryOrder(account.getId(), 1L, CategorySortingMethod.getCSM(1));
        CategorySortingMethod methodAfterModify = db.getCategoryDao().getCategoryOrder(1L);
        assertEquals(methodAfterModify, CategorySortingMethod.getCSM(1));

        // delete the Node
        db.deleteNote(noteID, DBStatus.VOID);
    }

    @Test
    @Ignore
    public void test_16_getAndModifyCategoryOrder() {
        // Normal categories
        // add a note to database
        NoteWithCategory cloudNote = new NoteWithCategory(new Note(1L, Calendar.getInstance(),
                "A Coding Day", "This is a day which is very suitable to code.",
                true, null), "CodingDiary");
        long noteID = db.addNote(account.getId(), cloudNote);

        // check the default value of ordering_method
        CategorySortingMethod defaultMethod = db.getCategoryOrder(new NavigationCategory(db.getCategoryDao().getCategoryByTitle(1L, "CodingDiary")));
        assertEquals(defaultMethod, CategorySortingMethod.getCSM(0));

        // modify the value of ordering_method and check
        db.getCategoryDao().modifyCategoryOrder(account.getId(), 1L, CategorySortingMethod.getCSM(1));
        CategorySortingMethod methodAfterModify = db.getCategoryOrder(new NavigationCategory(db.getCategoryDao().getCategoryByTitle(1L, "CodingDiary")));
        assertEquals(methodAfterModify, CategorySortingMethod.getCSM(1));

        // delete the Node
        db.deleteNote(noteID, DBStatus.VOID);

        // Special categories
        Context ctx = db.getContext().getApplicationContext();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor spe = sp.edit();
        spe.clear();
        spe.apply();
        // check default value
        // all notes
        defaultMethod = db.getCategoryOrder(new NavigationCategory(RECENT));
        assertEquals(defaultMethod, CategorySortingMethod.getCSM(0));

        // uncategorized
        defaultMethod = db.getCategoryOrder(new NavigationCategory(UNCATEGORIZED));
        assertEquals(defaultMethod, CategorySortingMethod.getCSM(0));

        // favorite
        defaultMethod = db.getCategoryOrder(new NavigationCategory(FAVORITES));
        assertEquals(defaultMethod, CategorySortingMethod.getCSM(0));

        // modify the value of ordering_method and check
        // all notes
        db.modifyCategoryOrder(account.getId(), new NavigationCategory(RECENT), CategorySortingMethod.getCSM(1));
        methodAfterModify = db.getCategoryOrder(new NavigationCategory(RECENT));
        assertEquals(methodAfterModify, CategorySortingMethod.getCSM(1));

        // uncategorized
        db.modifyCategoryOrder(account.getId(), new NavigationCategory(UNCATEGORIZED), CategorySortingMethod.getCSM(1));
        methodAfterModify = db.getCategoryOrder(new NavigationCategory(UNCATEGORIZED));
        assertEquals(methodAfterModify, CategorySortingMethod.getCSM(1));

        // favorite
        db.modifyCategoryOrder(account.getId(), new NavigationCategory(FAVORITES), CategorySortingMethod.getCSM(1));
        methodAfterModify = db.getCategoryOrder(new NavigationCategory(FAVORITES));
        assertEquals(methodAfterModify, CategorySortingMethod.getCSM(1));

        // delete SharedPreferences
        spe.clear();
        spe.apply();
    }

    public static String getCurDate() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
    }
}