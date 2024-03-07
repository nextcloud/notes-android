/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.core.app.ApplicationProvider;

import com.nextcloud.android.sso.model.SingleSignOnAccount;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import it.niedermann.owncloud.notes.persistence.sync.NotesAPI;
import it.niedermann.owncloud.notes.persistence.sync.OcsAPI;
import it.niedermann.owncloud.notes.shared.model.ApiVersion;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

@RunWith(RobolectricTestRunner.class)
public class ApiProviderTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private ApiProvider apiProvider;
    private SingleSignOnAccount ssoAccount;
    private SingleSignOnAccount secondSsoAccount;

    @Before
    public void setup() {
        apiProvider = ApiProvider.getInstance();
        ssoAccount = new SingleSignOnAccount("one", "eins", "1", "example.com", "");
        secondSsoAccount = new SingleSignOnAccount("two", "zwei", "2", "example.org", "");
    }

    @Test
    public void testGetOcsAPI() {
        var api = apiProvider.getOcsAPI(ApplicationProvider.getApplicationContext(), ssoAccount);

        assertNotNull(api);
        assertSame(api, apiProvider.getOcsAPI(ApplicationProvider.getApplicationContext(), ssoAccount));
        assertNotSame(api, apiProvider.getOcsAPI(ApplicationProvider.getApplicationContext(), secondSsoAccount));

        apiProvider.invalidateAPICache(ssoAccount);

        final OcsAPI newApi = apiProvider.getOcsAPI(ApplicationProvider.getApplicationContext(), ssoAccount);

        assertNotSame("After invalidating the cache, a new API instance is returned", api, newApi);

        api = newApi;

        assertSame(api, apiProvider.getOcsAPI(ApplicationProvider.getApplicationContext(), ssoAccount));
        assertNotSame("A new instance should be returned when another SingleSignOn account is provided",
                api, apiProvider.getOcsAPI(ApplicationProvider.getApplicationContext(), secondSsoAccount));

        apiProvider.invalidateAPICache();

        assertNotSame(api, apiProvider.getOcsAPI(ApplicationProvider.getApplicationContext(), ssoAccount));
        assertNotSame(newApi, apiProvider.getOcsAPI(ApplicationProvider.getApplicationContext(), secondSsoAccount));
    }

    @Test
    public void testGetNotesAPI() {
        final var notesAPI = apiProvider.getNotesAPI(ApplicationProvider.getApplicationContext(), ssoAccount, ApiVersion.API_VERSION_0_2);

        assertNotNull(notesAPI);

        assertSame("Before a manual invalidation, the returned Notes API will be the same instance",
                notesAPI, apiProvider.getNotesAPI(ApplicationProvider.getApplicationContext(), ssoAccount, ApiVersion.API_VERSION_0_2));
        assertSame("Before a manual invalidation, the returned Notes API will be the same instance, no matter which preferred version is passed",
                notesAPI, apiProvider.getNotesAPI(ApplicationProvider.getApplicationContext(), ssoAccount, ApiVersion.API_VERSION_1_0));

        apiProvider.invalidateAPICache();

        final var newNotesAPI = apiProvider.getNotesAPI(ApplicationProvider.getApplicationContext(), ssoAccount, ApiVersion.API_VERSION_1_0);

        assertNotSame("After a manual invalidation, the returned Notes API will be a new instance",
                notesAPI, newNotesAPI);
        assertSame("Before a manual invalidation, the returned Notes API will be the same instance, no matter which preferred version is passed",
                newNotesAPI, apiProvider.getNotesAPI(ApplicationProvider.getApplicationContext(), ssoAccount, ApiVersion.API_VERSION_0_2));
        assertNotSame("Before a manual invalidation, the returned Notes API will be a different instance, even if the preferred version is the same, if the ssoAccount is different",
                newNotesAPI, apiProvider.getNotesAPI(ApplicationProvider.getApplicationContext(), secondSsoAccount, ApiVersion.API_VERSION_0_2));

        apiProvider.invalidateAPICache(ssoAccount);

        assertNotSame("After a manual invalidation, the returned Notes API will be a new instance",
                newNotesAPI, apiProvider.getNotesAPI(ApplicationProvider.getApplicationContext(), ssoAccount, ApiVersion.API_VERSION_0_2));
    }
}
