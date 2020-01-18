package it.niedermann.owncloud.notes.util;

import android.content.Context;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yydcdut.markdown.loader.DefaultLoader;

import java.io.IOException;

/**
 * Extends Default Loader with Base64 capabilities
 */
public class NotesImageLoader extends DefaultLoader {

    /**
     * Constructor
     *
     * @param context Context
     */
    NotesImageLoader(Context context) {
        super(context);
    }

    @Nullable
    @Override
    public byte[] loadSync(@NonNull String url) throws IOException {
        byte[] bytes = super.loadSync(url);
        if (bytes == null && url.trim().startsWith("data:image/")) {
            return base64(url);
        }
        return bytes;
    }

    @Nullable
    private static byte[] base64(@NonNull String url) {
        String content = url.substring(url.indexOf(","));
        return Base64.decode(content, Base64.DEFAULT);
    }
}
