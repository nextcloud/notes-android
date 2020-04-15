package it.niedermann.owncloud.notes.persistence;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.api.AidlNetworkRequest;
import com.nextcloud.android.sso.api.Response;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotSupportedException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.niedermann.owncloud.notes.model.CloudNote;
import it.niedermann.owncloud.notes.model.LocalAccount;
import it.niedermann.owncloud.notes.util.ServerResponse.NoteResponse;
import it.niedermann.owncloud.notes.util.ServerResponse.NotesResponse;

@SuppressWarnings("WeakerAccess")
@WorkerThread
public abstract class NotesClient {

    final static int MIN_NEXTCLOUD_FILES_APP_VERSION_CODE = 30090000;
    private static final String TAG = NotesClient.class.getSimpleName();

    protected final Context appContext;

    protected static final String GET_PARAM_KEY_PRUNE_BEFORE = "pruneBefore";

    protected static final String HEADER_KEY_ETAG = "ETag";
    protected static final String HEADER_KEY_LAST_MODIFIED = "Last-Modified";
    protected static final String HEADER_KEY_CONTENT_TYPE = "Content-Type";
    protected static final String HEADER_KEY_IF_NONE_MATCH = "If-None-Match";

    protected static final String HEADER_VALUE_APPLICATION_JSON = "application/json";

    protected static final String METHOD_GET = "GET";
    protected static final String METHOD_PUT = "PUT";
    protected static final String METHOD_POST = "POST";
    protected static final String METHOD_DELETE = "DELETE";

    public static final String JSON_ID = "id";
    public static final String JSON_TITLE = "title";
    public static final String JSON_CONTENT = "content";
    public static final String JSON_FAVORITE = "favorite";
    public static final String JSON_CATEGORY = "category";
    public static final String JSON_ETAG = "etag";
    public static final String JSON_MODIFIED = "modified";

    public static NotesClient newInstance(@NonNull LocalAccount localAccount,
                                          @NonNull Context appContext) {
        // TODO check localAccount API_VERSION and create another instance if necessary
        return new NotesClient_0_2(appContext);
    }

    @SuppressWarnings("WeakerAccess")
    public NotesClient(@NonNull Context appContext) {
        this.appContext = appContext;
    }

    abstract NotesResponse getNotes(SingleSignOnAccount ssoAccount, long lastModified, String lastETag) throws Exception;

    abstract NoteResponse createNote(SingleSignOnAccount ssoAccount, CloudNote note) throws Exception;

    abstract NoteResponse editNote(SingleSignOnAccount ssoAccount, CloudNote note) throws Exception;

    abstract void deleteNote(SingleSignOnAccount ssoAccount, long noteId) throws Exception;

    /**
     * This entity class is used to return relevant data of the HTTP reponse.
     */
    public static class ResponseData {
        private final String content;
        private final String etag;
        private final long lastModified;

        ResponseData(String content, String etag, long lastModified) {
            this.content = content;
            this.etag = etag;
            this.lastModified = lastModified;
        }

        public String getContent() {
            return content;
        }

        public String getETag() {
            return etag;
        }

        public long getLastModified() {
            return lastModified;
        }
    }

    abstract protected String getApiPath();

    /**
     * Request-Method for POST, PUT with or without JSON-Object-Parameter
     *
     * @param target      Filepath to the wanted function
     * @param method      GET, POST, DELETE or PUT
     * @param parameter   optional headers to be sent
     * @param requestBody JSON Object which shall be transferred to the server.
     * @param lastETag    optional ETag of last response
     * @return Body of answer
     */
    protected ResponseData requestServer(SingleSignOnAccount ssoAccount, String target, String method, Map<String, String> parameter, JSONObject requestBody, String lastETag) throws Exception {
        final NextcloudRequest.Builder requestBuilder = new NextcloudRequest.Builder()
                .setMethod(method)
                .setUrl(getApiPath() + target);
        if (parameter != null) {
            requestBuilder.setParameter(parameter);
        }

        final Map<String, List<String>> header = new HashMap<>();
        if (requestBody != null) {
            header.put(HEADER_KEY_CONTENT_TYPE, Collections.singletonList(HEADER_VALUE_APPLICATION_JSON));
            requestBuilder.setRequestBody(requestBody.toString());
        }
        if (lastETag != null && !lastETag.isEmpty() && METHOD_GET.equals(method)) {
            header.put(HEADER_KEY_IF_NONE_MATCH, Collections.singletonList('"' + lastETag + '"'));
            requestBuilder.setHeader(header);
        }

        final NextcloudRequest nextcloudRequest = requestBuilder.build();
        final StringBuilder result = new StringBuilder();

        try {
            Log.v(TAG, ssoAccount.name + " → " + nextcloudRequest.getMethod() + " " + nextcloudRequest.getUrl() + " ");
            final Response response = SSOClient.requestFilesApp(appContext, ssoAccount, nextcloudRequest);
            Log.v(TAG, "NextcloudRequest: " + nextcloudRequest.toString());

            final BufferedReader rd = new BufferedReader(new InputStreamReader(response.getBody()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            response.getBody().close();

            String etag = "";
            final AidlNetworkRequest.PlainHeader eTagHeader = response.getPlainHeader(HEADER_KEY_ETAG);
            if (eTagHeader != null) {
                etag = Objects.requireNonNull(eTagHeader.getValue()).replace("\"", "");
            }

            long lastModified = 0;
            final AidlNetworkRequest.PlainHeader lastModifiedHeader = response.getPlainHeader(HEADER_KEY_LAST_MODIFIED);
            if (lastModifiedHeader != null)
                lastModified = new Date(lastModifiedHeader.getValue()).getTime() / 1000;
            Log.d(TAG, "ETag: " + etag + "; Last-Modified: " + lastModified + " (" + lastModified + ")");
            // return these header fields since they should only be saved after successful processing the result!
            return new ResponseData(result.toString(), etag, lastModified);
        } catch (NullPointerException e) {
            final PackageInfo pInfo = appContext.getPackageManager().getPackageInfo("com.nextcloud.client", 0);
            if (pInfo.versionCode < MIN_NEXTCLOUD_FILES_APP_VERSION_CODE) {
                throw new NextcloudFilesAppNotSupportedException();
            } else {
                throw e;
            }
        }
    }
}
