package it.niedermann.owncloud.notes.util;

import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import it.niedermann.owncloud.notes.model.CloudNote;
import it.niedermann.owncloud.notes.model.SyncDataEntry;

public class NotesClient {

    public static final String METHOD_GET = "GET";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_DELETE = "DELETE";
    private static final String key_id = "id";
    private static final String key_title = "title";
    private static final String key_content = "content";
    private static final String key_favorite = "favorite";
    private static final String key_category = "category";
    private static final String key_etag = "etag";
    private static final String key_modified = "modified";
    private static final String application_json = "application/json";
    private String url = "";
    private String username = "";
    private String password = "";

    public NotesClient(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private CloudNote getNoteFromJSON(JSONObject json) throws JSONException {
        long id = 0;
        String title = "";
        String content = "";
        Calendar modified = null;
        boolean favorite = false;
        String category = null;
        String etag = null;
        if (!json.isNull(key_id)) {
            id = json.getLong(key_id);
        }
        if (!json.isNull(key_title)) {
            title = json.getString(key_title);
        }
        if (!json.isNull(key_content)) {
            content = json.getString(key_content);
        }
        if (!json.isNull(key_modified)) {
            modified = GregorianCalendar.getInstance();
            modified.setTimeInMillis(json.getLong(key_modified) * 1000);
        }
        if (!json.isNull(key_favorite)) {
            favorite = json.getBoolean(key_favorite);
        }
        if (!json.isNull(key_category)) {
            category = json.getString(key_category);
        }
        if (!json.isNull(key_etag)) {
            etag = json.getString(key_etag);
        }
        return new CloudNote(id, modified, title, content, favorite, category, etag);
    }

    public List<CloudNote> getNotes(Collection<SyncDataEntry> etags) throws JSONException, IOException {
        List<CloudNote> notesList = new ArrayList<>();
        JSONArray notes = new JSONArray(requestServer("notes", METHOD_GET, null, etags));
        for (int i = 0; i < notes.length(); i++) {
            JSONObject json = notes.getJSONObject(i);
            notesList.add(getNoteFromJSON(json));
        }
        return notesList;
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
    public CloudNote getNoteById(long id) throws JSONException, IOException {
        JSONObject json = new JSONObject(requestServer("notes/" + id, METHOD_GET, null, null));
        return getNoteFromJSON(json);
    }

    private CloudNote putNote(CloudNote note, String path, String method)  throws JSONException, IOException {
        JSONObject paramObject = new JSONObject();
        paramObject.accumulate(key_content, note.getContent());
        paramObject.accumulate(key_modified, note.getModified().getTimeInMillis()/1000);
        paramObject.accumulate(key_favorite, note.isFavorite());
        JSONObject json = new JSONObject(requestServer(path, method, paramObject, null));
        return getNoteFromJSON(json);
    }

    /**
     * Creates a Note on the Server
     *
     * @param note {@link CloudNote} - the new Note
     * @return Created Note including generated Title, ID and lastModified-Date
     * @throws JSONException
     * @throws IOException
     */
    public CloudNote createNote(CloudNote note) throws JSONException, IOException {
        return putNote(note, "notes", METHOD_POST);
    }

    public CloudNote editNote(CloudNote note) throws JSONException, IOException {
        return putNote(note, "notes/" + note.getRemoteId(), METHOD_PUT);
    }

    public void deleteNote(long noteId) throws
            IOException {
        this.requestServer("notes/" + noteId, METHOD_DELETE, null, null);
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
    private String requestServer(String target, String method, JSONObject params, Collection<SyncDataEntry> etags)
            throws IOException {
        StringBuilder etagsBuilder = new StringBuilder();
        if(etags!=null) {
            for (SyncDataEntry entry : etags) {
                if (entry.getEtag() != null) {
                    etagsBuilder.append(entry.getEtag());
                }
            }
        }

        StringBuffer result = new StringBuffer();
        String targetURL = url + "index.php/apps/notes/api/v0.2/" + target;
        long timeStart = System.currentTimeMillis();
        HttpURLConnection con = (HttpURLConnection) new URL(targetURL)
                .openConnection();
        con.setRequestMethod(method);
        con.setRequestProperty(
                "Authorization",
                "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP));
        if(etagsBuilder.length()>0) {
            con.setRequestProperty("X-Notes-ETags", etagsBuilder.toString());
        }
        con.setConnectTimeout(10 * 1000); // 10 seconds
        Log.i(getClass().getSimpleName(), method + " " + targetURL);
        byte[] paramData=null;
        if (params != null) {
            paramData = params.toString().getBytes();
            Log.d(getClass().getSimpleName(), "Params: "+params);
            con.setFixedLengthStreamingMode(paramData.length);
            con.setRequestProperty("Content-Type", application_json);
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(paramData);
            os.flush();
            os.close();
        }
        long timePre = System.currentTimeMillis() - timeStart;
        long timeBeforeStream = System.currentTimeMillis();
        BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
        Log.i(getClass().getSimpleName(), "Content-Length: "+con.getContentLength()+"; Etag: " + con.getHeaderField("Etag") + (paramData==null ? "" : "; Request length: "+paramData.length));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        long timeStream = System.currentTimeMillis() - timeBeforeStream;
        Log.i(getClass().getSimpleName(), "Result length:  " + result.length() + (paramData==null ? "" : "; Request length: "+paramData.length));
        Log.i(getClass().getSimpleName(), "timePre: "+timePre+"ms; timeStream: "+timeStream+"ms");
        return result.toString();
    }
}