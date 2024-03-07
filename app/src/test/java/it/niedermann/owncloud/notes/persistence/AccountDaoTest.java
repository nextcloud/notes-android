/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.shared.model.Capabilities;

@RunWith(RobolectricTestRunner.class)
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
    public void insertAccount() {
        final long createdId = db.getAccountDao().insert(new Account("https://äöüß.example.com", "彼得", "彼得@äöüß.example.com", null, new Capabilities()));
        final var createdAccount = db.getAccountDao().getAccountById(createdId);
        assertEquals("https://äöüß.example.com", createdAccount.getUrl());
        assertEquals("彼得", createdAccount.getUserName());
        assertEquals("彼得@äöüß.example.com", createdAccount.getAccountName());
    }

    @Test
    public void updateApiVersionFromNull() {
        final var account = db.getAccountDao().getAccountById(db.getAccountDao().insert(new Account("https://äöüß.example.com", "彼得", "彼得@äöüß.example.com", null, new Capabilities())));
        assertNull(account.getApiVersion());

        assertEquals(0, db.getAccountDao().updateApiVersion(account.getId(), null));
        assertEquals(1, db.getAccountDao().updateApiVersion(account.getId(), "[0.2]"));
        assertEquals(0, db.getAccountDao().updateApiVersion(account.getId(), "[0.2]"));
    }

    @Test
    public void updateApiVersionFromExisting() {
        final var capabilities = new Capabilities();
        capabilities.setApiVersion("[0.2]");
        final var account = db.getAccountDao().getAccountById(db.getAccountDao().insert(new Account("https://äöüß.example.com", "彼得", "彼得@äöüß.example.com", null, capabilities)));
        assertEquals("[0.2]", account.getApiVersion());

        assertEquals(0, db.getAccountDao().updateApiVersion(account.getId(), "[0.2]"));
        assertEquals(1, db.getAccountDao().updateApiVersion(account.getId(), "[0.2, 1.0]"));
        assertEquals(1, db.getAccountDao().updateApiVersion(account.getId(), null));
    }

    @Test
    public void updateDisplayName() {
        final var account = db.getAccountDao().getAccountById(db.getAccountDao().insert(new Account("https://äöüß.example.com", "彼得", "彼得@äöüß.example.com", null, new Capabilities())));
        assertEquals("Should read userName in favor of displayName if displayName is NULL", "彼得", account.getDisplayName());

        db.getAccountDao().updateDisplayName(account.getId(), "");
        assertEquals("Should properly update the displayName, even if it is blank", "", db.getAccountDao().getAccountById(account.getId()).getDisplayName());

        db.getAccountDao().updateDisplayName(account.getId(), "Foo Bar");
        assertEquals("Foo Bar", db.getAccountDao().getAccountById(account.getId()).getDisplayName());

        db.getAccountDao().updateDisplayName(account.getId(), null);
        assertEquals("Should read userName in favor of displayName if displayName is NULL", "彼得", db.getAccountDao().getAccountById(account.getId()).getDisplayName());
    }
}
