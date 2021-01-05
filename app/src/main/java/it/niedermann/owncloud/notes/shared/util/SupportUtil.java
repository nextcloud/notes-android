package it.niedermann.owncloud.notes.shared.util;

import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;

/**
 * Some helper functionality in alike the Android support library.
 * Currently, it offers methods for working with HTML string resources.
 */
public class SupportUtil {

    private SupportUtil() {

    }

    /**
     * Fills a {@link TextView} with HTML content and activates links in that {@link TextView}.
     *
     * @param view       The {@link TextView} which should be filled.
     * @param stringId   The string resource containing HTML tags (escaped by <code>&lt;</code>)
     * @param formatArgs Arguments for the string resource.
     */
    public static void setHtml(@NonNull TextView view, int stringId, Object... formatArgs) {
        view.setText(HtmlCompat.fromHtml(
                view.getResources().getString(stringId, formatArgs), HtmlCompat.FROM_HTML_MODE_LEGACY));
        view.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
