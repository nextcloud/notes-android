package it.niedermann.owncloud.notes.persistence;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import it.niedermann.owncloud.notes.shared.model.DBStatus;

import static it.niedermann.owncloud.notes.persistence.NotesDatabaseTestUtil.randomString;
import static it.niedermann.owncloud.notes.persistence.NotesDatabaseTestUtil.uniqueLong;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class NotesDaoTest {

    @NonNull
    private NotesDatabase db;
    private Account account;

    @Before
    public void setupDB() throws NextcloudHttpRequestFailedException {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), NotesDatabase.class).build();
        // Create a new account if not exist
        final String accountURL = "HelloHowAreYou";
        final String accountUserName = "John Doe";
        final String accountName = accountUserName + "@" + accountURL;
        final String response = "{\"ocs\":{\"meta\":{\"status\":\"ok\",\"statuscode\":200,\"message\":\"OK\"},\"data\":{\"version\":{\"major\":18,\"minor\":0,\"micro\":4,\"string\":\"18.0.4\",\"edition\":\"\",\"extendedSupport\":false},\"capabilities\":{\"core\":{\"pollinterval\":60,\"webdav-root\":\"remote.php\\/webdav\"},\"bruteforce\":{\"delay\":0},\"files\":{\"bigfilechunking\":true,\"blacklisted_files\":[\".htaccess\"],\"directEditing\":{\"url\":\"https:\\/\\/efss.qloud.my\\/ocs\\/v2.php\\/apps\\/files\\/api\\/v1\\/directEditing\",\"etag\":\"ed2b141af2a39b0e42666952ba60988d\"},\"versioning\":true,\"undelete\":true},\"activity\":{\"apiv2\":[\"filters\",\"filters-api\",\"previews\",\"rich-strings\"]},\"ocm\":{\"enabled\":true,\"apiVersion\":\"1.0-proposal1\",\"endPoint\":\"https:\\/\\/efss.qloud.my\\/index.php\\/ocm\",\"resourceTypes\":[{\"name\":\"file\",\"shareTypes\":[\"user\",\"group\"],\"protocols\":{\"webdav\":\"\\/public.php\\/webdav\\/\"}}]},\"deck\":{\"version\":\"0.8.2\"},\"richdocuments\":{\"mimetypes\":[\"application\\/vnd.oasis.opendocument.text\",\"application\\/vnd.oasis.opendocument.spreadsheet\",\"application\\/vnd.oasis.opendocument.graphics\",\"application\\/vnd.oasis.opendocument.presentation\",\"application\\/vnd.lotus-wordpro\",\"application\\/vnd.visio\",\"application\\/vnd.wordperfect\",\"application\\/msonenote\",\"application\\/msword\",\"application\\/rtf\",\"text\\/rtf\",\"application\\/vnd.openxmlformats-officedocument.wordprocessingml.document\",\"application\\/vnd.openxmlformats-officedocument.wordprocessingml.template\",\"application\\/vnd.ms-word.document.macroEnabled.12\",\"application\\/vnd.ms-word.template.macroEnabled.12\",\"application\\/vnd.ms-excel\",\"application\\/vnd.openxmlformats-officedocument.spreadsheetml.sheet\",\"application\\/vnd.openxmlformats-officedocument.spreadsheetml.template\",\"application\\/vnd.ms-excel.sheet.macroEnabled.12\",\"application\\/vnd.ms-excel.template.macroEnabled.12\",\"application\\/vnd.ms-excel.addin.macroEnabled.12\",\"application\\/vnd.ms-excel.sheet.binary.macroEnabled.12\",\"application\\/vnd.ms-powerpoint\",\"application\\/vnd.openxmlformats-officedocument.presentationml.presentation\",\"application\\/vnd.openxmlformats-officedocument.presentationml.template\",\"application\\/vnd.openxmlformats-officedocument.presentationml.slideshow\",\"application\\/vnd.ms-powerpoint.addin.macroEnabled.12\",\"application\\/vnd.ms-powerpoint.presentation.macroEnabled.12\",\"application\\/vnd.ms-powerpoint.template.macroEnabled.12\",\"application\\/vnd.ms-powerpoint.slideshow.macroEnabled.12\",\"text\\/csv\"],\"mimetypesNoDefaultOpen\":[\"image\\/svg+xml\",\"application\\/pdf\",\"text\\/plain\",\"text\\/spreadsheet\"],\"collabora\":[],\"direct_editing\":false,\"templates\":false,\"productName\":\"\\u5728\\u7ebf\\u534f\\u4f5c\"},\"dav\":{\"chunking\":\"1.0\"},\"files_sharing\":{\"api_enabled\":true,\"public\":{\"enabled\":true,\"password\":{\"enforced\":true,\"askForOptionalPassword\":false},\"expire_date\":{\"enabled\":true,\"days\":\"7\",\"enforced\":false},\"multiple_links\":true,\"expire_date_internal\":{\"enabled\":false},\"send_mail\":false,\"upload\":true,\"upload_files_drop\":true},\"resharing\":true,\"user\":{\"send_mail\":false,\"expire_date\":{\"enabled\":true}},\"group_sharing\":true,\"group\":{\"enabled\":true,\"expire_date\":{\"enabled\":true}},\"default_permissions\":31,\"federation\":{\"outgoing\":false,\"incoming\":false,\"expire_date\":{\"enabled\":true}},\"sharee\":{\"query_lookup_default\":false},\"sharebymail\":{\"enabled\":true,\"upload_files_drop\":{\"enabled\":true},\"password\":{\"enabled\":true},\"expire_date\":{\"enabled\":true}}},\"external\":{\"v1\":[\"sites\",\"device\",\"groups\",\"redirect\"]},\"notifications\":{\"ocs-endpoints\":[\"list\",\"get\",\"delete\",\"delete-all\",\"icons\",\"rich-strings\",\"action-web\"],\"push\":[\"devices\",\"object-data\",\"delete\"],\"admin-notifications\":[\"ocs\",\"cli\"]},\"password_policy\":{\"minLength\":8,\"enforceNonCommonPassword\":true,\"enforceNumericCharacters\":false,\"enforceSpecialCharacters\":false,\"enforceUpperLowerCase\":false,\"api\":{\"generate\":\"https:\\/\\/efss.qloud.my\\/ocs\\/v2.php\\/apps\\/password_policy\\/api\\/v1\\/generate\",\"validate\":\"https:\\/\\/efss.qloud.my\\/ocs\\/v2.php\\/apps\\/password_policy\\/api\\/v1\\/validate\"}},\"theming\":{\"name\":\"QloudData\",\"url\":\"https:\\/\\/www.qloud.my\\/qloud-data\\/\",\"slogan\":\"Powered by NextCloud\",\"color\":\"#1E4164\",\"color-text\":\"#ffffff\",\"color-element\":\"#1E4164\",\"logo\":\"https:\\/\\/efss.qloud.my\\/index.php\\/apps\\/theming\\/image\\/logo?useSvg=1&v=47\",\"background\":\"https:\\/\\/efss.qloud.my\\/core\\/img\\/background.png?v=47\",\"background-plain\":false,\"background-default\":true,\"logoheader\":\"https:\\/\\/efss.qloud.my\\/index.php\\/apps\\/theming\\/image\\/logo?useSvg=1&v=47\",\"favicon\":\"https:\\/\\/efss.qloud.my\\/index.php\\/apps\\/theming\\/image\\/logo?useSvg=1&v=47\"},\"registration\":{\"enabled\":true,\"apiRoot\":\"\\/ocs\\/v2.php\\/apps\\/registration\\/api\\/v1\\/\",\"apiLevel\":\"v1\"}}}}}";
        Capabilities capabilities = new Capabilities(response, null);
        assertNotNull(capabilities);
        db.addAccount(accountURL, accountUserName, accountName, capabilities);
        account = db.getAccountDao().getLocalAccountByAccountName(accountName);
    }

    @Test
    public void testUpdateIfNotModifiedLocallyDuringSync() {
//        final List<Note> notes = new ArrayList<>(10);
//
//        notes.add(new Note(uniqueLong(), uniqueLong(), Calendar.getInstance(), randomString(10), randomString(50), randomString(5), true, null, DBStatus.VOID, account.getId(), "", 0));
//        notes.add(new Note(uniqueLong(), uniqueLong(), Calendar.getInstance(), randomString(10), randomString(50), randomString(5), true, null, DBStatus.VOID, account.getId(), "", 0));
//        notes.add(new Note(uniqueLong(), uniqueLong(), Calendar.getInstance(), randomString(10), randomString(50), randomString(5), true, null, DBStatus.VOID, account.getId(), "", 0));
//        notes.add(new Note(uniqueLong(), uniqueLong(), Calendar.getInstance(), randomString(10), randomString(50), randomString(5), true, null, DBStatus.VOID, account.getId(), "", 0));
//        notes.add(new Note(uniqueLong(), uniqueLong(), Calendar.getInstance(), randomString(10), randomString(50), randomString(5), true, null, DBStatus.LOCAL_EDITED, account.getId(), "", 0));
//        notes.add(new Note(uniqueLong(), uniqueLong(), Calendar.getInstance(), randomString(10), randomString(50), randomString(5), false, null, DBStatus.LOCAL_EDITED, account.getId(), "", 0));
//        notes.add(new Note(uniqueLong(), uniqueLong(), Calendar.getInstance(), randomString(10), randomString(50), randomString(5), false, null, DBStatus.LOCAL_EDITED, account.getId(), "", 0));
//        notes.add(new Note(uniqueLong(), uniqueLong(), Calendar.getInstance(), randomString(10), randomString(50), randomString(5), false, null, DBStatus.LOCAL_DELETED, account.getId(), "", 0));
//        notes.add(new Note(uniqueLong(), uniqueLong(), Calendar.getInstance(), randomString(10), randomString(50), randomString(5), false, null, DBStatus.LOCAL_DELETED, account.getId(), "", 0));
//        notes.add(new Note(uniqueLong(), uniqueLong(), Calendar.getInstance(), randomString(10), randomString(50), randomString(5), false, null, DBStatus.LOCAL_DELETED, account.getId(), "", 0));
//
//        for (Note note : notes) {
//            db.getNoteDao().addNote(note);
//        }
////        long id, Long modified, String title, boolean favorite, String eTag, String content, String excerpt, String oldContent, String oldCategory, boolean oldFavorite);
//        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyDuringSync(notes.get(0).getId(), Calendar.getInstance().getTimeInMillis(), notes.get(0).getTitle(), true, null, notes.get(0).getContent(), notes.get(0).getExcerpt(), notes.get(0).getContent(), notes.get(0).getCategory(), notes.get(0).getFavorite()));
    }
}