/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence;

import android.accounts.NetworkErrorException;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.core.app.ApplicationProvider;

import com.nextcloud.android.sso.api.ParsedResponse;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.Map;

import io.reactivex.Observable;
import it.niedermann.owncloud.notes.persistence.sync.OcsAPI;
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import it.niedermann.owncloud.notes.shared.model.OcsResponse;
import it.niedermann.owncloud.notes.shared.model.OcsUser;
import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CapabilitiesClientTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private final SingleSignOnAccount ssoAccount = mock(SingleSignOnAccount.class);
    private final ApiProvider apiProvider = mock(ApiProvider.class);
    private final OcsAPI ocsAPI = mock(OcsAPI.class);

    @Before
    public void setup() {
        when(apiProvider.getOcsAPI(any(), any())).thenReturn(ocsAPI);
    }

    @Test
    public void testGetCapabilities() throws Throwable {
        //noinspection unchecked
        final ParsedResponse<OcsResponse<Capabilities>> responseMock = mock(ParsedResponse.class);

        final OcsResponse<Capabilities> mockOcs = new OcsResponse<>();
        mockOcs.ocs = new OcsResponse.OcsWrapper<>();
        mockOcs.ocs.data = new Capabilities();
        mockOcs.ocs.data.setApiVersion("[1.0]");

        when(responseMock.getResponse()).thenReturn(mockOcs);
        when(responseMock.getHeaders()).thenReturn(Map.of("ETag", "1234"));
        when(ocsAPI.getCapabilities(any())).thenReturn(Observable.just(responseMock));

        final var capabilities = CapabilitiesClient.getCapabilities(ApplicationProvider.getApplicationContext(), ssoAccount, null, apiProvider);

        assertEquals("[1.0]", capabilities.getApiVersion());
        assertEquals("ETag should be read correctly from response but wasn't.", "1234", capabilities.getETag());

        when(ocsAPI.getCapabilities(any())).thenReturn(Observable.error(new RuntimeException()));
        assertThrows(RuntimeException.class, () -> CapabilitiesClient.getCapabilities(ApplicationProvider.getApplicationContext(), ssoAccount, null, apiProvider));

        when(ocsAPI.getCapabilities(any())).thenReturn(Observable.error(new RuntimeException(new NetworkErrorException())));
        assertThrows("Should unwrap exception cause if possible", NetworkErrorException.class, () -> CapabilitiesClient.getCapabilities(ApplicationProvider.getApplicationContext(), ssoAccount, null, apiProvider));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetDisplayName() throws IOException {
        final var mockOcs = new OcsResponse<OcsUser>();
        mockOcs.ocs = new OcsResponse.OcsWrapper<>();
        mockOcs.ocs.data = new OcsUser();
        mockOcs.ocs.data.displayName = "Peter";
        final var responseMock = Response.success(mockOcs);
        final var callMock = mock(Call.class);

        when(ocsAPI.getUser(any())).thenReturn(callMock);

        when(callMock.execute()).thenReturn(responseMock);
        assertEquals("Peter", CapabilitiesClient.getDisplayName(ApplicationProvider.getApplicationContext(), ssoAccount, apiProvider));

        when(callMock.execute()).thenThrow(new RuntimeException() {
            @Override
            public void printStackTrace() {
                // Do not spam console, this will be printed.
            }
        });
        assertNull(CapabilitiesClient.getDisplayName(ApplicationProvider.getApplicationContext(), ssoAccount, apiProvider));
    }
}
