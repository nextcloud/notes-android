package it.niedermann.owncloud.notes.persistence;

import android.os.Build;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.core.app.ApplicationProvider;

import com.nextcloud.android.sso.api.ParsedResponse;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Map;

import io.reactivex.Observable;
import it.niedermann.owncloud.notes.persistence.sync.OcsAPI;
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import it.niedermann.owncloud.notes.shared.model.OcsResponse;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.P})
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

        final Capabilities capabilities = CapabilitiesClient.getCapabilities(ApplicationProvider.getApplicationContext(), ssoAccount, null, apiProvider);

        assertEquals("[1.0]", capabilities.getApiVersion());
        assertEquals("ETag should be read correctly from response but wasn't.", "1234", capabilities.getETag());
    }
}