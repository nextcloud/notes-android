
/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

import it.niedermann.owncloud.notes.shared.model.ApiVersion;

public class ApiVersionUtil {

    private ApiVersionUtil() {
        throw new UnsupportedOperationException("Do not instantiate this util class.");
    }

    /**
     * @return a {@link Collection} of all valid {@link ApiVersion}s which have been found in {@param raw}.
     */
    @NonNull
    public static Collection<ApiVersion> parse(@Nullable String raw) {
        if (TextUtils.isEmpty(raw)) {
            return Collections.emptyList();
        }

        JSONArray a;
        try {
            a = new JSONArray(raw);
        } catch (JSONException e) {
            try {
                a = new JSONArray("[" + raw + "]");
            } catch (JSONException e1) {
                return Collections.emptyList();
            }
        }

        final var result = new ArrayList<ApiVersion>();
        for (int i = 0; i < a.length(); i++) {
            try {
                final var version = ApiVersion.of(a.getString(i));
                if (version.getMajor() != 0 || version.getMinor() != 0) {
                    result.add(version);
                }
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    /**
     * @return a serialized {@link String} of the given {@param apiVersions} or <code>null</code>.
     */
    @Nullable
    public static String serialize(@Nullable Collection<ApiVersion> apiVersions) {
        if (apiVersions == null || apiVersions.isEmpty()) {
            return null;
        }
        return "[" +
                apiVersions
                        .stream()
                        .filter(Objects::nonNull)
                        .map(v -> v.getMajor() + "." + v.getMinor())
                        .collect(Collectors.joining(","))
                + "]";
    }

    @Nullable
    public static String sanitize(@Nullable String raw) {
        return serialize(parse(raw));
    }

    /**
     * @return the highest {@link ApiVersion} that is supported by the server according to {@param raw},
     * whose major version is also supported by this app (see {@link ApiVersion#SUPPORTED_API_VERSIONS}).
     * Returns <code>null</code> if no better version could be found.
     */
    @Nullable
    public static ApiVersion getPreferredApiVersion(@Nullable String raw) {
        return parse(raw)
                .stream()
                .filter(version -> Arrays.asList(ApiVersion.SUPPORTED_API_VERSIONS).contains(version))
                .max((o1, o2) -> {
                    if (o2.getMajor() > o1.getMajor()) {
                        return -1;
                    } else if (o2.getMajor() < o1.getMajor()) {
                        return 1;
                    } else if (o2.getMinor() > o1.getMinor()) {
                        return -1;
                    } else if (o2.getMinor() < o1.getMinor()) {
                        return 1;
                    }
                    return 0;
                })
                .orElse(null);
    }
}
