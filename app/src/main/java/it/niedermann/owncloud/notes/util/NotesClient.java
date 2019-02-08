package it.niedermann.owncloud.notes.util;

import androidx.annotation.WorkerThread;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import at.bitfire.cert4android.CustomCertManager;
import it.niedermann.owncloud.notes.BuildConfig;
import it.niedermann.owncloud.notes.model.CloudNote;
import it.niedermann.owncloud.notes.util.ServerResponse.NoteResponse;
import it.niedermann.owncloud.notes.util.ServerResponse.NotesResponse;

@WorkerThread
public class NotesClient {

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
    private String url = "";
    private String username = "";
    private String password = "";

    public NotesClient(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public NotesResponse getNotes(CustomCertManager ccm, long lastModified, String lastETag) throws JSONException, IOException {
        String url = "notes";
        if (lastModified > 0) {
            url += "?pruneBefore=" + lastModified;
        }
        return new NotesResponse(requestServer(ccm, url, METHOD_GET, null, lastETag));
    }

    /**
     * Fetches a Note by ID from Server
     *
     * @param id long - ID of the wanted note
     * @return Requested Note
     * @throws JSONException
     * @throws IOException
     */
    @SuppressWarnings("unused")
    public NoteResponse getNoteById(CustomCertManager ccm, long id) throws JSONException, IOException {
        return new NoteResponse(requestServer(ccm, "notes/" + id, METHOD_GET, null, null));
    }

    private NoteResponse putNote(CustomCertManager ccm, CloudNote note, String path, String method) throws JSONException, IOException {
        JSONObject paramObject = new JSONObject();
        paramObject.accumulate(JSON_CONTENT, note.getContent());
        paramObject.accumulate(JSON_MODIFIED, note.getModified().getTimeInMillis() / 1000);
        paramObject.accumulate(JSON_FAVORITE, note.isFavorite());
        paramObject.accumulate(JSON_CATEGORY, note.getCategory());
        return new NoteResponse(requestServer(ccm, path, method, paramObject, null));
    }


    /**
     * Creates a Note on the Server
     *
     * @param note {@link CloudNote} - the new Note
     * @return Created Note including generated Title, ID and lastModified-Date
     * @throws JSONException
     * @throws IOException
     */
    public NoteResponse createNote(CustomCertManager ccm, CloudNote note) throws JSONException, IOException {
        return putNote(ccm, note, "notes", METHOD_POST);
    }

    public NoteResponse editNote(CustomCertManager ccm, CloudNote note) throws JSONException, IOException {
        return putNote(ccm, note, "notes/" + note.getRemoteId(), METHOD_PUT);
    }

    public void deleteNote(CustomCertManager ccm, long noteId) throws IOException {
        this.requestServer(ccm, "notes/" + noteId, METHOD_DELETE, null, null);
    }

    /**
     * Request-Method for POST, PUT with or without JSON-Object-Parameter
     *
     * @param target Filepath to the wanted function
     * @param method GET, POST, DELETE or PUT
     * @param params JSON Object which shall be transferred to the server.
     * @return Body of answer
     * @throws MalformedURLException
     * @throws IOException
     */
    private ResponseData requestServer(CustomCertManager ccm, String target, String method, JSONObject params, String lastETag)
            throws IOException {
        StringBuffer result = new StringBuffer();
        // setup connection
        String targetURL = url + "index.php/apps/notes/api/v0.2/" + target;
        HttpURLConnection con = SupportUtil.getHttpURLConnection(ccm, targetURL);
        con.setRequestMethod(method);
        con.setRequestProperty(
                "Authorization",
                "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP));
        // https://github.com/square/retrofit/issues/805#issuecomment-93426183
        con.setRequestProperty( "Connection", "Close");
        con.setRequestProperty("User-Agent", "nextcloud-notes/" + BuildConfig.VERSION_NAME + " (Android)");
        if (lastETag != null && METHOD_GET.equals(method)) {
            con.setRequestProperty("If-None-Match", lastETag);
        }
        con.setConnectTimeout(10 * 1000); // 10 seconds
        Log.d(getClass().getSimpleName(), method + " " + targetURL);
        // send request data (optional)
        byte[] paramData = null;
        if (params != null) {
            paramData = params.toString().getBytes();
            Log.d(getClass().getSimpleName(), "Params: " + params);
            con.setFixedLengthStreamingMode(paramData.length);
            con.setRequestProperty("Content-Type", application_json);
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(paramData);
            os.flush();
            os.close();
        }
        // read response data
        int responseCode = con.getResponseCode();
        Log.d(getClass().getSimpleName(), "HTTP response code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
            throw new ServerResponse.NotModifiedException();
        }

        BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        // create response object
        String etag = con.getHeaderField("ETag");
        long lastModified = con.getHeaderFieldDate("Last-Modified", 0) / 1000;
        Log.i(getClass().getSimpleName(), "Result length:  " + result.length() + (paramData == null ? "" : "; Request length: " + paramData.length));
        Log.d(getClass().getSimpleName(), "ETag: " + etag + "; Last-Modified: " + lastModified + " (" + con.getHeaderField("Last-Modified") + ")");
        // return these header fields since they should only be saved after successful processing the result!
        return new ResponseData(result.toString(), etag, lastModified);
    }
}
