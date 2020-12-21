package it.niedermann.android.markdown;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import java.util.Map;

/**
 * Can be used for editors and viewers as well.
 * Viewer can support basic edit features, like toggling checkboxes
 */
public interface MarkdownEditor {

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
     * @return the source {@link String} of the currently rendered markdown
     */
    LiveData<CharSequence> getMarkdownString();

    void setEnabled(boolean enabled);

    default void setSearchColor(@ColorInt int color) {
        // Optional
    }

    default void setSearchText(@Nullable CharSequence searchText) {
        setSearchText(searchText, null);
    }

    default void setSearchText(@Nullable CharSequence searchText, @Nullable Integer current) {
        // Optional
    }
}