/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.regex.Pattern;

/**
 * Builds authenticated WebDAV URLs for Nextcloud Notes attachment images.
 * Markdown stores attachment images as {@code ![alt](.attachments.XXX/file.ext)};
 * the relative {@code .attachments.XXX/...} path is not directly loadable, so it
 * is rewritten to a server WebDAV path that SSO-Glide can authenticate against.
 */
public final class AttachmentUrlUtil {

    /**
     * Matches a markdown image whose destination is a Nextcloud attachment path.
     * Group 1: the {@code ![alt](} prefix. Group 2: the {@code .attachments.XXX/file}
     * path. Group 3: the closing {@code )}.
     */
    public static final Pattern ATTACHMENT_PATTERN =
            Pattern.compile("(!\\[[^\\]]*\\]\\()(\\.attachments\\.[^)]+)(\\))");

    private AttachmentUrlUtil() {
    }

    /**
     * Rewrites a {@code .attachments.XXX/file} path into a server-relative WebDAV
     * URL. Inputs that are not attachment paths are returned unchanged. No URL
     * encoding is applied, matching the behaviour of the existing preview-mode
     * transform.
     *
     * @param attachmentPath the raw markdown destination, e.g. {@code .attachments.1/a.jpg}
     * @param username       the Nextcloud account username
     * @param notesRoot      the Notes app root folder, typically {@code "Notes"}
     * @param category       the note's category (sub-folder), or null/empty for root
     * @return a server-relative WebDAV URL beginning with {@code /remote.php/dav/...}
     */
    @NonNull
    public static String transformAttachmentPath(@NonNull String attachmentPath,
                                                 @NonNull String username,
                                                 @NonNull String notesRoot,
                                                 @Nullable String category) {
        if (!attachmentPath.startsWith(".attachments.")) {
            return attachmentPath;
        }
        final String fullPath;
        if (category != null && !category.isEmpty()) {
            fullPath = notesRoot + "/" + category;
        } else {
            fullPath = notesRoot;
        }
        return "/remote.php/dav/files/" + username + "/" + fullPath + "/" + attachmentPath;
    }
}
