package it.niedermann.android.markdown.markwon.plugins;

import android.text.Spannable;
import android.text.style.URLSpan;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Function;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonPlugin;
import it.niedermann.android.markdown.markwon.span.InterceptedURLSpan;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static it.niedermann.android.markdown.MarkdownUtil.getContentAsSpannable;

public class LinkClickInterceptorPlugin extends AbstractMarkwonPlugin {

    @NonNull
    private final Collection<Function<String, Boolean>> onLinkClickCallbacks = new LinkedList<>();

    public static MarkwonPlugin create() {
        return new LinkClickInterceptorPlugin();
    }

    @Override
    public void afterSetText(@NonNull TextView textView) {
        super.afterSetText(textView);
        if (onLinkClickCallbacks.size() > 0) {
            final Spannable spannable = getContentAsSpannable(textView);
            final URLSpan[] spans = spannable.getSpans(0, spannable.length(), URLSpan.class);

            for (URLSpan originalSpan : spans) {
                final InterceptedURLSpan interceptedSpan = new InterceptedURLSpan(onLinkClickCallbacks, originalSpan.getURL());
                final int start = spannable.getSpanStart(originalSpan);
                final int end = spannable.getSpanEnd(originalSpan);
                spannable.removeSpan(originalSpan);
                spannable.setSpan(interceptedSpan, start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    public void registerOnLinkClickCallback(@NonNull Function<String, Boolean> callback) {
        this.onLinkClickCallbacks.add(callback);
    }
}
