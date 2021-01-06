package it.niedermann.android.markdown.markwon;

import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

public class MarkwonMarkdownUtil {

    private MarkwonMarkdownUtil() {
        // Util class
    }

    public static boolean isDarkThemeActive(@NonNull Context context) {
        final int uiMode = context.getResources().getConfiguration().uiMode;
        return (uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }
}
