package it.niedermann.owncloud.notes.persistence.sync;


import com.nextcloud.android.sso.api.ParsedResponse;

import java.util.List;

import io.reactivex.Observable;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * @link <a href="https://github.com/nextcloud/notes/blob/master/docs/api/README.md">Notes API v1</a>
 */
public interface NotesAPI_1_0 {

    @GET("notes")
    Observable<ParsedResponse<List<Note>>> getNotes(@Query(value = "pruneBefore") long lastModified, @Header("If-None-Match") String lastETag);

    @POST("notes")
    Call<Note> createNote(@Body Note note);

    @PUT("notes/{remoteId}")
    Call<Note> editNote(@Body Note note, @Path("remoteId") long remoteId);

    @DELETE("notes/{remoteId}")
    Call<Void> deleteNote(@Path("remoteId") long noteId);
}
