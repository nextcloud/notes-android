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
        // utility class -> private constructor
    }

    public static boolean areCredentialsAvailable(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

        if (keyguardManager != null) {
            return keyguardManager.isKeyguardSecure();
        } else {
            Log.e(TAG, "Keyguard manager is null");
            return false;
        }
    }
}
