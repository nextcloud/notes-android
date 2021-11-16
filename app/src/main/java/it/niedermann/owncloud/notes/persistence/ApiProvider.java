package it.niedermann.owncloud.notes.persistence;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import it.niedermann.owncloud.notes.persistence.sync.CapabilitiesDeserializer;
import it.niedermann.owncloud.notes.persistence.sync.NotesAPI;
import it.niedermann.owncloud.notes.persistence.sync.OcsAPI;
import it.niedermann.owncloud.notes.shared.model.ApiVersion;
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import retrofit2.NextcloudRetrofitApiBuilder;
import retrofit2.Retrofit;

/**
 * Since creating APIs via {@link Retrofit} uses reflection and {@link NextcloudAPI} <a href="https://github.com/nextcloud/Android-SingleSignOn/issues/120#issuecomment-540069990">is supposed to stay alive as long as possible</a>, those artifacts are going to be cached.
 * They can be invalidated by using either {@link #invalidateAPICache()} for all or {@link #invalidateAPICache(SingleSignOnAccount)} for a specific {@link SingleSignOnAccount} and will be recreated when they are queried the next time.
 */
@WorkerThread
public class ApiProvider {

    private static final String TAG = ApiProvider.class.getSimpleName();

    private static final ApiProvider INSTANCE = new ApiProvider();

    private static final String API_ENDPOINT_OCS = "/ocs/v2.php/cloud/";

    private static final Map<String, NextcloudAPI> API_CACHE = new ConcurrentHashMap<>();

    private static final Map<String, OcsAPI> API_CACHE_OCS = new ConcurrentHashMap<>();
    private static final Map<String, NotesAPI> API_CACHE_NOTES = new ConcurrentHashMap<>();

    public static ApiProvider getInstance() {
        return INSTANCE;
    }

    private ApiProvider() {
        // Singleton
    }

    /**
     * An {@link OcsAPI} currently shares the {@link Gson} configuration with the {@link NotesAPI} and therefore divides all {@link Calendar} milliseconds by 1000 while serializing and multiplies values by 1000 during deserialization.
     */
    public synchronized OcsAPI getOcsAPI(@NonNull Context context, @NonNull SingleSignOnAccount ssoAccount) {
        if (API_CACHE_OCS.containsKey(ssoAccount.name)) {
            return API_CACHE_OCS.get(ssoAccount.name);
        }
        final var ocsAPI = new NextcloudRetrofitApiBuilder(getNextcloudAPI(context, ssoAccount), API_ENDPOINT_OCS).create(OcsAPI.class);
        API_CACHE_OCS.put(ssoAccount.name, ocsAPI);
        return ocsAPI;
    }

    /**
     * In case the {@param preferredApiVersion} changes, call {@link #invalidateAPICache(SingleSignOnAccount)} or {@link #invalidateAPICache()} to make sure that this call returns a {@link NotesAPI} that uses the correct compatibility layer.
     */
    public synchronized NotesAPI getNotesAPI(@NonNull Context context, @NonNull SingleSignOnAccount ssoAccount, @Nullable ApiVersion preferredApiVersion) {
        if (API_CACHE_NOTES.containsKey(ssoAccount.name)) {
            return API_CACHE_NOTES.get(ssoAccount.name);
        }
        final var notesAPI = new NotesAPI(getNextcloudAPI(context, ssoAccount), preferredApiVersion);
        API_CACHE_NOTES.put(ssoAccount.name, notesAPI);
        return notesAPI;
    }

    private synchronized NextcloudAPI getNextcloudAPI(@NonNull Context context, @NonNull SingleSignOnAccount ssoAccount) {
        if (API_CACHE.containsKey(ssoAccount.name)) {
            return API_CACHE.get(ssoAccount.name);
        } else {
            Log.v(TAG, "NextcloudRequest account: " + ssoAccount.name);
            final var nextcloudAPI = new NextcloudAPI(context.getApplicationContext(), ssoAccount,
                    new GsonBuilder()
                            .excludeFieldsWithoutExposeAnnotation()
                            .registerTypeHierarchyAdapter(Calendar.class, (JsonSerializer<Calendar>) (src, typeOfSrc, ctx) -> new JsonPrimitive(src.getTimeInMillis() / 1_000))
                            .registerTypeHierarchyAdapter(Calendar.class, (JsonDeserializer<Calendar>) (src, typeOfSrc, ctx) -> {
                                final var calendar = Calendar.getInstance();
                                calendar.setTimeInMillis(src.getAsLong() * 1_000);
                                return calendar;
                            })
                            .registerTypeAdapter(Capabilities.class, new CapabilitiesDeserializer())
                            .create(), (e) -> {
                invalidateAPICache(ssoAccount);
                e.printStackTrace();
            });
            API_CACHE.put(ssoAccount.name, nextcloudAPI);
            return nextcloudAPI;
        }
    }

    /**
     * Invalidates the API cache for the given {@param ssoAccount}
     *
     * @param ssoAccount the ssoAccount for which the API cache should be cleared.
     */
    public synchronized void invalidateAPICache(@NonNull SingleSignOnAccount ssoAccount) {
        Log.v(TAG, "Invalidating API cache for " + ssoAccount.name);
        if (API_CACHE.containsKey(ssoAccount.name)) {
            final var nextcloudAPI = API_CACHE.get(ssoAccount.name);
            if (nextcloudAPI != null) {
                nextcloudAPI.stop();
            }
            API_CACHE.remove(ssoAccount.name);
        }
        API_CACHE_NOTES.remove(ssoAccount.name);
        API_CACHE_OCS.remove(ssoAccount.name);
    }

    /**
     * Invalidates the whole API cache for all accounts
     */
    public synchronized void invalidateAPICache() {
        for (final String key : API_CACHE.keySet()) {
            Log.v(TAG, "Invalidating API cache for " + key);
            if (API_CACHE.containsKey(key)) {
                final var nextcloudAPI = API_CACHE.get(key);
                if (nextcloudAPI != null) {
                    nextcloudAPI.stop();
                }
                API_CACHE.remove(key);
            }
        }
        API_CACHE_NOTES.clear();
        API_CACHE_OCS.clear();
    }
}
