package it.niedermann.owncloud.notes;

import android.app.Activity;
import android.content.pm.PackageManager;

import com.nextcloud.android.sso.helper.VersionCheckHelper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class ExceptionUtil {

    private ExceptionUtil() {

    }

    public static String getDebugInfos(Activity activity, Throwable throwable) {
        List<Throwable> throwables = new ArrayList<>();
        throwables.add(throwable);
        return getDebugInfos(activity, throwables);
    }

    public static String getDebugInfos(Activity activity, List<Throwable> throwables) {
        StringBuilder debugInfos = new StringBuilder(""
                + getAppVersions(activity)
                + "\n\n---\n"
                + getDeviceInfos()
                + "\n\n---"
                + "\n\n");
        for (Throwable throwable : throwables) {
            debugInfos.append(getStacktraceOf(throwable));
        }
        return debugInfos.toString();
    }

    private static String getAppVersions(Activity activity) {
        String versions = ""
                + "App Version: " + BuildConfig.VERSION_NAME + "\n"
                + "App Version Code: " + BuildConfig.VERSION_CODE + "\n"
                + "App Flavor: " + BuildConfig.FLAVOR + "\n";

        try {
            versions += "\nFiles App Version Code: " + VersionCheckHelper.getNextcloudFilesVersionCode(activity);
        } catch (PackageManager.NameNotFoundException e) {
            versions += "\nFiles App Version Code: " + e.getMessage();
            e.printStackTrace();
        }
        return versions;
    }

    private static String getDeviceInfos() {
        return ""
                + "\nOS Version: " + System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")"
                + "\nOS API Level: " + android.os.Build.VERSION.SDK_INT
                + "\nDevice: " + android.os.Build.DEVICE
                + "\nModel (and Product): " + android.os.Build.MODEL + " (" + android.os.Build.PRODUCT + ")";
    }

    private static String getStacktraceOf(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
