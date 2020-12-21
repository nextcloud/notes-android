package it.niedermann.android.markdown.markwon;

import android.content.Context;
import android.os.Build;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import io.noties.markwon.Markwon;
import io.noties.markwon.editor.MarkwonEditor;
import io.noties.markwon.editor.handler.EmphasisEditHandler;
import io.noties.markwon.editor.handler.StrongEmphasisEditHandler;
import it.niedermann.android.markdown.MarkdownEditor;
import it.niedermann.android.markdown.markwon.format.ContextBasedFormattingCallback;
import it.niedermann.android.markdown.markwon.format.ContextBasedRangeFormattingCallback;
import it.niedermann.android.markdown.markwon.handler.BlockQuoteEditHandler;
import it.niedermann.android.markdown.markwon.handler.CodeBlockEditHandler;
import it.niedermann.android.markdown.markwon.handler.CodeEditHandler;
import it.niedermann.android.markdown.markwon.handler.HeadingEditHandler;
import it.niedermann.android.markdown.markwon.handler.StrikethroughEditHandler;
import it.niedermann.android.markdown.markwon.textwatcher.CombinedTextWatcher;
import it.niedermann.android.markdown.markwon.textwatcher.SearchHighlightTextWatcher;

public class MarkwonMarkdownEditor extends AppCompatEditText implements MarkdownEditor {

    private static final String TAG = MarkwonMarkdownEditor.class.getSimpleName();

    private final MutableLiveData<CharSequence> unrenderedText$ = new MutableLiveData<>();
    private final CombinedTextWatcher combinedWatcher;

    public MarkwonMarkdownEditor(@NonNull Context context) {
        this(context, null);
    }

    public MarkwonMarkdownEditor(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public MarkwonMarkdownEditor(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final Markwon markwon = MarkwonMarkdownUtil.initMarkwonEditor(context).build();
        final MarkwonEditor editor = MarkwonEditor.builder(markwon)
                .useEditHandler(new EmphasisEditHandler())
                .useEditHandler(new StrongEmphasisEditHandler())
                .useEditHandler(new StrikethroughEditHandler())
                .useEditHandler(new CodeEditHandler())
                .useEditHandler(new CodeBlockEditHandler())
                .useEditHandler(new BlockQuoteEditHandler())
                .useEditHandler(new HeadingEditHandler())
                .build();
        combinedWatcher = new CombinedTextWatcher(editor, this);
        addTextChangedListener(combinedWatcher);
        setCustomSelectionActionModeCallback(new ContextBasedRangeFormattingCallback(this));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setCustomInsertionActionModeCallback(new ContextBasedFormattingCallback(this));
        }
    }

    @Override
    public void setSearchColor(@ColorInt int color) {
        final SearchHighlightTextWatcher searchHighlightTextWatcher = combinedWatcher.get(SearchHighlightTextWatcher.class);
        if (searchHighlightTextWatcher == null) {
            Log.w(TAG, SearchHighlightTextWatcher.class.getSimpleName() + " is not a registered " + TextWatcher.class.getSimpleName());
        } else {
            searchHighlightTextWatcher.setSearchColor(color);
        }
    }

    @Override
    public void setSearchText(@Nullable CharSequence searchText, @Nullable Integer current) {
        final SearchHighlightTextWatcher searchHighlightTextWatcher = combinedWatcher.get(SearchHighlightTextWatcher.class);
        if (searchHighlightTextWatcher == null) {
            Log.w(TAG, SearchHighlightTextWatcher.class.getSimpleName() + " is not a registered " + TextWatcher.class.getSimpleName());
        } else {
            searchHighlightTextWatcher.setSearchText(searchText, current);
        }
    }

    @Override
    public void setMarkdownString(CharSequence text) {
        setText(text);
        setMarkdownStringModel(text);
    }

    /**
     * Updates the current model which matches the rendered state of the editor *without* triggering
     * anything of the native {@link EditText}
     */
    public void setMarkdownStringModel(CharSequence text) {
        unrenderedText$.setValue(text == null ? "" : text.toString());
    }

    @Override
    public LiveData<CharSequence> getMarkdownString() {
        return unrenderedText$;
    }
}
