package it.niedermann.owncloud.notes.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import static android.content.Context.CLIPBOARD_SERVICE;

public class ClipboardUtil {

    private static final String TAG = ClipboardUtil.class.getCanonicalName();

    private ClipboardUtil() {
        // Util class
    }

    public static String getClipboardURLorNull(Context context) {
        String clipboardURL = null;
        ClipData clipboardData = Objects.requireNonNull(((ClipboardManager) Objects.requireNonNull(context.getSystemService(CLIPBOARD_SERVICE))).getPrimaryClip());
        if (clipboardData.getItemCount() > 0) {
            try {
                clipboardURL = new URL(clipboardData.getItemAt(0).getText().toString()).toString();
            } catch (MalformedURLException e) {
                Log.d(TAG, "Clipboard does not contain a valid URL: " + clipboardData.getItemAt(0).getText().toString());
            }
        }
        return clipboardURL;
    }
}
