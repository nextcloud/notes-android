package it.niedermann.owncloud.notes;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;

import com.nextcloud.android.sso.helper.VersionCheckHelper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class ExceptionUtil {

    private ExceptionUtil() {

    }

    public static String getDebugInfos(Context context, Throwable throwable) {
        List<Throwable> throwables = new ArrayList<>();
        throwables.add(throwable);
        return getDebugInfos(context, throwables);
    }

    public static String getDebugInfos(@NonNull Context context, List<Throwable> throwables) {
        StringBuilder debugInfos = new StringBuilder()
                .append(getAppVersions(context))
                .append("\n\n---\n")
                .append(getDeviceInfos())
                .append("\n\n---");
        for (Throwable throwable : throwables) {
            debugInfos.append("\n\n").append(getStacktraceOf(throwable));
        }
        return debugInfos.toString();
    }

    private static String getAppVersions(Context context) {
        String versions = ""
                + "App Version: " + BuildConfig.VERSION_NAME + "\n"
                + "App Version Code: " + BuildConfig.VERSION_CODE + "\n"
                + "App Flavor: " + BuildConfig.FLAVOR + "\n";

        try {
            versions += "\nFiles App Version Code: " + VersionCheckHelper.getNextcloudFilesVersionCode(context);
        } catch (PackageManager.NameNotFoundException e) {
            versions += "\nFiles App Version Code: " + e.getMessage();
            e.printStackTrace();
        }
        return versions;
    }

    private static String getDeviceInfos() {
        return ""
                + "\nOS Version: " + System.getProperty("os.version") + "(" + Build.VERSION.INCREMENTAL + ")"
                + "\nOS API Level: " + Build.VERSION.SDK_INT
                + "\nDevice: " + Build.DEVICE
                + "\nManufacturer: " + Build.MANUFACTURER
                + "\nModel (and Product): " + Build.MODEL + " (" + Build.PRODUCT + ")";
    }

    private static String getStacktraceOf(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
