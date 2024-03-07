/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.MalformedURLException;
import java.net.URL;

import it.niedermann.android.markdown.MarkdownUtil;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class ShareUtil {

    private ShareUtil() {
        throw new UnsupportedOperationException("Do not instantiate this util class.");
    }

    public static void openShareDialog(@NonNull Context context, @Nullable String subject, @Nullable String text) {
        context.startActivity(Intent.createChooser(new Intent()
                .setAction(Intent.ACTION_SEND)
                .setType(MIMETYPE_TEXT_PLAIN)
                .putExtra(Intent.EXTRA_SUBJECT, subject)
                .putExtra(Intent.EXTRA_TITLE, subject)
                .putExtra(Intent.EXTRA_TEXT, text), subject));
    }

    public static String extractSharedText(@NonNull Intent intent) {
        final String text = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (intent.hasExtra(Intent.EXTRA_SUBJECT)) {
            final String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
            try {
                new URL(text);
                if (text != null && subject != null && !subject.trim().isEmpty()) {
                    return MarkdownUtil.getMarkdownLink(subject, text);
                } else {
                    return text;
                }
            } catch (MalformedURLException e) {
                if (subject != null && !subject.trim().isEmpty()) {
                    return subject + ": " + text;
                } else {
                    return text;
                }
            }
        } else {
            return text;
        }
    }
}
