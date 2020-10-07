package it.niedermann.owncloud.notes.persistence.migration;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.bumptech.glide.Glide;

public class Migration_18_19 extends Migration {

    private static final String TAG = Migration_18_19.class.getSimpleName();
    @NonNull
    private final Context context;


    public Migration_18_19(@NonNull Context context) {
        super(18, 19);
        this.context = context;
    }

    /**
     * Clears the {@link Glide} disk cache to fix wrong avatars in a multi user setup
     * https://github.com/stefan-niedermann/nextcloud-deck/issues/531
     */
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase db) {
        new Thread(() -> {
            Log.i(TAG, "Clearing Glide disk cache");
            Glide.get(context.getApplicationContext()).clearDiskCache();
        }).start();
    }
}
