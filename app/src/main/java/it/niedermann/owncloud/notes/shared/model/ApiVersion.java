/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.model;

import androidx.annotation.NonNull;

import java.util.Objects;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
public class ApiVersion implements Comparable<ApiVersion> {
    private static final Pattern NUMBER_EXTRACTION_PATTERN = Pattern.compile("[0-9]+");

    public static final ApiVersion API_VERSION_0_2 = new ApiVersion("0.2", 0, 2);
    public static final ApiVersion API_VERSION_1_0 = new ApiVersion("1.0", 1, 0);
    public static final ApiVersion API_VERSION_1_2 = new ApiVersion("1.2", 1, 2);
    public static final ApiVersion API_VERSION_1_3 = new ApiVersion("1.3", 1, 3);

    public static final ApiVersion[] SUPPORTED_API_VERSIONS = new ApiVersion[]{
            API_VERSION_1_0,
            API_VERSION_0_2
    };

    private String originalVersion = "?";
    private final int major;
    private final int minor;

    public ApiVersion(String originalVersion, int major, int minor) {
        this(major, minor);
        this.originalVersion = originalVersion;
    }

    public ApiVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public static ApiVersion of(String versionString) {
        int major = 0, minor = 0;
        if (versionString != null) {
            final String[] split = versionString.split("\\.");
            if (split.length > 0) {
                major = extractNumber(split[0]);
                if (split.length > 1) {
                    minor = extractNumber(split[1]);
                }
            }
        }
        return new ApiVersion(versionString, major, minor);
    }

    private static int extractNumber(String containsNumbers) {
        final var matcher = NUMBER_EXTRACTION_PATTERN.matcher(containsNumbers);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return 0;
    }

    /**
     * @param compare another version object
     * @return -1 if the compared major version is <strong>higher</strong> than the current major version
     * 0 if the compared major version is equal to the current major version
     * 1 if the compared major version is <strong>lower</strong> than the current major version
     */
    @Override
    public int compareTo(@NonNull ApiVersion compare) {
        if (compare.getMajor() > getMajor()) {
            return -1;
        } else if (compare.getMajor() < getMajor()) {
            return 1;
        }
        return 0;
    }

    /**
     * While setting the file suffix to <code>.txt</code> or <code>.md</code> was possible starting
     * with {@link #API_VERSION_1_2}, we will only support this feature with {@link #API_VERSION_1_3}
     * because it allows us to set any value and skip client side validations.
     *
     * @see <a href="https://github.com/nextcloud/notes/blob/master/docs/api/v1.md#settings">Settings API</a>
     */
    public boolean supportsFileSuffixChange() {
        return getMajor() >= API_VERSION_1_3.getMajor() && getMinor() >= API_VERSION_1_3.getMinor();
    }

    public boolean supportsNotesPathChange() {
        return getMajor() >= API_VERSION_1_2.getMajor() && getMinor() >= API_VERSION_1_2.getMinor();
    }

    /**
     * Checks only the <strong>{@link #major}</strong> version.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiVersion that = (ApiVersion) o;
        return compareTo(that) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor);
    }

    @NonNull
    @Override
    public String toString() {
        return "Version{" +
                "originalVersion='" + originalVersion + '\'' +
                ", major=" + major +
                ", minor=" + minor +
                '}';
    }
}
