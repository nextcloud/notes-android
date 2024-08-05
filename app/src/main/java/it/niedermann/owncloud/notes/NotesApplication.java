/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2016-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2016-2021 Stefan Niedermann <info@niedermann.it>
 * SPDX-FileCopyrightText: 2021 Konrad Pozniak <k.pozniak@gmx.at>
 * SPDX-FileCopyrightText: 2020 Christoph Loy <loy.christoph@gmail.com>
 * SPDX-FileCopyrightText: 2017 Daniel Bailey <dan0xii@users.noreply.github.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatDelegate;

import com.nextcloud.android.sso.FilesAppTypeRegistry;
import com.nextcloud.android.sso.model.FilesAppType;

import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.preferences.DarkModeSetting;

public class NotesApplication extends Application {
    private static final String TAG = NotesApplication.class.getSimpleName();

    private static final long LOCK_TIME = 30_000;
    private static boolean lockedPreference = false;
    private static boolean isLocked = true;
    private static long lastInteraction = 0;
    private static String PREF_KEY_THEME;
    private static boolean isGridViewEnabled = false;
    private static BrandingUtil brandingUtil;

    @Override
    public void onCreate() {
        PREF_KEY_THEME = getString(R.string.pref_key_theme);
        setAppTheme(getAppTheme(getApplicationContext()));
        final var prefs = getDefaultSharedPreferences(getApplicationContext());
        lockedPreference = prefs.getBoolean(getString(R.string.pref_key_lock), false);
        isGridViewEnabled = getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_key_gridview), false);
        super.onCreate();
        brandingUtil = BrandingUtil.getInstance(this);
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        registerFilesAppType();
    }

    private void registerFilesAppType() {
        String packageId = getResources().getString(R.string.package_id);
        String accountType = getResources().getString(R.string.account_type);

        if (TextUtils.isEmpty(packageId) || TextUtils.isEmpty(accountType)) {
            return;
        }

        FilesAppTypeRegistry.getInstance().init(new FilesAppType(packageId, accountType, FilesAppType.Type.PROD));
    }

    public static BrandingUtil brandingUtil() {
        return brandingUtil;
    }

    public static void setAppTheme(DarkModeSetting setting) {
        AppCompatDelegate.setDefaultNightMode(setting.getModeId());
    }

    public static boolean isGridViewEnabled() {
        return isGridViewEnabled;
    }

    public static void updateGridViewEnabled(boolean gridView) {
        isGridViewEnabled = gridView;
    }

    public static DarkModeSetting getAppTheme(Context context) {
        final var prefs = getDefaultSharedPreferences(context);
        String mode;
        try {
            mode = prefs.getString(PREF_KEY_THEME, DarkModeSetting.SYSTEM_DEFAULT.name());
        } catch (ClassCastException e) {
            final boolean darkModeEnabled = prefs.getBoolean(PREF_KEY_THEME, false);
            mode = darkModeEnabled ? DarkModeSetting.DARK.name() : DarkModeSetting.LIGHT.name();
        }
        return DarkModeSetting.valueOf(mode);
    }

    public static void setLockedPreference(boolean lockedPreference) {
        Log.i(TAG, "New locked preference: " + lockedPreference);
        NotesApplication.lockedPreference = lockedPreference;
    }

    public static boolean isLocked() {
        if (!isLocked && System.currentTimeMillis() > (LOCK_TIME + lastInteraction)) {
            isLocked = true;
        }
        return lockedPreference && isLocked;
    }

    public static void unlock() {
        isLocked = false;
    }

    public static void updateLastInteraction() {
        lastInteraction = System.currentTimeMillis();
    }
}
