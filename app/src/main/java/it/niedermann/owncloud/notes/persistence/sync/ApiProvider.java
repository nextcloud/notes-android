package it.niedermann.owncloud.notes.persistence.sync;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.GsonBuilder;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import it.niedermann.owncloud.notes.persistence.SSOClient;
import retrofit2.NextcloudRetrofitApiBuilder;

/**
 * Created by david on 26.05.17.
 */
public class ApiProvider {

    private static final String API_ENDPOINT_OCS = "/ocs/v2.php/cloud/";
    private static final String API_ENDPOINT_NOTES = "/index.php/apps/notes/api/v1/";

    private final OcsAPI ocsAPI;
    private final NotesAPI notesAPI;

    public ApiProvider(@NonNull Context appContext, @NonNull SingleSignOnAccount ssoAccount) {
        final NextcloudAPI nextcloudAPI = SSOClient.getNextcloudAPI(appContext, ssoAccount);
        ocsAPI = new NextcloudRetrofitApiBuilder(nextcloudAPI, API_ENDPOINT_OCS).create(OcsAPI.class);
        notesAPI = new NextcloudRetrofitApiBuilder(nextcloudAPI, API_ENDPOINT_NOTES).create(NotesAPI.class);
    }

    public NotesAPI getNotesAPI() {
        return notesAPI;
    }

    public OcsAPI getOcsAPI() {
        return ocsAPI;
    }
}
