package it.niedermann.owncloud.notes.edit;

import static androidx.lifecycle.Transformations.distinctUntilChanged;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

import it.niedermann.android.markdown.MarkdownEditor;

public class PlainTextViewerImpl  extends AppCompatTextView implements MarkdownEditor {

    private static final String TAG = PlainTextViewerImpl.class.getSimpleName();

    @Nullable
    private Consumer<CharSequence> listener = null;
    private final MutableLiveData<CharSequence> unrenderedText$ = new MutableLiveData<>();

    private final ExecutorService renderService;

    public PlainTextViewerImpl(@NonNull Context context) {
        this(context, null);
    }

    public PlainTextViewerImpl(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public PlainTextViewerImpl(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.renderService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void registerOnLinkClickCallback(@NonNull Function<String, Boolean> callback) {
    }

    @Override
    public void setEnabled(boolean enabled) {
    }

    @Override
    public void setMarkdownString(CharSequence text) {
        setMarkdownString(text, null);
    }

    @Override
    public void setMarkdownString(CharSequence text, Runnable afterRender) {
        final var previousText = this.unrenderedText$.getValue();
        this.unrenderedText$.setValue(text);
        if (listener != null) {
            listener.accept(text);
        }
        if (TextUtils.isEmpty(text)) {
            setText(text);
        } else {
            if (!text.equals(previousText)) {
                this.renderService.execute(() -> post(() -> {
                    if (afterRender != null) {
                        afterRender.run();
                    }
                }));
            }
        }
    }

    @Override
    public void setSearchColor(@ColorInt int color) {
    }

    @Override
    public void setSearchText(@Nullable CharSequence searchText, @Nullable Integer current) {
    }

    @Override
    public void setMarkdownStringAndHighlightMentions(CharSequence text, @NonNull Map<String, String> mentions) {
        setMarkdownString(text);
    }

    @Override
    public LiveData<CharSequence> getMarkdownString() {
        return distinctUntilChanged(this.unrenderedText$);
    }

    @Override
    public void setMarkdownStringChangedListener(@Nullable Consumer<CharSequence> listener) {
        this.listener = listener;
    }
}
