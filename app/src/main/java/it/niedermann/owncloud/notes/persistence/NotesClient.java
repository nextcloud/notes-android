package it.niedermann.owncloud.notes.persistence;

import android.content.Context;
import android.util.Log;

import androidx.annotation.WorkerThread;

import com.google.gson.GsonBuilder;
import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.api.AidlNetworkRequest;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.api.Response;
import com.nextcloud.android.sso.exceptions.NextcloudApiNotRespondingException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import org.json.JSONException;
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
import it.niedermann.owncloud.notes.util.ServerResponse.NoteResponse;
import it.niedermann.owncloud.notes.util.ServerResponse.NotesResponse;

@WorkerThread
public class NotesClient {

    private static final String TAG = NotesClient.class.getSimpleName();

    private final Context context;
    private NextcloudAPI mNextcloudAPI;

    /**
     * This entity class is used to return relevant data of the HTTP reponse.
     */
    public static class ResponseData {
        private final String content;
        private final String etag;
        private final long lastModified;

        public ResponseData(String content, String etag, long lastModified) {
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

    private static final String API_PATH = "/index.php/apps/notes/api/v0.2/";

    private static final String GET_PARAM_KEY_PRUNE_BEFORE = "pruneBefore";

    private static final String HEADER_KEY_ETAG = "ETag";
    private static final String HEADER_KEY_LAST_MODIFIED = "Last-Modified";
    private static final String HEADER_KEY_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_KEY_IF_NONE_MATCH = "If-None-Match";

    private static final String HEADER_VALUE_APPLICATION_JSON = "application/json";

    private static final String METHOD_GET = "GET";
    private static final String METHOD_PUT = "PUT";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_DELETE = "DELETE";

    public static final String JSON_ID = "id";
    public static final String JSON_TITLE = "title";
    public static final String JSON_CONTENT = "content";
    public static final String JSON_FAVORITE = "favorite";
    public static final String JSON_CATEGORY = "category";
    public static final String JSON_ETAG = "etag";
    public static final String JSON_MODIFIED = "modified";

    NotesClient(Context context) {
        this.context = context;
        updateAccount();
    }
    
    void updateAccount() {
        if(mNextcloudAPI != null) {
            mNextcloudAPI.stop();
        }
        try {
            SingleSignOnAccount ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(context);
            Log.v(TAG, "NextcloudRequest account: " + ssoAccount.name);
            mNextcloudAPI = new NextcloudAPI(context, ssoAccount, new GsonBuilder().create(), new NextcloudAPI.ApiConnectedListener() {
                @Override
                public void onConnected() {
                    Log.v(TAG, "SSO API connected");
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            });
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            e.printStackTrace();
        }
    }

    NotesResponse getNotes(long lastModified, String lastETag) throws NextcloudHttpRequestFailedException, NextcloudApiNotRespondingException {
        Map<String, String> parameter = new HashMap<>();
        parameter.put(GET_PARAM_KEY_PRUNE_BEFORE, Long.toString(lastModified));
        return new NotesResponse(requestServer("notes", METHOD_GET, parameter, null, lastETag));
    }

    private NoteResponse putNote(CloudNote note, String path, String method) throws JSONException, NextcloudHttpRequestFailedException, NextcloudApiNotRespondingException {
        JSONObject paramObject = new JSONObject();
        paramObject.accumulate(JSON_CONTENT, note.getContent());
        paramObject.accumulate(JSON_MODIFIED, note.getModified().getTimeInMillis() / 1000);
        paramObject.accumulate(JSON_FAVORITE, note.isFavorite());
        paramObject.accumulate(JSON_CATEGORY, note.getCategory());
        return new NoteResponse(requestServer(path, method, null, paramObject, null));
    }

    /**
     * Creates a Note on the Server
     *
     * @param note {@link CloudNote} - the new Note
     * @return Created Note including generated Title, ID and lastModified-Date
     * @throws JSONException
     * @throws NextcloudHttpRequestFailedException
     */
    NoteResponse createNote(CloudNote note) throws JSONException, NextcloudHttpRequestFailedException, NextcloudApiNotRespondingException {
        return putNote(note, "notes", METHOD_POST);
    }

    NoteResponse editNote(CloudNote note) throws JSONException, NextcloudHttpRequestFailedException, NextcloudApiNotRespondingException {
        return putNote(note, "notes/" + note.getRemoteId(), METHOD_PUT);
    }

    void deleteNote(long noteId) {
        try {
            this.requestServer("notes/" + noteId, METHOD_DELETE, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Request-Method for POST, PUT with or without JSON-Object-Parameter
     *
     * @param target Filepath to the wanted function
     * @param method GET, POST, DELETE or PUT
     * @param parameter optional headers to be sent
     * @param requestBody JSON Object which shall be transferred to the server.
     * @param lastETag optional ETag of last response
     * @return Body of answer
     */
    private ResponseData requestServer(String target, String method, Map<String, String> parameter, JSONObject requestBody, String lastETag) throws NextcloudHttpRequestFailedException, NextcloudApiNotRespondingException {
        NextcloudRequest.Builder requestBuilder = new NextcloudRequest.Builder()
                .setMethod(method)
                .setUrl(API_PATH + target);
        if(parameter != null) {
            requestBuilder.setParameter(parameter);
        }

        Map<String, List<String>> header = new HashMap<>();
        if (requestBody != null) {
            header.put(HEADER_KEY_CONTENT_TYPE, Collections.singletonList(HEADER_VALUE_APPLICATION_JSON));
            requestBuilder.setRequestBody(requestBody.toString());
        }
        if (lastETag != null && !lastETag.isEmpty() && METHOD_GET.equals(method)) {
            header.put(HEADER_KEY_IF_NONE_MATCH, Collections.singletonList('"' + lastETag + '"'));
            requestBuilder.setHeader(header);
        }

        NextcloudRequest nextcloudRequest = requestBuilder.build();

        StringBuilder result = new StringBuilder();

        try {
            Log.v(TAG, "NextcloudRequest: " + nextcloudRequest.toString());
            Response response = mNextcloudAPI.performNetworkRequestV2(nextcloudRequest);
            Log.v(TAG, "NextcloudRequest: " + nextcloudRequest.toString());
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getBody()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            response.getBody().close();

            String etag = "";
            AidlNetworkRequest.PlainHeader eTagHeader = response.getPlainHeader(HEADER_KEY_ETAG);
            if (eTagHeader != null) {
                etag = Objects.requireNonNull(eTagHeader.getValue()).replace("\"", "");
            }

            long lastModified = 0;
            AidlNetworkRequest.PlainHeader lastModifiedHeader = response.getPlainHeader(HEADER_KEY_LAST_MODIFIED);
            if (lastModifiedHeader != null)
                lastModified = new Date(lastModifiedHeader.getValue()).getTime() / 1000;
            Log.d(TAG, "ETag: " + etag + "; Last-Modified: " + lastModified + " (" + lastModified + ")");
            // return these header fields since they should only be saved after successful processing the result!
            return new ResponseData(result.toString(), etag, lastModified);
        } catch (Exception e) {
            if(e instanceof NextcloudHttpRequestFailedException) {
                throw (NextcloudHttpRequestFailedException) e;
            } else if(e instanceof NextcloudApiNotRespondingException) {
                throw (NextcloudApiNotRespondingException) e;
            } else {
                e.printStackTrace();
            }
        }
        return null;
    }
}
