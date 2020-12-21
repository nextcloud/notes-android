package it.niedermann.android.markdown.markwon.textwatcher;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import it.niedermann.android.markdown.R;
import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor;
import it.niedermann.android.markdown.markwon.MarkwonMarkdownUtil;
import it.niedermann.android.markdown.markwon.span.SearchSpan;

public class SearchHighlightTextWatcher extends InterceptorTextWatcher {

    private final MarkwonMarkdownEditor editText;
    @Nullable
    private CharSequence searchText;
    private Integer current;
    private int color;

    public SearchHighlightTextWatcher(@NonNull TextWatcher originalWatcher, @NonNull MarkwonMarkdownEditor editText) {
        super(originalWatcher);
        this.editText = editText;
        this.color = ContextCompat.getColor(editText.getContext(), R.color.search_color);
    }

    public void setSearchText(@Nullable CharSequence searchText, @Nullable Integer current) {
        this.current = current;
        if (TextUtils.isEmpty(searchText)) {
            this.searchText = null;
            MarkwonMarkdownUtil.removeSpans(editText.getText(), SearchSpan.class);
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
            MarkwonMarkdownUtil.removeSpans(s, SearchSpan.class);
            MarkwonMarkdownUtil.searchAndColor(s, searchText, editText.getContext(), current, color);
        }
    }
}
