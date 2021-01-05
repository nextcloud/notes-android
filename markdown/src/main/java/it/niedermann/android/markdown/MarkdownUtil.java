package it.niedermann.android.markdown;

import android.content.Context;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.RemoteViews.RemoteView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;

import com.yydcdut.markdown.MarkdownProcessor;
import com.yydcdut.markdown.syntax.text.TextFactory;
import com.yydcdut.rxmarkdown.RxMarkdown;

import io.noties.markwon.Markwon;

public class MarkdownUtil {
    private static final String MD_IMAGE_WITH_EMPTY_DESCRIPTION = "![](";
    private static final String MD_IMAGE_WITH_SPACE_DESCRIPTION = "![ ](";
    private static final String[] MD_IMAGE_WITH_EMPTY_DESCRIPTION_ARRAY = new String[]{MD_IMAGE_WITH_EMPTY_DESCRIPTION};
    private static final String[] MD_IMAGE_WITH_SPACE_DESCRIPTION_ARRAY = new String[]{MD_IMAGE_WITH_SPACE_DESCRIPTION};

    private MarkdownUtil() {
        // Util class
    }

    /**
     * {@link RemoteView}s have a limited subset of supported classes to maintain compatibility with many different launchers.
     * <p>
     * Since {@link Markwon} makes heavy use of custom spans, this won't look nice e. g. at app widgets, because they simply won't be rendered.
     * Therefore we currently fall back on {@link RxMarkdown} as the results will look better in this special case.
     * We might change this in the future by utilizing {@link Markwon} and creating a {@link Spanned} from an {@link HtmlCompat} interemediate.
     */
    public static CharSequence renderForRemoteView(@NonNull Context context, @NonNull CharSequence content) {
        final MarkdownProcessor markdownProcessor = new MarkdownProcessor(context);
        markdownProcessor.factory(TextFactory.create());
        return parseCompat(markdownProcessor, content);
    }

    /**
     * This is a compatibility-method that provides workarounds for several bugs in RxMarkdown
     * <p>
     * https://github.com/stefan-niedermann/nextcloud-notes/issues/772
     *
     * @param markdownProcessor RxMarkdown MarkdownProcessor instance
     * @param text              CharSequence that should be parsed
     * @return the processed text but with several workarounds for Bugs in RxMarkdown
     */
    @NonNull
    private static CharSequence parseCompat(@NonNull final MarkdownProcessor markdownProcessor, CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }

        while (TextUtils.indexOf(text, MD_IMAGE_WITH_EMPTY_DESCRIPTION) >= 0) {
            text = TextUtils.replace(text, MD_IMAGE_WITH_EMPTY_DESCRIPTION_ARRAY, MD_IMAGE_WITH_SPACE_DESCRIPTION_ARRAY);
        }

        return markdownProcessor.parse(text);
    }
}
