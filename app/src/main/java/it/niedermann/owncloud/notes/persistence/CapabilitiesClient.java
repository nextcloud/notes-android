package it.niedermann.owncloud.notes.persistence;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.api.ParsedResponse;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.io.IOException;
import java.util.Map;

import it.niedermann.owncloud.notes.persistence.sync.OcsAPI;
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import retrofit2.NextcloudRetrofitApiBuilder;

@WorkerThread
public class CapabilitiesClient {

    private static final String TAG = CapabilitiesClient.class.getSimpleName();

    private static final String API_ENDPOINT_OCS = "/ocs/v2.php/cloud/";
    private static final String HEADER_KEY_ETAG = "ETag";

    public static Capabilities getCapabilities(@NonNull Context context, @NonNull SingleSignOnAccount ssoAccount, @Nullable String lastETag) throws NextcloudHttpRequestFailedException, IOException {
        final NextcloudAPI nextcloudAPI = SSOClient.getNextcloudAPI(context.getApplicationContext(), ssoAccount);
        final OcsAPI ocsAPI = new NextcloudRetrofitApiBuilder(nextcloudAPI, API_ENDPOINT_OCS).create(OcsAPI.class);
        try {
            final ParsedResponse<Capabilities> response = ocsAPI.getCapabilities(lastETag).blockingSingle();
            final Capabilities capabilities = response.getResponse();
            final Map<String, String> headers = response.getHeaders();
            if (headers != null) {
                capabilities.setETag(headers.get(HEADER_KEY_ETAG));
            } else {
                Log.w(TAG, "Response headers of capabilities are null");
            }
            return capabilities;
        } catch (RuntimeException e) {
            if (e.getCause() instanceof NextcloudHttpRequestFailedException) {
                throw (NextcloudHttpRequestFailedException) e.getCause();
            } else {
                throw e;
            }
        }
    }
}
