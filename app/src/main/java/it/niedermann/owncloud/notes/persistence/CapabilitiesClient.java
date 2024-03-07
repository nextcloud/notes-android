/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.nextcloud.android.sso.api.ParsedResponse;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.util.Map;

import it.niedermann.owncloud.notes.persistence.sync.OcsAPI;
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import it.niedermann.owncloud.notes.shared.model.OcsResponse;
import it.niedermann.owncloud.notes.shared.model.OcsUser;
import retrofit2.Response;

@WorkerThread
public class CapabilitiesClient {

    private static final String TAG = CapabilitiesClient.class.getSimpleName();

    private static final String HEADER_KEY_ETAG = "ETag";

    @WorkerThread
    public static Capabilities getCapabilities(@NonNull Context context, @NonNull SingleSignOnAccount ssoAccount, @Nullable String lastETag, @NonNull ApiProvider apiProvider) throws Throwable {
        final var ocsAPI = apiProvider.getOcsAPI(context, ssoAccount);
        try {
            final var response = ocsAPI.getCapabilities(lastETag).blockingSingle();
            final var capabilities = response.getResponse().ocs.data;
            final var headers = response.getHeaders();
            if (headers != null) {
                capabilities.setETag(headers.get(HEADER_KEY_ETAG));
            } else {
                Log.w(TAG, "Response headers of capabilities are null");
            }
            return capabilities;
        } catch (RuntimeException e) {
            final var cause = e.getCause();
            if (cause != null) {
                throw cause;
            } else {
                throw e;
            }
        }
    }

    @WorkerThread
    @Nullable
    public static String getDisplayName(@NonNull Context context, @NonNull SingleSignOnAccount ssoAccount, @NonNull ApiProvider apiProvider) {
        final var ocsAPI = apiProvider.getOcsAPI(context, ssoAccount);
        try {
            final var userResponse = ocsAPI.getUser(ssoAccount.userId).execute();
            if (userResponse.isSuccessful()) {
                final var ocsResponse = userResponse.body();
                if (ocsResponse != null) {
                    return ocsResponse.ocs.data.displayName;
                } else {
                    Log.w(TAG, "ocsResponse is null");
                }
            } else {
                Log.w(TAG, "Fetching user was not successful.");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }
}
