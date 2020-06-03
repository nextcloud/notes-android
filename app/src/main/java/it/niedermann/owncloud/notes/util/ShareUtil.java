package it.niedermann.owncloud.notes.util;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class ShareUtil {
    public static void openShareDialog(@NonNull Context context, @Nullable String subject, @Nullable String text) {
        context.startActivity(Intent.createChooser(new Intent()
                .setAction(Intent.ACTION_SEND)
                .setType(MIMETYPE_TEXT_PLAIN)
                .putExtra(Intent.EXTRA_SUBJECT, subject)
                .putExtra(Intent.EXTRA_TITLE, subject)
                .putExtra(Intent.EXTRA_TEXT, text), subject));
    }
}
