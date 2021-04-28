package it.niedermann.owncloud.notes.persistence;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import it.niedermann.owncloud.notes.persistence.sync.CapabilitiesDeserializer;
import it.niedermann.owncloud.notes.shared.model.Capabilities;

@SuppressWarnings("WeakerAccess")
@WorkerThread
public class SSOClient {

    private static final String TAG = SSOClient.class.getSimpleName();

    private static final Map<String, NextcloudAPI> mNextcloudAPIs = new HashMap<>();

    public static synchronized NextcloudAPI getNextcloudAPI(@NonNull Context appContext, @NonNull SingleSignOnAccount ssoAccount) {
        if (mNextcloudAPIs.containsKey(ssoAccount.name)) {
            return mNextcloudAPIs.get(ssoAccount.name);
        } else {
            Log.v(TAG, "NextcloudRequest account: " + ssoAccount.name);
            final NextcloudAPI nextcloudAPI = new NextcloudAPI(appContext, ssoAccount,
                    new GsonBuilder()
                            .excludeFieldsWithoutExposeAnnotation()
                            .registerTypeHierarchyAdapter(Calendar.class, (JsonSerializer<Calendar>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getTimeInMillis() / 1_000))
                            .registerTypeHierarchyAdapter(Calendar.class, (JsonDeserializer<Calendar>) (src, typeOfSrc, context) -> {
                                final Calendar calendar = Calendar.getInstance();
                                calendar.setTimeInMillis(src.getAsLong() * 1_000);
                                return calendar;
                            })
                            .registerTypeAdapter(Capabilities.class, new CapabilitiesDeserializer())
                            .create(), new NextcloudAPI.ApiConnectedListener() {
                @Override
                public void onConnected() {
                    Log.i(TAG, "SSO API connected for " + ssoAccount);
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                    SSOClient.invalidateAPICache(ssoAccount);
                }
            });
            mNextcloudAPIs.put(ssoAccount.name, nextcloudAPI);
            return nextcloudAPI;
        }
    }

    /**
     * Invalidates thes API cache for the given ssoAccount
     *
     * @param ssoAccount the ssoAccount for which the API cache should be cleared.
     */
    public static void invalidateAPICache(@NonNull SingleSignOnAccount ssoAccount) {
        Log.v(TAG, "Invalidating API cache for " + ssoAccount.name);
        if (mNextcloudAPIs.containsKey(ssoAccount.name)) {
            final NextcloudAPI nextcloudAPI = mNextcloudAPIs.get(ssoAccount.name);
            if (nextcloudAPI != null) {
                nextcloudAPI.stop();
            }
            mNextcloudAPIs.remove(ssoAccount.name);
        }
    }

    /**
     * Invalidates the whole API cache for all accounts
     */
    public static void invalidateAPICache() {
        for (String key : mNextcloudAPIs.keySet()) {
            Log.v(TAG, "Invalidating API cache for " + key);
            if (mNextcloudAPIs.containsKey(key)) {
                final NextcloudAPI nextcloudAPI = mNextcloudAPIs.get(key);
                if (nextcloudAPI != null) {
                    nextcloudAPI.stop();
                }
                mNextcloudAPIs.remove(key);
            }
        }
    }
}
