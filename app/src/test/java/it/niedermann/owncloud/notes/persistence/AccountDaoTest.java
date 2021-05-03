package it.niedermann.owncloud.notes.persistence;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.shared.model.Capabilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.P})
public class AccountDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @NonNull
    private NotesDatabase db;

    @Before
    public void setupDB() {
        db = Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), NotesDatabase.class)
                .allowMainThreadQueries()
                .build();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void insertAccount() throws NextcloudHttpRequestFailedException {
        final long createdId = db.getAccountDao().insert(new Account("https://äöüß.example.com", "彼得", "彼得@äöüß.example.com", new Capabilities("{ocs: {}}", null)));
        final Account createdAccount = db.getAccountDao().getAccountById(createdId);
        assertEquals("https://äöüß.example.com", createdAccount.getUrl());
        assertEquals("彼得", createdAccount.getUserName());
        assertEquals("彼得@äöüß.example.com", createdAccount.getAccountName());
    }

    @Test
    public void updateApiVersionFromNull() throws NextcloudHttpRequestFailedException {
        final Account account = db.getAccountDao().getAccountById(db.getAccountDao().insert(new Account("https://äöüß.example.com", "彼得", "彼得@äöüß.example.com", new Capabilities("{ocs: {}}", null))));
        assertNull(account.getApiVersion());

        assertEquals(0, db.getAccountDao().updateApiVersion(account.getId(), null));
        assertEquals(1, db.getAccountDao().updateApiVersion(account.getId(), "[0.2]"));
        assertEquals(0, db.getAccountDao().updateApiVersion(account.getId(), "[0.2]"));
    }

    @Test
    public void updateApiVersionFromExisting() throws NextcloudHttpRequestFailedException {
        final Account account = db.getAccountDao().getAccountById(db.getAccountDao().insert(new Account("https://äöüß.example.com", "彼得", "彼得@äöüß.example.com", new Capabilities("{ocs: {data: {capabilities: {notes: {api_version: '[0.2]'}}}}}", null))));
        assertEquals("[0.2]", account.getApiVersion());

        assertEquals(0, db.getAccountDao().updateApiVersion(account.getId(), "[0.2]"));
        assertEquals(1, db.getAccountDao().updateApiVersion(account.getId(), "[0.2, 1.0]"));
        assertEquals(1, db.getAccountDao().updateApiVersion(account.getId(), null));
    }

}