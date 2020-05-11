package it.niedermann.owncloud.notes.glide;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.LibraryGlideModule;

import java.io.InputStream;

/**
 * Registers OkHttp related classes via Glide's annotation processor.
 *
 * <p>For Applications that depend on this library and include an {@link LibraryGlideModule} and Glide's
 * annotation processor, this class will be automatically included.
 */
@GlideModule
public final class SingleSignOnLibraryGlideModule extends LibraryGlideModule {

    private static final String TAG = SingleSignOnLibraryGlideModule.class.getSimpleName();

    @Override
    public void registerComponents(
            @NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        Log.v(TAG, "Replacing default implementation for " + GlideUrl.class.getSimpleName() +  ".");
        registry.replace(GlideUrl.class, InputStream.class, new SingleSignOnUrlLoader.Factory(context));
    }
}
