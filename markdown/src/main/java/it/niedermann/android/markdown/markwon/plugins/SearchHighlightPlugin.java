package it.niedermann.android.markdown.markwon.plugins;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonPlugin;
import it.niedermann.android.markdown.R;
import it.niedermann.android.markdown.markwon.MarkwonMarkdownUtil;
import it.niedermann.android.markdown.markwon.span.SearchSpan;

public class SearchHighlightPlugin extends AbstractMarkwonPlugin {

    private static final String TAG = SearchHighlightPlugin.class.getSimpleName();

    @Nullable
    private CharSequence searchText = null;
    private Integer current;
    private int color;

    public SearchHighlightPlugin(@NonNull Context context) {
        color = ContextCompat.getColor(context, R.color.search_color);
    }

    public static MarkwonPlugin create(@NonNull Context context) {
        return new SearchHighlightPlugin(context);
    }

    public void setSearchText(@Nullable CharSequence searchText, @Nullable Integer current, @NonNull TextView textView) {
        this.current = current;
        MarkwonMarkdownUtil.removeSpans(getContentAsSpannable(textView), SearchSpan.class);
        if (TextUtils.isEmpty(searchText)) {
            this.searchText = null;
        } else {
            this.searchText = searchText;
            afterSetText(textView);
        }
    }

    public void setSearchColor(@ColorInt int color, @NonNull TextView textView) {
        this.color = color;
        afterSetText(textView);
    }

    @Override
    public void afterSetText(@NonNull TextView textView) {
        super.afterSetText(textView);
        if (this.searchText != null) {
            final Spannable spannable = getContentAsSpannable(textView);
            MarkwonMarkdownUtil.searchAndColor(spannable, searchText, textView.getContext(), current, color);
        }
    }

    /**
     * @return When the content of the {@param textView} is already of type {@link Spannable}, it will cast and return it directly.
     * Otherwise it will create a new {@link SpannableString} from the content, set this as new content of the {@param textView} and return it.
     */
    private static Spannable getContentAsSpannable(@NonNull TextView textView) {
        final CharSequence content = textView.getText();
        if (content.getClass() == SpannableString.class || content instanceof Spannable) {
            return (Spannable) content;
        } else {
            Log.w(TAG, "Expected " + TextView.class.getSimpleName() + " content to be of type " + Spannable.class.getSimpleName() + ", but was of type " + content.getClass() + ". Search highlighting will be not performant.");
            final Spannable spannableContent = new SpannableString(content);
            textView.setText(spannableContent, TextView.BufferType.SPANNABLE);
            return spannableContent;
        }
    }
}
