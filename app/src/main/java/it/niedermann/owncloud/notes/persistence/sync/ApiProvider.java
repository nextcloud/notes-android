package it.niedermann.owncloud.notes.persistence.sync;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.GsonBuilder;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import retrofit2.NextcloudRetrofitApiBuilder;

/**
 * Created by david on 26.05.17.
 */
public class ApiProvider {

    private static final String API_ENDPOINT_NOTES = "/index.php/apps/notes/api/v1/";

    private final NotesAPI notesAPI;

    public ApiProvider(@NonNull Context context, @NonNull SingleSignOnAccount ssoAccount, @NonNull final NextcloudAPI.ApiConnectedListener callback) {
        final NextcloudAPI nextcloudAPI = new NextcloudAPI(context, ssoAccount, new GsonBuilder().create(), callback);
        notesAPI = new NextcloudRetrofitApiBuilder(nextcloudAPI, API_ENDPOINT_NOTES).create(NotesAPI.class);
    }

    public NotesAPI getNotesAPI() {
        return notesAPI;
    }
}
