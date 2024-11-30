/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.branding;

import android.content.Context;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.preference.PreferenceManager;

import com.nextcloud.android.common.ui.theme.MaterialSchemes;
import com.nextcloud.android.common.ui.theme.ViewThemeUtilsBase;
import com.nextcloud.android.common.ui.theme.utils.AndroidViewThemeUtils;
import com.nextcloud.android.common.ui.theme.utils.AndroidXViewThemeUtils;
import com.nextcloud.android.common.ui.theme.utils.DialogViewThemeUtils;
import com.nextcloud.android.common.ui.theme.utils.MaterialViewThemeUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import it.niedermann.android.sharedpreferences.SharedPreferenceIntLiveData;
import it.niedermann.owncloud.notes.R;

public class BrandingUtil extends ViewThemeUtilsBase {

    private static final String TAG = BrandingUtil.class.getSimpleName();
    private static final ConcurrentMap<Integer, BrandingUtil> CACHE = new ConcurrentHashMap<>();
    private static final String pref_key_branding_main = "branding_main";

    public final AndroidViewThemeUtils platform;
    public final MaterialViewThemeUtils material;
    public final AndroidXViewThemeUtils androidx;
    public final DialogViewThemeUtils dialog;
    public final NotesViewThemeUtils notes;

    private BrandingUtil(
            final MaterialSchemes schemes,
            final com.nextcloud.android.common.ui.color.ColorUtil colorUtil
    ) {
        super(schemes);

        this.platform = new AndroidViewThemeUtils(schemes, colorUtil);
        this.material = new MaterialViewThemeUtils(schemes, colorUtil);
        this.androidx = new AndroidXViewThemeUtils(schemes, this.platform);
        this.dialog = new DialogViewThemeUtils(schemes);
        this.notes = new NotesViewThemeUtils(schemes);
    }

    public static BrandingUtil getInstance(@NonNull Context context) {
        int color = BrandingUtil.readBrandMainColor(context);
        return new BrandingUtil(
                MaterialSchemes.Companion.fromColor(color),
                new com.nextcloud.android.common.ui.color.ColorUtil(context)
        );
    }

    public static BrandingUtil of(@ColorInt int color, @NonNull Context context) {
        return CACHE.computeIfAbsent(color, c -> new BrandingUtil(
                MaterialSchemes.Companion.fromColor(c),
                new com.nextcloud.android.common.ui.color.ColorUtil(context)
        ));
    }

    public static LiveData<Integer> readBrandMainColorLiveData(@NonNull Context context) {
        final var sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        Log.v(TAG, "--- Read: shared_preference_theme_main");
        return new SharedPreferenceIntLiveData(sharedPreferences, pref_key_branding_main, ContextCompat.getColor(context, R.color.defaultBrand));
    }

    @ColorInt
    public static int readBrandMainColor(@NonNull Context context) {
        final var sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        Log.v(TAG, "--- Read: shared_preference_theme_main");
        return sharedPreferences.getInt(pref_key_branding_main, ContextCompat.getColor(context, R.color.defaultBrand));
    }

    public static void saveBrandColor(@NonNull Context context, @ColorInt int color) {
        final int previousMainColor = readBrandMainColor(context);
        final var editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        Log.v(TAG, "--- Write: shared_preference_theme_main" + " | " + color);
        editor.putInt(pref_key_branding_main, color);
        editor.apply();
        if (context instanceof BrandedActivity) {
            if (color != previousMainColor) {
                final var activity = (BrandedActivity) context;
                activity.runOnUiThread(() -> ActivityCompat.recreate(activity));
            }
        }
    }
}
