package it.niedermann.owncloud.notes.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.WorkerThread;

import com.google.gson.GsonBuilder;
import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.niedermann.owncloud.notes.model.CloudNote;
import it.niedermann.owncloud.notes.util.ServerResponse.NoteResponse;
import it.niedermann.owncloud.notes.util.ServerResponse.NotesResponse;

@WorkerThread
public class NotesClient {

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

    public static final String METHOD_GET = "GET";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_DELETE = "DELETE";
    public static final String JSON_ID = "id";
    public static final String JSON_TITLE = "title";
    public static final String JSON_CONTENT = "content";
    public static final String JSON_FAVORITE = "favorite";
    public static final String JSON_CATEGORY = "category";
    public static final String JSON_ETAG = "etag";
    public static final String JSON_MODIFIED = "modified";
    private static final String application_json = "application/json";

    public NotesClient(Context context) {
        this.context = context;
        updateAccount();
    }
    
    public void updateAccount() {
        if(mNextcloudAPI != null) {
            mNextcloudAPI.stop();
        }
        try {
            SingleSignOnAccount ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(context);
            Log.v(getClass().getSimpleName(), "NextcloudRequest account: " + ssoAccount.name);
            mNextcloudAPI = new NextcloudAPI(context, ssoAccount, new GsonBuilder().create(), new NextcloudAPI.ApiConnectedListener() {
                @Override
                public void onConnected() {
                    Log.v(getClass().getSimpleName(), "SSO API connected");
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

    public NotesResponse getNotes(long lastModified, String lastETag) {
        String url = "notes";
        if (lastModified > 0) {
            url += "?pruneBefore=" + lastModified;
        }
        return new NotesResponse(requestServer(url, METHOD_GET, null, lastETag));
    }

    /**
     * Fetches a Note by ID from Server
     *
     * @param id long - ID of the wanted note
     * @return Requested Note
     */
    @SuppressWarnings("unused")
    public NoteResponse getNoteById(long id) {
        return new NoteResponse(requestServer("notes/" + id, METHOD_GET, null, null));
    }

    private NoteResponse putNote(CloudNote note, String path, String method) throws JSONException {
        JSONObject paramObject = new JSONObject();
        paramObject.accumulate(JSON_CONTENT, note.getContent());
        paramObject.accumulate(JSON_MODIFIED, note.getModified().getTimeInMillis() / 1000);
        paramObject.accumulate(JSON_FAVORITE, note.isFavorite());
        paramObject.accumulate(JSON_CATEGORY, note.getCategory());
        return new NoteResponse(requestServer(path, method, paramObject, null));
    }


    /**
     * Creates a Note on the Server
     *
     * @param note {@link CloudNote} - the new Note
     * @return Created Note including generated Title, ID and lastModified-Date
     * @throws JSONException
     */
    public NoteResponse createNote(CloudNote note) throws JSONException {
        return putNote(note, "notes", METHOD_POST);
    }

    public NoteResponse editNote(CloudNote note) throws JSONException {
        return putNote(note, "notes/" + note.getRemoteId(), METHOD_PUT);
    }

    public void deleteNote(long noteId) {
        this.requestServer("notes/" + noteId, METHOD_DELETE, null, null);
    }

    /**
     * Request-Method for POST, PUT with or without JSON-Object-Parameter
     *
     * @param target Filepath to the wanted function
     * @param method GET, POST, DELETE or PUT
     * @param params JSON Object which shall be transferred to the server.
     * @return Body of answer
     */
    private ResponseData requestServer(String target, String method, JSONObject params, String lastETag) {
        NextcloudRequest.Builder requestBuilder = new NextcloudRequest.Builder()
                .setMethod(method)
                .setUrl("/index.php/apps/notes/api/v0.2/" + target);

        Map<String, List<String>> header = new HashMap<>();
        if (params != null) {
            header.put("Content-Type", Collections.singletonList(application_json));
            requestBuilder.setRequestBody(params.toString());
        }
        if (lastETag != null && !lastETag.isEmpty() && METHOD_GET.equals(method)) {
            header.put("If-None-Match", Collections.singletonList(lastETag));
            requestBuilder.setHeader(header);
        }

        NextcloudRequest nextcloudRequest = requestBuilder.build();

        StringBuilder result = new StringBuilder();

        try {
            Log.v(getClass().getSimpleName(), "NextcloudRequest: " + nextcloudRequest.toString());
            InputStream inputStream = mNextcloudAPI.performNetworkRequest(nextcloudRequest);
            Log.v(getClass().getSimpleName(), "NextcloudRequest: " + nextcloudRequest.toString());
            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String etag = "";
        if (nextcloudRequest.getHeader().get("ETag") != null) {
            etag = nextcloudRequest.getHeader().get("ETag").get(0);
        }
        long lastModified = 0;
        if (nextcloudRequest.getHeader().get("Last-Modified") != null)
            lastModified = Long.parseLong(nextcloudRequest.getHeader().get("Last-Modified").get(0)) / 1000;
        Log.d(getClass().getSimpleName(), "ETag: " + etag + "; Last-Modified: " + lastModified + " (" + lastModified + ")");
        // return these header fields since they should only be saved after successful processing the result!
        return new ResponseData(result.toString(), etag, lastModified);
    }
}
