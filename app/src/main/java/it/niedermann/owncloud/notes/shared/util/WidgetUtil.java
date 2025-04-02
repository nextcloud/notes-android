/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util;

import android.app.PendingIntent;
import android.os.Build;

public class WidgetUtil {

    private WidgetUtil() {
        throw new UnsupportedOperationException("This class must not get instantiated");
    }

    /**
     * Android 14 (API 34) and above require FLAG_IMMUTABLE
     *
     * <p>
     * Android 12 (API 31) and above allow FLAG_MUTABLE
     *
     * <p>
     * Ensures compatibility with different Android API levels by setting appropriate flags for a PendingIntent.
     *
     * @param flags The original flags to be used for the PendingIntent.
     * @return The modified flags with compatibility adjustments based on the Android API level.
     */
    public static int pendingIntentFlagCompat(int flags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return flags | PendingIntent.FLAG_IMMUTABLE;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return flags | PendingIntent.FLAG_MUTABLE;
        }
        return flags;
    }
}
