/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.sync;

import com.nextcloud.android.sso.api.EmptyResponse;
import com.nextcloud.android.sso.api.ParsedResponse;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.NotesSettings;
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
    Observable<ParsedResponse<List<Note>>> getNotes(@Query("pruneBefore") long lastModified, @Header("If-None-Match") String lastETag);

    @GET("notes?exclude=etag,readonly,content,title,category,favorite,modified")
    Observable<ParsedResponse<List<Note>>> getNotesIDs();

    @POST("notes")
    Call<Note> createNote(@Body Note note);

    @GET("notes/{remoteId}")
    Observable<ParsedResponse<Note>> getNote(@Path("remoteId") long remoteId);

    @PUT("notes/{remoteId}")
    Call<Note> editNote(@Body Note note, @Path("remoteId") long remoteId);

    @DELETE("notes/{remoteId}")
    Call<EmptyResponse> deleteNote(@Path("remoteId") long noteId);

    @GET("settings")
    Call<NotesSettings> getSettings();

    @PUT("settings")
    Call<NotesSettings> putSettings(@Body NotesSettings settings);

    @PUT("notes/{id}")
    Call<Note> updateNote(@Path("id") long id, @Body Note note);
}
