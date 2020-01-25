package it.niedermann.owncloud.notes.util;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

/**
 * Utility class with methods for handling device credentials.
 */
@RequiresApi(Build.VERSION_CODES.M)
class DeviceCredentialUtils {

    private static final String TAG = DeviceCredentialUtils.class.getCanonicalName();

    private DeviceCredentialUtils() {
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
