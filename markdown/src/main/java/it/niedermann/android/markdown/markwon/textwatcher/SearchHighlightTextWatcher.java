package it.niedermann.android.markdown.markwon.textwatcher;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import it.niedermann.android.markdown.MarkdownUtil;
import it.niedermann.android.markdown.R;
import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor;
import it.niedermann.android.markdown.markwon.MarkwonMarkdownUtil;
import it.niedermann.android.markdown.model.SearchSpan;

public class SearchHighlightTextWatcher extends InterceptorTextWatcher {

    private final MarkwonMarkdownEditor editText;
    @Nullable
    private CharSequence searchText;
    private Integer current;
    @ColorInt
    private int color;
    @ColorInt
    private final int highlightColor;
    private final boolean darkTheme;

    public SearchHighlightTextWatcher(@NonNull TextWatcher originalWatcher, @NonNull MarkwonMarkdownEditor editText) {
        super(originalWatcher);
        this.editText = editText;
        final Context context = editText.getContext();
        this.color = ContextCompat.getColor(context, R.color.search_color);
        this.highlightColor = ContextCompat.getColor(context, R.color.bg_highlighted);
        this.darkTheme = MarkwonMarkdownUtil.isDarkThemeActive(context);
    }

    public void setSearchText(@Nullable CharSequence searchText, @Nullable Integer current) {
        this.current = current;
        if (TextUtils.isEmpty(searchText)) {
            this.searchText = null;
            final Editable text = editText.getText();
            if (text != null) {
                MarkdownUtil.removeSpans(text, SearchSpan.class);
            }
        } else {
            this.searchText = searchText;
            afterTextChanged(editText.getText());
        }
    }

    public void setSearchColor(@ColorInt int color) {
        this.color = color;
        afterTextChanged(editText.getText());
    }

    @Override
    public void afterTextChanged(Editable s) {
        originalWatcher.afterTextChanged(s);
        if (searchText != null) {
            MarkdownUtil.removeSpans(s, SearchSpan.class);
            MarkdownUtil.searchAndColor(s, searchText, current, color, highlightColor, darkTheme);
        }
    }
}
