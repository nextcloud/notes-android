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
import java.util.GregorianCalendar;
import java.util.List;

import it.niedermann.owncloud.notes.model.CloudNote;

public class NotesClient {

    public static final String METHOD_GET = "GET";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_DELETE = "DELETE";
    private static final String key_id = "id";
    private static final String key_title = "title";
    private static final String key_content = "content";
    private static final String key_favorite = "favorite";
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
        long noteId = 0;
        String noteTitle = "";
        String noteContent = "";
        Calendar noteModified = null;
        boolean noteFavorite = false;
        if (!json.isNull(key_id)) {
            noteId = json.getLong(key_id);
        }
        if (!json.isNull(key_title)) {
            noteTitle = json.getString(key_title);
        }
        if (!json.isNull(key_content)) {
            noteContent = json.getString(key_content);
        }
        if (!json.isNull(key_modified)) {
            noteModified = GregorianCalendar.getInstance();
            noteModified.setTimeInMillis(json.getLong(key_modified) * 1000);
        }
        if (!json.isNull(key_favorite)) {
            noteFavorite = json.getBoolean(key_favorite);
        }
        return new CloudNote(noteId, noteModified, noteTitle, noteContent, noteFavorite);
    }

    public List<CloudNote> getNotes() throws JSONException, IOException {
        List<CloudNote> notesList = new ArrayList<>();
        JSONArray notes = new JSONArray(requestServer("notes", METHOD_GET, null));
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
        JSONObject json = new JSONObject(requestServer("notes/" + id, METHOD_GET, null));
        return getNoteFromJSON(json);
    }

    private CloudNote putNote(CloudNote note, String path, String method)  throws JSONException, IOException {
        JSONObject paramObject = new JSONObject();
        paramObject.accumulate(key_content, note.getContent());
        paramObject.accumulate(key_modified, note.getModified().getTimeInMillis()/1000);
        paramObject.accumulate(key_favorite, note.isFavorite());
        JSONObject json = new JSONObject(requestServer(path, method, paramObject));
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
        this.requestServer("notes/" + noteId, METHOD_DELETE, null);
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
    private String requestServer(String target, String method, JSONObject params)
            throws IOException {
        StringBuffer result = new StringBuffer();
        String targetURL = url + "index.php/apps/notes/api/v0.2/" + target;
        HttpURLConnection con = (HttpURLConnection) new URL(targetURL)
                .openConnection();
        con.setRequestMethod(method);
        con.setRequestProperty(
                "Authorization",
                "Basic "
                        + new String(Base64.encode((username + ":"
                        + password).getBytes(), Base64.NO_WRAP)));
        con.setConnectTimeout(10 * 1000); // 10 seconds
        Log.d(getClass().getSimpleName(), method + " " + targetURL);
        if (params != null) {
            Log.d(getClass().getSimpleName(), "Params: "+params);
            con.setFixedLengthStreamingMode(params.toString().getBytes().length);
            con.setRequestProperty("Content-Type", application_json);
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(params.toString().getBytes());
            os.flush();
            os.close();
        }
        BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }
}