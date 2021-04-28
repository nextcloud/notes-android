package it.niedermann.owncloud.notes.persistence.sync;


import java.util.List;

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
 * @link <a href="https://deck.readthedocs.io/en/latest/API/">Deck REST API</a>
 */
public interface NextcloudAPI {

    @GET("notes")
    Call<List<Note>> getNotes(@Query(value = "pruneBefore") long lastModified, @Query("If-None-Match") String lastETag);

    @POST("notes")
    Call<Note> createNote(@Body Note note);

    @PUT("notes/{remoteId}")
    Call<Note> editNote(@Body Note note, @Path("remoteId") long remoteId);

    @DELETE("notes/{remoteId}")
    Call<Note> deleteNote(@Path("remoteId") long noteId);

}
