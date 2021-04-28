package it.niedermann.owncloud.notes.persistence.sync;


import com.google.gson.annotations.Expose;
import com.nextcloud.android.sso.api.ParsedResponse;

import java.util.Calendar;
import java.util.List;

import io.reactivex.Observable;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * @link <a href="https://github.com/nextcloud/notes/wiki/API-0.2">Notes API v0.2</a>
 */
public interface NotesAPI_0_2 {

    @GET("notes")
    Observable<ParsedResponse<List<Note>>> getNotes(@Query(value = "pruneBefore") long lastModified, @Query("If-None-Match") String lastETag);

    default Call<Note> createNote(@Body Note note) {
        return createNote(new Note_0_2(note));
    }

    @POST("notes")
    Call<Note> createNote(@Body Note_0_2 note);

    @PUT("notes/{remoteId}")
    Call<Note> editNote(@Body Note note, @Path("remoteId") long remoteId);

    @DELETE("notes/{remoteId}")
    Call<Note> deleteNote(@Path("remoteId") long noteId);

    class Note_0_2 {
        @Expose
        public final long id;
        @Expose
        public final String title;
        @Expose
        public final String category;
        @Expose
        public final Calendar modified;
        @Expose
        public final String content;
        @Expose
        public final boolean favorite;
        @Expose
        public final String eTag;

        private Note_0_2(Note note) {
            if (note == null) {
                throw new IllegalArgumentException(Note.class.getSimpleName() + " can not be converted to " + Note_0_2.class.getSimpleName() + " because it is null.");
            }
            this.id = note.getRemoteId();
            this.title = note.getTitle();
            this.category = note.getCategory();
            this.modified = note.getModified();
            this.content = note.getContent();
            this.favorite = note.getFavorite();
            this.eTag = note.getETag();
        }
    }
}
