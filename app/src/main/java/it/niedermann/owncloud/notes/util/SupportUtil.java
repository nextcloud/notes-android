package it.niedermann.owncloud.notes.util;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

/**
 * Some helper functionality in alike the Android support library.
 * Currently, it offers methods for working with HTML string resources.
 */
public class SupportUtil {

    /**
     * Creates a {@link Spanned} from a HTML string on all SDK versions.
     * @see Html#fromHtml(String)
     * @see Html#fromHtml(String, int)
     * @param source Source string with HTML markup
     * @return Spannable for using in a {@link TextView}
     */
    public static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= 24) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    /**
     * Fills a {@link TextView} with HTML content and activates links in that {@link TextView}.
     * @param view          The {@link TextView} which should be filled.
     * @param stringId      The string resource containing HTML tags (escaped by <code>&lt;</code>)
     * @param formatArgs    Arguments for the string resource.
     */
    public static void setHtml(TextView view, int stringId, Object... formatArgs) {
        view.setText(SupportUtil.fromHtml(view.getResources().getString(stringId, formatArgs)));
        view.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
