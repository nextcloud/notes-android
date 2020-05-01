package it.niedermann.owncloud.notes.branding;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.preference.PreferenceManager;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.DarkModeSetting;
import it.niedermann.owncloud.notes.util.Notes;

import static it.niedermann.owncloud.notes.util.ColorUtil.contrastRatioIsSufficient;

public class BrandingUtil {

    private static final String TAG = BrandingUtil.class.getSimpleName();
    private static final String pref_key_branding = "branding";
    private static final String pref_key_branding_main = "branding_main";
    private static final String pref_key_branding_text = "branding_text";

    private BrandingUtil() {

    }


    public static boolean isBrandingEnabled(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(pref_key_branding, true);
    }

    @ColorInt
    public static int readBrandMainColor(@NonNull Context context) {
        if (BrandingUtil.isBrandingEnabled(context)) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            Log.v(TAG, "--- Read: shared_preference_theme_main");
            return sharedPreferences.getInt(pref_key_branding_main, context.getApplicationContext().getResources().getColor(R.color.primary));
        } else {
            return context.getResources().getColor(R.color.primary);
        }
    }

    @ColorInt
    public static int readBrandTextColor(@NonNull Context context) {
        if (isBrandingEnabled(context)) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            Log.v(TAG, "--- Read: shared_preference_theme_text");
            return sharedPreferences.getInt(pref_key_branding_text, context.getApplicationContext().getResources().getColor(android.R.color.white));
        } else {
            return Color.WHITE;
        }
    }

    public static void saveBrandColors(@NonNull Context context, @ColorInt int mainColor, @ColorInt int textColor) {
        if (isBrandingEnabled(context) && context instanceof BrandedActivity) {
            final BrandedActivity activity = (BrandedActivity) context;
            activity.applyBrand(mainColor, textColor);
            BrandedActivity.applyBrandToStatusbar(activity.getWindow(), mainColor, textColor);
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        Log.v(TAG, "--- Write: shared_preference_theme_main" + " | " + mainColor);
        Log.v(TAG, "--- Write: shared_preference_theme_text" + " | " + textColor);
        editor.putInt(pref_key_branding_main, mainColor);
        editor.putInt(pref_key_branding_text, textColor);
        editor.apply();
    }

    /**
     * Since we may collide with dark theme in this area, we have to make sure that the color is visible depending on the background
     */
    @ColorInt
    public static int getSecondaryForegroundColorDependingOnTheme(@NonNull Context context, @ColorInt int mainColor) {
        final boolean isDarkTheme = Notes.getAppTheme(context) == DarkModeSetting.DARK;
        if (isDarkTheme && !contrastRatioIsSufficient(mainColor, Color.BLACK)) {
            Log.v(TAG, "Contrast ratio between brand color " + String.format("#%06X", (0xFFFFFF & mainColor)) + " and dark theme is too low. Falling back to WHITE as brand color.");
            return Color.WHITE;
        } else if (!isDarkTheme && !contrastRatioIsSufficient(mainColor, Color.WHITE)) {
            Log.v(TAG, "Contrast ratio between brand color " + String.format("#%06X", (0xFFFFFF & mainColor)) + " and light theme is too low. Falling back to BLACK as brand color.");
            return Color.BLACK;
        } else {
            return mainColor;
        }
    }

    public static void applyBrandToEditText(@ColorInt int mainColor, @ColorInt int textColor, @NonNull EditText editText) {
        @ColorInt final int finalMainColor = getSecondaryForegroundColorDependingOnTheme(editText.getContext(), mainColor);
        DrawableCompat.setTintList(editText.getBackground(), new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_active},
                        new int[]{android.R.attr.state_activated},
                        new int[]{android.R.attr.state_focused},
                        new int[]{android.R.attr.state_pressed},
                        new int[]{}
                },
                new int[]{
                        finalMainColor,
                        finalMainColor,
                        finalMainColor,
                        finalMainColor,
                        editText.getContext().getResources().getColor(R.color.fg_default)
                }
        ));
    }
}
