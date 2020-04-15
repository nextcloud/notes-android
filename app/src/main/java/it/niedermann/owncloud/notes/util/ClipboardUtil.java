package it.niedermann.owncloud.notes.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import it.niedermann.owncloud.notes.R;

import static android.content.Context.CLIPBOARD_SERVICE;

public class ClipboardUtil {

    private static final String TAG = ClipboardUtil.class.getCanonicalName();

    private ClipboardUtil() {
        // Util class
    }

    public static boolean copyToClipboard(@NonNull Context context, @Nullable String text) {
        return copyToClipboard(context, text, text);
    }

    public static boolean copyToClipboard(@NonNull Context context, @Nullable String label, @Nullable String text) {
        final ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        if (clipboardManager == null) {
            Log.e(TAG, "ClipboardManager is null");
            Toast.makeText(context, R.string.could_not_copy_to_clipboard, Toast.LENGTH_LONG).show();
            return false;
        }
        final ClipData clipData = ClipData.newPlainText(label, text);
        clipboardManager.setPrimaryClip(clipData);
        Log.i(TAG, "Copied to clipboard: [" + label + "] \"" + text + "\"");
        Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
        return true;
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
