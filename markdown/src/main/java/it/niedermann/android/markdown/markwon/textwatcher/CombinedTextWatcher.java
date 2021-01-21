package it.niedermann.android.markdown.markwon.textwatcher;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.concurrent.Executors;

import io.noties.markwon.editor.MarkwonEditor;
import io.noties.markwon.editor.MarkwonEditorTextWatcher;
import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor;

public class CombinedTextWatcher extends HashMap<Class<?>, TextWatcher> implements TextWatcher {

    private final TextWatcher watcher;

    @SuppressWarnings("ConstantConditions")
    public CombinedTextWatcher(@NonNull MarkwonEditor editor, @NonNull MarkwonMarkdownEditor editText) {
        put(MarkwonEditorTextWatcher.class, MarkwonEditorTextWatcher.withPreRender(editor, Executors.newSingleThreadExecutor(), editText));
        put(AutoContinuationTextWatcher.class, new AutoContinuationTextWatcher(get(MarkwonEditorTextWatcher.class), editText));
        put(SearchHighlightTextWatcher.class, new SearchHighlightTextWatcher(get(AutoContinuationTextWatcher.class), editText));
        watcher = get(SearchHighlightTextWatcher.class);
    }

    @SuppressWarnings({"unchecked"})
    @Nullable
    public <T> T get(@Nullable Class<T> key) {
        return (T) super.get(key);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        watcher.beforeTextChanged(s, start, count, after);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        watcher.onTextChanged(s, start, before, count);
    }

    @Override
    public void afterTextChanged(Editable s) {
        watcher.afterTextChanged(s);
    }
}
