package it.niedermann.owncloud.notes.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.MalformedURLException;
import java.net.URL;

import it.niedermann.owncloud.notes.R;

import static android.content.Context.CLIPBOARD_SERVICE;

public class ClipboardUtil {

    private static final String TAG = ClipboardUtil.class.getSimpleName();

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

    @Nullable
    public static String getClipboardURLorNull(Context context) {
        final ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        if (clipboardManager == null) {
            return null;
        }
        final ClipData clipboardData = clipboardManager.getPrimaryClip();
        if (clipboardData == null) {
            return null;
        }
        if (clipboardData.getItemCount() < 1) {
            return null;
        }
        final ClipData.Item clipItem = clipboardData.getItemAt(0);
        if (clipItem == null) {
            return null;
        }
        final CharSequence clipText = clipItem.getText();
        if (TextUtils.isEmpty(clipText)) {
            return null;
        }
        try {
            return new URL(clipText.toString()).toString();
        } catch (MalformedURLException e) {
            Log.d(TAG, "Clipboard does not contain a valid URL: " + clipText);
        }
        return null;
    }
}
