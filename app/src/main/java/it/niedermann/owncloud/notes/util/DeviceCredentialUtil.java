package it.niedermann.owncloud.notes.util;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

/**
 * Utility class with methods for handling device credentials.
 */
public class DeviceCredentialUtil {

    private static final String TAG = DeviceCredentialUtil.class.getCanonicalName();

    private DeviceCredentialUtil() {
        // utility class -> private constructor
    }

    public static boolean areCredentialsAvailable(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

        if (keyguardManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                return keyguardManager.isKeyguardSecure();
            } else {
                Log.i(TAG, "No credentials are available on Android " + Build.VERSION.CODENAME);
                return false;
            }
        } else {
            Log.e(TAG, "Keyguard manager is null");
            return false;
        }
    }
}
