package it.niedermann.android.markdown;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.yydcdut.markdown.MarkdownProcessor;
import com.yydcdut.markdown.syntax.text.TextFactory;

public class MarkdownUtil {
    private static final String MD_IMAGE_WITH_EMPTY_DESCRIPTION = "![](";
    private static final String MD_IMAGE_WITH_SPACE_DESCRIPTION = "![ ](";
    private static final String[] MD_IMAGE_WITH_EMPTY_DESCRIPTION_ARRAY = new String[]{MD_IMAGE_WITH_EMPTY_DESCRIPTION};
    private static final String[] MD_IMAGE_WITH_SPACE_DESCRIPTION_ARRAY = new String[]{MD_IMAGE_WITH_SPACE_DESCRIPTION};

    private MarkdownUtil() {
        // Util class
    }

    public static CharSequence renderForWidget(@NonNull Context context, @NonNull CharSequence content) {
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
