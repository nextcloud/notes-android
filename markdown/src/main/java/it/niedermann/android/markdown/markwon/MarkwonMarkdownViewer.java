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
import java.util.function.Consumer;
import java.util.function.Function;

import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonPlugin;
import io.noties.markwon.SoftBreakAddsNewLinePlugin;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;
import io.noties.markwon.image.DefaultDownScalingMediaDecoder;
import io.noties.markwon.image.ImagesPlugin;
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import io.noties.markwon.simple.ext.SimpleExtPlugin;
import io.noties.markwon.syntax.Prism4jTheme;
import io.noties.markwon.syntax.Prism4jThemeDarkula;
import io.noties.markwon.syntax.Prism4jThemeDefault;
import io.noties.markwon.syntax.SyntaxHighlightPlugin;
import io.noties.prism4j.Prism4j;
import io.noties.prism4j.annotations.PrismBundle;
import it.niedermann.android.markdown.MarkdownEditor;
import it.niedermann.android.markdown.MarkdownUtil;
import it.niedermann.android.markdown.markwon.plugins.LinkClickInterceptorPlugin;
import it.niedermann.android.markdown.markwon.plugins.NextcloudMentionsPlugin;
import it.niedermann.android.markdown.markwon.plugins.SearchHighlightPlugin;
import it.niedermann.android.markdown.markwon.plugins.ThemePlugin;
import it.niedermann.android.markdown.markwon.plugins.ToggleableTaskListPlugin;

import static androidx.lifecycle.Transformations.distinctUntilChanged;

@PrismBundle(includeAll = true, grammarLocatorClassName = ".MarkwonGrammarLocator")
public class MarkwonMarkdownViewer extends AppCompatTextView implements MarkdownEditor {

    private static final String TAG = MarkwonMarkdownViewer.class.getSimpleName();

    private static final Prism4j prism4j = new Prism4j(new MarkwonGrammarLocator());

    private Markwon markwon;
    @Nullable
    private Consumer<CharSequence> listener = null;
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
        this.markwon = createMarkwonBuilder(context).build();
        this.renderService = Executors.newSingleThreadExecutor();
    }

    private Markwon.Builder createMarkwonBuilder(@NonNull Context context) {
        final Prism4jTheme prism4jTheme = MarkwonMarkdownUtil.isDarkThemeActive(context)
                ? Prism4jThemeDarkula.create()
                : Prism4jThemeDefault.create();
        return Markwon.builder(context)
                .usePlugin(ThemePlugin.create(context))
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(SimpleExtPlugin.create())
                .usePlugin(MarkwonInlineParserPlugin.create())
                .usePlugin(SearchHighlightPlugin.create(context))
                .usePlugin(TablePlugin.create(context))
                .usePlugin(TaskListPlugin.create(context))
                .usePlugin(LinkifyPlugin.create(true))
                .usePlugin(LinkClickInterceptorPlugin.create())
                .usePlugin(ImagesPlugin.create(plugin -> plugin.defaultMediaDecoder(DefaultDownScalingMediaDecoder.create(context.getResources().getDisplayMetrics().widthPixels, 0))))
                .usePlugin(SoftBreakAddsNewLinePlugin.create())
                .usePlugin(SyntaxHighlightPlugin.create(prism4j, prism4jTheme))
                .usePlugin(new ToggleableTaskListPlugin((toggledCheckboxPosition, newCheckedState) -> {
                    final CharSequence oldUnrenderedText = unrenderedText$.getValue();
                    if (oldUnrenderedText == null) {
                        throw new IllegalStateException("Checkbox #" + toggledCheckboxPosition + ", but unrenderedText$ value is null.");
                    }
                    final CharSequence newUnrenderedText = MarkdownUtil.setCheckboxStatus(oldUnrenderedText.toString(), toggledCheckboxPosition, newCheckedState);
                    this.setMarkdownString(newUnrenderedText);
                }));
    }

    public Markwon.Builder createMarkwonBuilder(@NonNull Context context, @NonNull Map<String, String> mentions) {
        return createMarkwonBuilder(context)
                .usePlugin(NextcloudMentionsPlugin.create(context, mentions));
    }

    @Override
    public void registerOnLinkClickCallback(@NonNull Function<String, Boolean> callback) {
        final LinkClickInterceptorPlugin plugin = this.markwon.getPlugin(LinkClickInterceptorPlugin.class);
        if (plugin == null) {
            Log.w(TAG, "Tried to register callback, but " + LinkClickInterceptorPlugin.class.getSimpleName() + " is not a registered " + MarkwonPlugin.class.getSimpleName() + ".");
        } else {
            plugin.registerOnLinkClickCallback(callback);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        final ToggleableTaskListPlugin plugin = this.markwon.getPlugin(ToggleableTaskListPlugin.class);
        if (plugin == null) {
            Log.w(TAG, "Tried to set enabled state for " + ToggleableTaskListPlugin.class.getSimpleName() + ", but " + ToggleableTaskListPlugin.class.getSimpleName() + " is not a registered " + MarkwonPlugin.class.getSimpleName() + ".");
        } else {
            plugin.setEnabled(enabled);
        }
    }

    @Override
    public void setMarkdownString(CharSequence text) {
        final CharSequence previousText = this.unrenderedText$.getValue();
        this.unrenderedText$.setValue(text);
        if (listener != null) {
            listener.accept(text);
        }
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
        this.markwon = createMarkwonBuilder(getContext(), mentions).build();
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
