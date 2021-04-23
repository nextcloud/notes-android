package it.niedermann.owncloud.notes.persistence;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;

import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.Capabilities;

import static it.niedermann.owncloud.notes.persistence.NotesDatabaseTestUtil.getOrAwaitValue;
import static it.niedermann.owncloud.notes.shared.model.DBStatus.LOCAL_DELETED;
import static it.niedermann.owncloud.notes.shared.model.DBStatus.LOCAL_EDITED;
import static it.niedermann.owncloud.notes.shared.model.DBStatus.VOID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.P})
public class NotesRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @NonNull
    private Context context;
    private NotesRepository repoMock = null;
    private Account account = null;
    private Account secondAccount = null;

    @Before
    public void setupDB() throws NextcloudHttpRequestFailedException {
        context = ApplicationProvider.getApplicationContext();
        repoMock = Mockito.mock(NotesRepository.class);
        final NotesDatabase db = Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), NotesDatabase.class)
                .allowMainThreadQueries()
                .build();
        Mockito.when(repoMock.db.getNoteDao()).thenReturn(db.getNoteDao());

        repoMock.addAccount("https://äöüß.example.com", "彼得", "彼得@äöüß.example.com", new Capabilities("{\"ocs\":{\"meta\":{\"status\":\"ok\",\"statuscode\":200,\"message\":\"OK\"},\"data\":{\"version\":{\"major\":18,\"minor\":0,\"micro\":4,\"string\":\"18.0.4\",\"edition\":\"\",\"extendedSupport\":false},\"capabilities\":{\"core\":{\"pollinterval\":60,\"webdav-root\":\"remote.php\\/webdav\"},\"bruteforce\":{\"delay\":0},\"files\":{\"bigfilechunking\":true,\"blacklisted_files\":[\".htaccess\"],\"directEditing\":{\"url\":\"https:\\/\\/efss.qloud.my\\/ocs\\/v2.php\\/apps\\/files\\/api\\/v1\\/directEditing\",\"etag\":\"ed2b141af2a39b0e42666952ba60988d\"},\"versioning\":true,\"undelete\":true},\"activity\":{\"apiv2\":[\"filters\",\"filters-api\",\"previews\",\"rich-strings\"]},\"ocm\":{\"enabled\":true,\"apiVersion\":\"1.0-proposal1\",\"endPoint\":\"https:\\/\\/efss.qloud.my\\/index.php\\/ocm\",\"resourceTypes\":[{\"name\":\"file\",\"shareTypes\":[\"user\",\"group\"],\"protocols\":{\"webdav\":\"\\/public.php\\/webdav\\/\"}}]},\"deck\":{\"version\":\"0.8.2\"},\"richdocuments\":{\"mimetypes\":[\"application\\/vnd.oasis.opendocument.text\",\"application\\/vnd.oasis.opendocument.spreadsheet\",\"application\\/vnd.oasis.opendocument.graphics\",\"application\\/vnd.oasis.opendocument.presentation\",\"application\\/vnd.lotus-wordpro\",\"application\\/vnd.visio\",\"application\\/vnd.wordperfect\",\"application\\/msonenote\",\"application\\/msword\",\"application\\/rtf\",\"text\\/rtf\",\"application\\/vnd.openxmlformats-officedocument.wordprocessingml.document\",\"application\\/vnd.openxmlformats-officedocument.wordprocessingml.template\",\"application\\/vnd.ms-word.document.macroEnabled.12\",\"application\\/vnd.ms-word.template.macroEnabled.12\",\"application\\/vnd.ms-excel\",\"application\\/vnd.openxmlformats-officedocument.spreadsheetml.sheet\",\"application\\/vnd.openxmlformats-officedocument.spreadsheetml.template\",\"application\\/vnd.ms-excel.sheet.macroEnabled.12\",\"application\\/vnd.ms-excel.template.macroEnabled.12\",\"application\\/vnd.ms-excel.addin.macroEnabled.12\",\"application\\/vnd.ms-excel.sheet.binary.macroEnabled.12\",\"application\\/vnd.ms-powerpoint\",\"application\\/vnd.openxmlformats-officedocument.presentationml.presentation\",\"application\\/vnd.openxmlformats-officedocument.presentationml.template\",\"application\\/vnd.openxmlformats-officedocument.presentationml.slideshow\",\"application\\/vnd.ms-powerpoint.addin.macroEnabled.12\",\"application\\/vnd.ms-powerpoint.presentation.macroEnabled.12\",\"application\\/vnd.ms-powerpoint.template.macroEnabled.12\",\"application\\/vnd.ms-powerpoint.slideshow.macroEnabled.12\",\"text\\/csv\"],\"mimetypesNoDefaultOpen\":[\"image\\/svg+xml\",\"application\\/pdf\",\"text\\/plain\",\"text\\/spreadsheet\"],\"collabora\":[],\"direct_editing\":false,\"templates\":false,\"productName\":\"\\u5728\\u7ebf\\u534f\\u4f5c\"},\"dav\":{\"chunking\":\"1.0\"},\"files_sharing\":{\"api_enabled\":true,\"public\":{\"enabled\":true,\"password\":{\"enforced\":true,\"askForOptionalPassword\":false},\"expire_date\":{\"enabled\":true,\"days\":\"7\",\"enforced\":false},\"multiple_links\":true,\"expire_date_internal\":{\"enabled\":false},\"send_mail\":false,\"upload\":true,\"upload_files_drop\":true},\"resharing\":true,\"user\":{\"send_mail\":false,\"expire_date\":{\"enabled\":true}},\"group_sharing\":true,\"group\":{\"enabled\":true,\"expire_date\":{\"enabled\":true}},\"default_permissions\":31,\"federation\":{\"outgoing\":false,\"incoming\":false,\"expire_date\":{\"enabled\":true}},\"sharee\":{\"query_lookup_default\":false},\"sharebymail\":{\"enabled\":true,\"upload_files_drop\":{\"enabled\":true},\"password\":{\"enabled\":true},\"expire_date\":{\"enabled\":true}}},\"external\":{\"v1\":[\"sites\",\"device\",\"groups\",\"redirect\"]},\"notifications\":{\"ocs-endpoints\":[\"list\",\"get\",\"delete\",\"delete-all\",\"icons\",\"rich-strings\",\"action-web\"],\"push\":[\"devices\",\"object-data\",\"delete\"],\"admin-notifications\":[\"ocs\",\"cli\"]},\"password_policy\":{\"minLength\":8,\"enforceNonCommonPassword\":true,\"enforceNumericCharacters\":false,\"enforceSpecialCharacters\":false,\"enforceUpperLowerCase\":false,\"api\":{\"generate\":\"https:\\/\\/efss.qloud.my\\/ocs\\/v2.php\\/apps\\/password_policy\\/api\\/v1\\/generate\",\"validate\":\"https:\\/\\/efss.qloud.my\\/ocs\\/v2.php\\/apps\\/password_policy\\/api\\/v1\\/validate\"}},\"theming\":{\"name\":\"QloudData\",\"url\":\"https:\\/\\/www.qloud.my\\/qloud-data\\/\",\"slogan\":\"Powered by NextCloud\",\"color\":\"#1E4164\",\"color-text\":\"#ffffff\",\"color-element\":\"#1E4164\",\"logo\":\"https:\\/\\/efss.qloud.my\\/index.php\\/apps\\/theming\\/image\\/logo?useSvg=1&v=47\",\"background\":\"https:\\/\\/efss.qloud.my\\/core\\/img\\/background.png?v=47\",\"background-plain\":false,\"background-default\":true,\"logoheader\":\"https:\\/\\/efss.qloud.my\\/index.php\\/apps\\/theming\\/image\\/logo?useSvg=1&v=47\",\"favicon\":\"https:\\/\\/efss.qloud.my\\/index.php\\/apps\\/theming\\/image\\/logo?useSvg=1&v=47\"},\"registration\":{\"enabled\":true,\"apiRoot\":\"\\/ocs\\/v2.php\\/apps\\/registration\\/api\\/v1\\/\",\"apiLevel\":\"v1\"}}}}}", null));
        account = repoMock.getAccountDao().getAccountByName("彼得@äöüß.example.com");

        repoMock.addAccount("https://example.org", "test", "test@example.org", new Capabilities("{ocs: {}}", null));
        secondAccount = repoMock.getAccountDao().getAccountByName("test@example.org");

        Arrays.stream(new Note[]{
                new Note(1, 1001L, Calendar.getInstance(), "美好的一天", "C", "Movies", false, null, VOID, account.getId(), "", 0),
                new Note(2, null, Calendar.getInstance(), "T", "C", "Movies", false, null, LOCAL_EDITED, account.getId(), "", 0),
                new Note(3, 1003L, Calendar.getInstance(), "美好的一天", "C", "Movies", false, null, LOCAL_EDITED, account.getId(), "", 0),
                new Note(4, null, Calendar.getInstance(), "T", "C", "Music", false, null, VOID, account.getId(), "", 0),
                new Note(5, 1005L, Calendar.getInstance(), "美好的一天", "C", " 兄弟，这真是美好的一天。", false, null, LOCAL_EDITED, account.getId(), "", 0),
                new Note(6, 1006L, Calendar.getInstance(), "美好的一天", "C", " 兄弟，这真是美好的一天。", false, null, LOCAL_DELETED, account.getId(), "", 0),
                new Note(7, null, Calendar.getInstance(), "T", "C", "Music", true, null, LOCAL_EDITED, secondAccount.getId(), "", 0),
                new Note(8, 1008L, Calendar.getInstance(), "美好的一天", "C", "ToDo", true, null, LOCAL_EDITED, secondAccount.getId(), "", 0),
                new Note(9, 1009L, Calendar.getInstance(), "美好的一天", "C", "ToDo", true, null, LOCAL_DELETED, secondAccount.getId(), "", 0)
        }).forEach(note -> repoMock.getNoteDao().addNote(note));
    }

    @After
    public void closeDb() {
        repoMock.close();
    }

    @Test
    public void testGetIdMap() {
        final Map<Long, Long> idMapOfFirstAccount = repoMock.getIdMap(account.getId());
        assertEquals(3, idMapOfFirstAccount.size());
        assertEquals(Long.valueOf(1L), idMapOfFirstAccount.get(1001L));
        assertEquals(Long.valueOf(3L), idMapOfFirstAccount.get(1003L));
        assertEquals(Long.valueOf(5L), idMapOfFirstAccount.get(1005L));

        final Map<Long, Long> idMapOfSecondAccount = repoMock.getIdMap(secondAccount.getId());
        assertEquals(1, idMapOfSecondAccount.size());
        assertEquals(Long.valueOf(8L), idMapOfSecondAccount.get(1008L));
    }

    @Test
    public void testAddAccount() throws NextcloudHttpRequestFailedException, InterruptedException {
        final Account createdAccount = getOrAwaitValue(repoMock.addAccount("https://äöüß.example.com", "彼得", "彼得@äöüß.example.com", new Capabilities("{ocs: {}}", null)));
        assertEquals("https://äöüß.example.com", createdAccount.getUrl());
        assertEquals("彼得", createdAccount.getUserName());
        assertEquals("彼得@äöüß.example.com", createdAccount.getAccountName());
    }

    @Test
    public void testAddNote() {
        final Note localNote = new Note(null, Calendar.getInstance(), "Fancy Title", "MyContent", "Samples", false, "123");
        localNote.setId(99);
        final Note createdNoteFromLocal = repoMock.addNote(account.getId(), localNote);
        assertEquals(LOCAL_EDITED, createdNoteFromLocal.getStatus());
        assertEquals("MyContent", createdNoteFromLocal.getExcerpt());

        final Note createdNoteFromRemote = repoMock.addNote(account.getId(), new Note(null, Calendar.getInstance(), "Fancy Title", "MyContent", "Samples", false, "123"));
        assertEquals(VOID, createdNoteFromRemote.getStatus());
        assertEquals("MyContent", createdNoteFromRemote.getExcerpt());
    }

    @Test
    public void updateApiVersion() {
        assertThrows(IllegalArgumentException.class, () -> repoMock.updateApiVersion(account.getId(), ""));
        assertThrows(IllegalArgumentException.class, () -> repoMock.updateApiVersion(account.getId(), "asdf"));
        assertThrows(IllegalArgumentException.class, () -> repoMock.updateApiVersion(account.getId(), "{}"));

        repoMock.updateApiVersion(account.getId(), null);
        assertNull(repoMock.getAccountDao().getAccountById(account.getId()).getApiVersion());
        repoMock.updateApiVersion(account.getId(), "[]");
        assertNull(repoMock.getAccountDao().getAccountById(account.getId()).getApiVersion());

        repoMock.updateApiVersion(account.getId(), "[1.0]");
        assertEquals("[1.0]", repoMock.getAccountDao().getAccountById(account.getId()).getApiVersion());
        repoMock.updateApiVersion(account.getId(), "[0.2, 1.0]");
        assertEquals("[0.2, 1.0]", repoMock.getAccountDao().getAccountById(account.getId()).getApiVersion());

        // TODO is this really indented?
        repoMock.updateApiVersion(account.getId(), "[0.2, abc]");
        assertEquals("[0.2, abc]", repoMock.getAccountDao().getAccountById(account.getId()).getApiVersion());
    }

    @Test
    @Ignore("Need to find a way to stub deleteAndSync method")
    public void moveNoteToAnotherAccount() throws InterruptedException {
        final Note noteToMove = repoMock.getNoteDao().getNoteById(1);
        assertEquals(3, repoMock.getNoteDao().getLocalModifiedNotes(secondAccount.getId()).size());
        final Note movedNote = getOrAwaitValue(repoMock.moveNoteToAnotherAccount(secondAccount, noteToMove));
        assertEquals(4, repoMock.getNoteDao().getLocalModifiedNotes(secondAccount.getId()).size());
        assertEquals(LOCAL_EDITED, movedNote.getStatus());
        // TODO assert deleteAndSync has been called
    }
}