package it.niedermann.owncloud.notes.persistence;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.gson.JsonParseException;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.io.IOException;

import it.niedermann.owncloud.notes.persistence.sync.ApiProvider;
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import retrofit2.Response;

import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;

@WorkerThread
public class CapabilitiesClient {

    protected static final String HEADER_KEY_ETAG = "ETag";

    public static Capabilities getCapabilities(@NonNull Context context, @NonNull SingleSignOnAccount ssoAccount, @Nullable String lastETag) throws NextcloudHttpRequestFailedException, IOException {
        final ApiProvider provider = new ApiProvider(context, ssoAccount);
        final Response<Capabilities> response = provider.getOcsAPI().getCapabilities(lastETag).execute();
        try {
            final Capabilities capabilities = response.body();
            capabilities.setETag(response.headers().get(HEADER_KEY_ETAG));
            return capabilities;
        } catch (JsonParseException e) {
            if (e.getCause() instanceof NextcloudHttpRequestFailedException && ((NextcloudHttpRequestFailedException) e.getCause()).getStatusCode() == HTTP_UNAVAILABLE) {
                throw (NextcloudHttpRequestFailedException) e.getCause();
            } else {
                throw e;
            }
        }
    }
}
