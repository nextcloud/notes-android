package it.niedermann.android.markdown.markwon.plugins;

import android.content.Context;
import android.text.Spannable;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonPlugin;
import it.niedermann.android.markdown.MarkdownUtil;
import it.niedermann.android.markdown.R;
import it.niedermann.android.markdown.markwon.MarkwonMarkdownUtil;
import it.niedermann.android.markdown.model.SearchSpan;

import static it.niedermann.android.markdown.MarkdownUtil.getContentAsSpannable;

public class SearchHighlightPlugin extends AbstractMarkwonPlugin {

    @Nullable
    private CharSequence searchText = null;
    private Integer current;
    @ColorInt
    private int color;
    @ColorInt
    private final int highlightColor;
    private final boolean darkTheme;

    public SearchHighlightPlugin(@NonNull Context context) {
        this.color = ContextCompat.getColor(context, R.color.search_color);
        this.highlightColor = ContextCompat.getColor(context, R.color.bg_highlighted);
        this.darkTheme = MarkwonMarkdownUtil.isDarkThemeActive(context);
    }

    public static MarkwonPlugin create(@NonNull Context context) {
        return new SearchHighlightPlugin(context);
    }

    public void setSearchText(@Nullable CharSequence searchText, @Nullable Integer current, @NonNull TextView textView) {
        this.current = current;
        MarkdownUtil.removeSpans(getContentAsSpannable(textView), SearchSpan.class);
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
            MarkdownUtil.searchAndColor(spannable, searchText, current, color, highlightColor, darkTheme);
        }
    }
}
