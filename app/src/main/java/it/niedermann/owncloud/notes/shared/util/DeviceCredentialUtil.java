/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util;

import android.app.KeyguardManager;
import android.content.Context;
import android.util.Log;

/**
 * Utility class with methods for handling device credentials.
 */
public class DeviceCredentialUtil {

    private static final String TAG = DeviceCredentialUtil.class.getSimpleName();

    private DeviceCredentialUtil() {
        throw new UnsupportedOperationException("Do not instantiate this util class.");
    }

    public static boolean areCredentialsAvailable(Context context) {
        final var keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

        if (keyguardManager != null) {
            return keyguardManager.isKeyguardSecure();
        } else {
            Log.e(TAG, "Keyguard manager is null");
            return false;
        }
    }
}
