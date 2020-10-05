package it.niedermann.owncloud.notes.persistence;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.api.AidlNetworkRequest;
import com.nextcloud.android.sso.api.Response;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotSupportedException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.niedermann.owncloud.notes.shared.model.Capabilities;

@WorkerThread
public class CapabilitiesClient {

    private static final String TAG = CapabilitiesClient.class.getSimpleName();

    private static final int MIN_NEXTCLOUD_FILES_APP_VERSION_CODE = 30090000;

    protected static final String HEADER_KEY_IF_NONE_MATCH = "If-None-Match";
    protected static final String HEADER_KEY_ETAG = "ETag";

    private static final String API_PATH = "/ocs/v2.php/cloud/capabilities";
    private static final String METHOD_GET = "GET";
    private static final String PARAM_KEY_FORMAT = "format";
    private static final String PARAM_VALUE_JSON = "json";

    private static final Map<String, String> parameters = new HashMap<>();

    static {
        parameters.put(PARAM_KEY_FORMAT, PARAM_VALUE_JSON);
    }

    public static Capabilities getCapabilities(@NonNull Context context, @NonNull SingleSignOnAccount ssoAccount, @Nullable String lastETag) throws Exception {
        final NextcloudRequest.Builder requestBuilder = new NextcloudRequest.Builder()
                .setMethod(METHOD_GET)
                .setUrl(API_PATH)
                .setParameter(parameters);

        final Map<String, List<String>> header = new HashMap<>();
        if (lastETag != null && !lastETag.isEmpty()) {
            header.put(HEADER_KEY_IF_NONE_MATCH, Collections.singletonList('"' + lastETag + '"'));
            requestBuilder.setHeader(header);
        }

        final NextcloudRequest nextcloudRequest = requestBuilder.build();
        final StringBuilder result = new StringBuilder();

        try {
            Log.v(TAG, ssoAccount.name + " → " + nextcloudRequest.getMethod() + " " + nextcloudRequest.getUrl() + " ");
            final Response response = SSOClient.requestFilesApp(context.getApplicationContext(), ssoAccount, nextcloudRequest);
            Log.v(TAG, "NextcloudRequest: " + nextcloudRequest.toString());

            final BufferedReader rd = new BufferedReader(new InputStreamReader(response.getBody()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            response.getBody().close();

            String etag = null;
            final AidlNetworkRequest.PlainHeader eTagHeader = response.getPlainHeader(HEADER_KEY_ETAG);
            if (eTagHeader != null) {
                etag = eTagHeader.getValue().replace("\"", "");
            }

            return new Capabilities(result.toString(), etag);
        } catch (NullPointerException e) {
            final PackageInfo pInfo = context.getApplicationContext().getPackageManager().getPackageInfo("com.nextcloud.client", 0);
            if (pInfo.versionCode < MIN_NEXTCLOUD_FILES_APP_VERSION_CODE) {
                throw new NextcloudFilesAppNotSupportedException();
            } else {
                throw e;
            }
        }
    }
}
