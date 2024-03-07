/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import it.niedermann.owncloud.notes.R;

import static it.niedermann.android.markdown.MarkdownUtil.removeMarkdown;
import static it.niedermann.android.markdown.MarkdownUtil.replaceCheckboxesWithEmojis;

/**
 * Provides basic functionality for Note operations.
 */
@SuppressWarnings("WeakerAccess")
public class NoteUtil {

    public static final String EXCERPT_LINE_SEPARATOR = "   ";

    private NoteUtil() {
        throw new UnsupportedOperationException("Do not instantiate this util class.");
    }

    /**
     * Checks if a line is empty.
     * <pre>
     * " "    -> empty
     * "\n"   -> empty
     * "\n "  -> empty
     * " \n"  -> empty
     * " \n " -> empty
     * </pre>
     *
     * @param line String - a single Line which ends with \n
     * @return boolean isEmpty
     */
    public static boolean isEmptyLine(@Nullable String line) {
        return removeMarkdown(line).trim().length() == 0;
    }

    /**
     * Truncates a string to a desired maximum length.
     * Like String.substring(int,int), but throw no exception if desired length is longer than the string.
     *
     * @param str String to truncate
     * @param len Maximum length of the resulting string
     * @return truncated string
     */
    @NonNull
    private static String truncateString(@NonNull String str, @SuppressWarnings("SameParameterValue") int len) {
        return str.substring(0, Math.min(len, str.length()));
    }

    /**
     * Generates an excerpt of a content that does <em>not</em> match the given title
     *
     * @param content {@link String}
     * @param title   {@link String} In case the content starts with the title, the excerpt should be generated starting from this point
     * @return excerpt String
     */
    @NonNull
    public static String generateNoteExcerpt(@NonNull String content, @Nullable String title) {
        content = removeMarkdown(replaceCheckboxesWithEmojis(content.trim()));
        if (TextUtils.isEmpty(content)) {
            return "";
        }
        if (!TextUtils.isEmpty(title)) {
            assert title != null;
            final String trimmedTitle = removeMarkdown(replaceCheckboxesWithEmojis(title.trim()));
            if (content.startsWith(trimmedTitle)) {
                content = content.substring(trimmedTitle.length());
            }
        }
        return truncateString(content.trim(), 200).replace("\n", EXCERPT_LINE_SEPARATOR);
    }

    @NonNull
    public static String generateNonEmptyNoteTitle(@NonNull String content, Context context) {
        String title = generateNoteTitle(content);
        if (title.isEmpty()) {
            title = context.getString(R.string.action_create);
        }
        return title;
    }

    /**
     * Generates a title of a content String (reads fist linew which is not empty)
     *
     * @param content String
     * @return excerpt String
     */
    @NonNull
    public static String generateNoteTitle(@NonNull String content) {
        return getLineWithoutMarkdown(content, 0);
    }

    /**
     * Reads the requested line and strips all Markdown. If line is empty, it will go ahead to find the next not-empty line.
     *
     * @param content    String
     * @param lineNumber int
     * @return lineContent String
     */
    @NonNull
    public static String getLineWithoutMarkdown(@NonNull String content, int lineNumber) {
        String line = "";
        if (content.contains("\n")) {
            String[] lines = content.split("\n");
            int currentLine = lineNumber;
            while (currentLine < lines.length && NoteUtil.isEmptyLine(lines[currentLine])) {
                currentLine++;
            }
            if (currentLine < lines.length) {
                line = removeMarkdown(lines[currentLine]);
            }
        } else {
            line = removeMarkdown(content);
        }
        return line;
    }

    @NonNull
    public static String extendCategory(@NonNull String category) {
        return category.replace("/", " / ");
    }

    @SuppressWarnings("WeakerAccess") //PMD...
    public static float getFontSizeFromPreferences(@NonNull Context context, @NonNull SharedPreferences sp) {
        final String prefValueSmall = context.getString(R.string.pref_value_font_size_small);
        final String prefValueMedium = context.getString(R.string.pref_value_font_size_medium);
        // final String prefValueLarge = getString(R.string.pref_value_font_size_large);
        String fontSize = sp.getString(context.getString(R.string.pref_key_font_size), prefValueMedium);

        if (fontSize.equals(prefValueSmall)) {
            return context.getResources().getDimension(R.dimen.note_font_size_small);
        } else if (fontSize.equals(prefValueMedium)) {
            return context.getResources().getDimension(R.dimen.note_font_size_medium);
        } else {
            return context.getResources().getDimension(R.dimen.note_font_size_large);
        }
    }
}
