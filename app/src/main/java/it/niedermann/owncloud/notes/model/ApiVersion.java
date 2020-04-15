package it.niedermann.owncloud.notes.model;


import androidx.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
public class ApiVersion implements Comparable<ApiVersion> {
    private static final Pattern NUMBER_EXTRACTION_PATTERN = Pattern.compile("[0-9]+");

    private String originalVersion = "?";
    private int major = 0;
    private int minor = 0;
    private int patch = 0;

    public ApiVersion(String originalVersion, int major, int minor, int patch) {
        this(major, minor, patch);
        this.originalVersion = originalVersion;
    }

    public ApiVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    public boolean isGreaterOrEqualTo(ApiVersion v) {
        return compareTo(v) >= 0;
    }

    public String getOriginalVersion() {
        return originalVersion;
    }

    public static ApiVersion of(String versionString) {
        int major = 0, minor = 0, micro = 0;
        if (versionString != null) {
            String[] split = versionString.split("\\.");
            if (split.length > 0) {
                major = extractNumber(split[0]);
                if (split.length > 1) {
                    minor = extractNumber(split[1]);
                    if (split.length > 2) {
                        micro = extractNumber(split[2]);
                    }
                }
            }
        }
        return new ApiVersion(versionString, major, minor, micro);
    }

    private static int extractNumber(String containsNumbers) {
        final Matcher matcher = NUMBER_EXTRACTION_PATTERN.matcher(containsNumbers);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return 0;
    }

    /**
     * @param compare another version object
     * @return -1 if the compared version is <strong>higher</strong> than the current version
     * 0 if the compared version is equal to the current version
     * 1 if the compared version is <strong>lower</strong> than the current version
     */
    @Override
    public int compareTo(ApiVersion compare) {
        if (compare.getMajor() > getMajor()) {
            return -1;
        } else if (compare.getMajor() < getMajor()) {
            return 1;
        } else if (compare.getMinor() > getMinor()) {
            return -1;
        } else if (compare.getMinor() < getMinor()) {
            return 1;
        } else if (compare.getPatch() > getPatch()) {
            return -1;
        } else if (compare.getPatch() < getPatch()) {
            return 1;
        }
        return 0;
    }

    @NonNull
    @Override
    public String toString() {
        return "Version{" +
                "originalVersion='" + originalVersion + '\'' +
                ", major=" + major +
                ", minor=" + minor +
                ", patch=" + patch +
                '}';
    }
}
