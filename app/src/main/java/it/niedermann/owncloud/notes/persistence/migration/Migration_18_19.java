package it.niedermann.owncloud.notes.persistence.migration;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

public class Migration_18_19 {
    private static final String TAG = Migration_18_19.class.getSimpleName();

    /**
     * Clears the {@link Glide} disk cache to fix wrong avatars in a multi user setup
     * https://github.com/stefan-niedermann/nextcloud-deck/issues/531
     *
     * @param context {@link Context}
     */
    public Migration_18_19(@NonNull Context context) {
        new Thread(() -> {
            Log.i(TAG, "Clearing Glide disk cache");
            Glide.get(context.getApplicationContext()).clearDiskCache();
        }).start();
    }
}
