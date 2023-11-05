package it.niedermann.owncloud.notes.edit;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.function.Consumer;

import it.niedermann.android.markdown.MarkdownEditor;

public class PlainTextEditorImpl  extends AppCompatEditText implements MarkdownEditor {

    private static final String TAG = PlainTextEditorImpl.class.getSimpleName();

    @Nullable
    private Consumer<CharSequence> listener;
    private final MutableLiveData<CharSequence> unrenderedText$ = new MutableLiveData<>();
    public PlainTextEditorImpl(@NonNull Context context) {
        this(context, null);
    }

    public PlainTextEditorImpl(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public PlainTextEditorImpl(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setSearchColor(@ColorInt int color) {
    }

    @Override
    public void setSearchText(@Nullable CharSequence searchText, @Nullable Integer current) {
    }

    @Override
    public void setMarkdownString(CharSequence text) {
        setText(text);
        setMarkdownStringModel(text);
    }

    @Override
    public void setMarkdownString(CharSequence text, Runnable afterRender) {
        throw new UnsupportedOperationException("This is not available in " + PlainTextEditorImpl.class.getSimpleName() + " because the text is getting rendered all the time.");
    }

    /**
     * Updates the current model which matches the rendered state of the editor *without* triggering
     * anything of the native {@link EditText}
     */
    public void setMarkdownStringModel(CharSequence text) {
        unrenderedText$.setValue(text == null ? "" : text.toString());
        if (listener != null) {
            listener.accept(text);
        }
    }

    @Override
    public LiveData<CharSequence> getMarkdownString() {
        return unrenderedText$;
    }

    @Override
    public void setMarkdownStringChangedListener(@Nullable Consumer<CharSequence> listener) {
        this.listener = listener;
    }}
