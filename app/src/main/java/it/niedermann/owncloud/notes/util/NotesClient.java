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

import it.niedermann.owncloud.notes.model.Note;

public class NotesClient {

	private String url = "";
	private String username = "";
	private String password = "";

	public NotesClient(String url, String username, String password) {
		this.url = url;
		this.username = username;
		this.password = password;
	}

	public List<Note> getNotes() throws JSONException,
            IOException {
		List<Note> notesList = new ArrayList<>();
		JSONArray notes = new JSONArray(requestServer("notes", "GET", null));
		long noteId = 0;
		String noteTitle = "";
		String noteContent = "";
		Calendar noteModified = null;
		JSONObject currentItem;
		for (int i = 0; i < notes.length(); i++) {
			currentItem = notes.getJSONObject(i);

			if (!currentItem.isNull("id")) {
				noteId = currentItem.getLong("id");
			}
			if (!currentItem.isNull("title")) {
				noteTitle = currentItem.getString("title");
			}
			if (!currentItem.isNull("content")) {
				noteContent = currentItem.getString("content");
			}
			if (!currentItem.isNull("modified")) {
				noteModified = GregorianCalendar.getInstance();
				noteModified
						.setTimeInMillis(currentItem.getLong("modified") * 1000);
			}
			notesList
					.add(new Note(noteId, noteModified, noteTitle, noteContent));
		}
		return notesList;
	}

    /**
     * Fetches on Note by ID from Server
     * TODO Maybe fetch only id, title and modified from server until a note has been opened?
     * @param id long - ID of the wanted note
     * @return Requested Note
     * @throws JSONException
     * @throws IOException
     */
	@SuppressWarnings("unused")
    public Note getNoteById(long id) throws
            JSONException, IOException {
		long noteId = 0;
		String noteTitle = "";
		String noteContent = "";
		Calendar noteModified = null;
		JSONObject currentItem = new JSONObject(
				requestServer("notes/" + id, "GET", null));

		if (!currentItem.isNull("id")) {
			noteId = currentItem.getLong("id");
		}
		if (!currentItem.isNull("title")) {
			noteTitle = currentItem.getString("title");
		}
		if (!currentItem.isNull("content")) {
			noteContent = currentItem.getString("content");
		}
		if (!currentItem.isNull("modified")) {
			noteModified = GregorianCalendar.getInstance();
			noteModified
					.setTimeInMillis(currentItem.getLong("modified") * 1000);
		}
		return new Note(noteId, noteModified, noteTitle, noteContent);
	}

    /**
     * Creates a Note on the Server
     * @param content String - Content of the new Note
     * @return Created Note including generated Title, ID and lastModified-Date
     * @throws JSONException
     * @throws IOException
     */
	public Note createNote(String content) throws
            JSONException, IOException {
		long noteId = 0;
		String noteTitle = "";
		String noteContent = "";
		Calendar noteModified = null;

		JSONObject paramObject = new JSONObject();
		paramObject.accumulate("content", content);
		JSONObject currentItem = new JSONObject(requestServer("notes", "POST",
				paramObject));

		if (!currentItem.isNull("id")) {
			noteId = currentItem.getLong("id");
		}
		if (!currentItem.isNull("title")) {
			noteTitle = currentItem.getString("title");
		}
		if (!currentItem.isNull("content")) {
			noteContent = currentItem.getString("content");
		}
		if (!currentItem.isNull("modified")) {
			noteModified = GregorianCalendar.getInstance();
			noteModified
					.setTimeInMillis(currentItem.getLong("modified") * 1000);
		}
		return new Note(noteId, noteModified, noteTitle, noteContent);
	}

	public Note editNote(long noteId, String content)
			throws JSONException, IOException {
		String noteTitle = "";
		Calendar noteModified = null;

		JSONObject paramObject = new JSONObject();
		paramObject.accumulate("content", content);
		JSONObject currentItem = new JSONObject(requestServer(
				"notes/" + noteId, "PUT", paramObject));

		if (!currentItem.isNull("title")) {
			noteTitle = currentItem.getString("title");
		}
		if (!currentItem.isNull("modified")) {
			noteModified = GregorianCalendar.getInstance();
			noteModified
					.setTimeInMillis(currentItem.getLong("modified") * 1000);
		}
		return new Note(noteId, noteModified, noteTitle, content);
	}

	public void deleteNote(long noteId) throws
            IOException {
		this.requestServer("notes/" + noteId, "DELETE", null);
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
		String result = "";
		String targetURL = url + "index.php/apps/notes/api/v0.2/" + target;
		Log.v("Note", targetURL);
		HttpURLConnection con = (HttpURLConnection) new URL(targetURL)
				.openConnection();
		con.setRequestMethod(method);
		con.setRequestProperty(
				"Authorization",
				"Basic "
						+ new String(Base64.encode((username + ":"
                        + password).getBytes(), Base64.NO_WRAP)));
        con.setConnectTimeout(10 * 1000); // 10 seconds
		if (params != null) {
            con.setFixedLengthStreamingMode(params.toString().getBytes().length);
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
			Log.v("Note", params.toString());
            os.write(params.toString().getBytes());
            os.flush();
            os.close();
		}
		BufferedReader rd;
		String line;
		rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
		while ((line = rd.readLine()) != null) {
			result += line;
		}
		return result;
	}
}