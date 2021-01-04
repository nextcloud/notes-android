package it.niedermann.android.markdown.markwon;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonPlugin;
import it.niedermann.android.markdown.MarkdownEditor;
import it.niedermann.android.markdown.markwon.plugins.LinkClickInterceptorPlugin;
import it.niedermann.android.markdown.markwon.plugins.SearchHighlightPlugin;
import it.niedermann.android.markdown.markwon.plugins.ToggleableTaskListPlugin;

import static androidx.lifecycle.Transformations.distinctUntilChanged;
import static it.niedermann.android.markdown.markwon.MarkwonMarkdownUtil.initMarkwonViewer;

public class MarkwonMarkdownViewer extends AppCompatTextView implements MarkdownEditor {

    private static final String TAG = MarkwonMarkdownViewer.class.getSimpleName();

    private Markwon markwon;
    private final MutableLiveData<CharSequence> unrenderedText$ = new MutableLiveData<>();

    private final ExecutorService renderService;

    public MarkwonMarkdownViewer(@NonNull Context context) {
        this(context, null);
    }

    public MarkwonMarkdownViewer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public MarkwonMarkdownViewer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.markwon = initMarkwonViewer(context)
                .usePlugin(new ToggleableTaskListPlugin((toggledCheckboxPosition, newCheckedState) -> {
                    final CharSequence oldUnrenderedText = unrenderedText$.getValue();
                    if (oldUnrenderedText == null) {
                        throw new IllegalStateException("Checkbox #" + toggledCheckboxPosition + ", but unrenderedText$ value is null.");
                    }
                    final CharSequence newUnrenderedText = MarkwonMarkdownUtil.setCheckboxStatus(oldUnrenderedText.toString(), toggledCheckboxPosition, newCheckedState);
                    this.setMarkdownString(newUnrenderedText);
                }))
                .build();
        this.renderService = Executors.newSingleThreadExecutor();
    }

    public void registerOnLinkClickCallback(@NonNull Function<String, Boolean> callback) {
        this.markwon.getPlugin(LinkClickInterceptorPlugin.class).registerOnLinkClickCallback(callback);
    }

    public void removeOnLinkClickCallback(@NonNull Function<String, Boolean> callback) {
        this.markwon.getPlugin(LinkClickInterceptorPlugin.class).removeOnLinkClickCallback(callback);
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.markwon.getPlugin(ToggleableTaskListPlugin.class).setEnabled(enabled);
    }

    @Override
    public void setMarkdownString(CharSequence text) {
        final CharSequence previousText = this.unrenderedText$.getValue();
        this.unrenderedText$.setValue(text);
        if (TextUtils.isEmpty(text)) {
            setText(text);
        } else {
            if (!text.equals(previousText)) {
                this.renderService.execute(() -> post(() -> this.markwon.setMarkdown(this, text.toString())));
            }
        }
    }

    @Override
    public void setSearchColor(@ColorInt int color) {
        final SearchHighlightPlugin searchHighlightPlugin = this.markwon.getPlugin(SearchHighlightPlugin.class);
        if (searchHighlightPlugin == null) {
            Log.w(TAG, SearchHighlightPlugin.class.getSimpleName() + " is not a registered " + MarkwonPlugin.class.getSimpleName());
        } else {
            searchHighlightPlugin.setSearchColor(color, this);
        }
    }

    @Override
    public void setSearchText(@Nullable CharSequence searchText, @Nullable Integer current) {
        final SearchHighlightPlugin searchHighlightPlugin = this.markwon.getPlugin(SearchHighlightPlugin.class);
        if (searchHighlightPlugin == null) {
            Log.w(TAG, SearchHighlightPlugin.class.getSimpleName() + " is not a registered " + MarkwonPlugin.class.getSimpleName());
        } else {
            searchHighlightPlugin.setSearchText(searchText, current, this);
        }
    }

    @Override
    public void setMarkdownString(CharSequence text, @NonNull Map<String, String> mentions) {
        this.markwon = initMarkwonViewer(getContext(), mentions).build();
        setMarkdownString(text);
    }

    @Override
    public LiveData<CharSequence> getMarkdownString() {
        return distinctUntilChanged(this.unrenderedText$);
    }
}
