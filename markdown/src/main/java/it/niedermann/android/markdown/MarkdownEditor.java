package it.niedermann.android.markdown;

import android.text.style.URLSpan;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Can be used for editors and viewers as well.
 * Viewer can support basic edit features, like toggling checkboxes
 */
public interface MarkdownEditor {

    String TAG = MarkdownEditor.class.getSimpleName();

    /**
     * The given {@link String} will be parsed and rendered
     */
    void setMarkdownString(CharSequence text);

    /**
     * Will replace all `@mention`s of Nextcloud users with the avatar and given display name.
     *
     * @param mentions {@link Map} of mentions, where the key is the user id and the value is the display name
     */
    default void setMarkdownString(CharSequence text, @NonNull Map<String, String> mentions) {
        setMarkdownString(text);
    }

    /**
     * @return the source {@link CharSequence} of the currently rendered markdown
     */
    LiveData<CharSequence> getMarkdownString();

    /**
     * Similar to {@link #getMarkdownString()} but without {@link LiveData}. Will remove previously set {@link Consumer}s.
     *
     * @param listener a {@link Consumer} which will receive the changed markdown string.
     */
    void setMarkdownStringChangedListener(@Nullable Consumer<CharSequence> listener);

    void setEnabled(boolean enabled);

    /**
     * @param color which will be used for highlighting. See {@link #setSearchText(CharSequence)}
     */
    default void setSearchColor(@ColorInt int color) {
        Log.w(TAG, "This feature is not supported by the currently used implementation.");
    }

    /**
     * See {@link #setSearchText(CharSequence, Integer)}
     */
    default void setSearchText(@Nullable CharSequence searchText) {
        setSearchText(searchText, null);
    }

    /**
     * Highlights the given {@param searchText} in the {@link MarkdownEditor}.
     *
     * @param searchText the term to highlight
     * @param current    highlights the occurrence of the {@param searchText} at this position special
     */
    default void setSearchText(@Nullable CharSequence searchText, @Nullable Integer current) {
        Log.w(TAG, "This feature is not supported by the currently used implementation.");
    }

    /**
     * Intercepts each click on a clickable element like {@link URLSpan}s
     *
     * @param callback Will be called on a click. When the {@param callback} returns <code>true</code>, the click will not be propagated further.
     */
    default void registerOnLinkClickCallback(@NonNull Function<String, Boolean> callback) {
        Log.w(TAG, "This feature is not supported by the currently used implementation.");
    }
}