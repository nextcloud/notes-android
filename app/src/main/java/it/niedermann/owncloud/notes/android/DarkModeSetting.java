package it.niedermann.owncloud.notes.android;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.NoSuchElementException;

/**
 * Possible values of the Dark Mode Setting.
 * <p>
 * The Dark Mode Setting can be stored in {@link android.content.SharedPreferences} as String by using {@link DarkModeSetting#name()} and received via {@link DarkModeSetting#valueOf(String)}.
 * <p>
 * Additionally, the equivalent {@link AppCompatDelegate}-Mode can be received via {@link #getModeId()}. To convert a {@link AppCompatDelegate}-Mode to a {@link DarkModeSetting}, use {@link #fromModeID(int)}
 *
 * @see AppCompatDelegate#MODE_NIGHT_YES
 * @see AppCompatDelegate#MODE_NIGHT_NO
 * @see AppCompatDelegate#MODE_NIGHT_FOLLOW_SYSTEM
 */
public enum DarkModeSetting {
    // WARNING - The names of the constants must *NOT* be changed since they are used as keys in SharedPreferences

    /**
     * Always use light mode.
     */
    LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
    /**
     * Always use dark mode.
     */
    DARK(AppCompatDelegate.MODE_NIGHT_YES),
    /**
     * Follow the global system setting for dark mode.
     */
    SYSTEM_DEFAULT(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

    private final int modeId;

    DarkModeSetting(int modeId) {
        this.modeId = modeId;
    }

    public int getModeId() {
        return modeId;
    }

    /**
     * Returns the instance of {@link DarkModeSetting} that corresponds to the ModeID of {@link AppCompatDelegate}
     * <p>
     * Possible ModeIDs are:
     * <ul>
     *     <li>{@link AppCompatDelegate#MODE_NIGHT_YES}</li>
     *     <li>{@link AppCompatDelegate#MODE_NIGHT_NO}</li>
     *     <li>{@link AppCompatDelegate#MODE_NIGHT_FOLLOW_SYSTEM}</li>
     * </ul>
     *
     * @param id One of the {@link AppCompatDelegate}-Night-Modes
     * @return An instance of {@link DarkModeSetting}
     */
    public static DarkModeSetting fromModeID(int id) {
        for (DarkModeSetting value : DarkModeSetting.values()) {
            if (value.modeId == id) {
                return value;
            }
        }

        throw new NoSuchElementException("No NightMode with ID " + id + " found");
    }
}
