package it.niedermann.owncloud.notes.glide;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.google.gson.GsonBuilder;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;

import java.io.InputStream;

/**
 * A simple model loader for fetching media over http/https using OkHttp.
 */
public class SingleSignOnUrlLoader implements ModelLoader<GlideUrl, InputStream> {

    private static final String TAG = SingleSignOnUrlLoader.class.getCanonicalName();
    private final NextcloudAPI client;

    // Public API.
    @SuppressWarnings("WeakerAccess")
    public SingleSignOnUrlLoader(@NonNull NextcloudAPI client) {
        this.client = client;
    }

    @Override
    public boolean handles(@NonNull GlideUrl url) {
        return true;
    }

    @Override
    public LoadData<InputStream> buildLoadData(
            @NonNull GlideUrl model, int width, int height, @NonNull Options options) {
        return new LoadData<>(model, new SingleSignOnStreamFetcher(client, model));
    }

    /**
     * The default factory for {@link SingleSignOnUrlLoader}s.
     */
    // Public API.
    @SuppressWarnings("WeakerAccess")
    public static class Factory implements ModelLoaderFactory<GlideUrl, InputStream> {
        private SingleSignOnUrlLoader loader;

        /**
         * Constructor for a new Factory that runs requests using given client.
         */
        public Factory(@NonNull Context context) {
            try {
                loader = new SingleSignOnUrlLoader(new NextcloudAPI(context, SingleAccountHelper.getCurrentSingleSignOnAccount(context), new GsonBuilder().create(), new NextcloudAPI.ApiConnectedListener() {
                    @Override
                    public void onConnected() {
                        Log.v(TAG, "SSO API successfully initialized");
                    }

                    @Override
                    public void onError(Exception ex) {
                        Log.e(TAG, ex.getMessage(), ex);
                    }
                }));
            } catch (NextcloudFilesAppAccountNotFoundException e) {
                e.printStackTrace();
            } catch (NoCurrentAccountSelectedException e) {
                e.printStackTrace();
            }
        }

        @NonNull
        @Override
        public ModelLoader<GlideUrl, InputStream> build(MultiModelLoaderFactory multiFactory) {
            return loader;
        }

        @Override
        public void teardown() {
            // Do nothing, this instance doesn't own the client.
        }
    }
}
