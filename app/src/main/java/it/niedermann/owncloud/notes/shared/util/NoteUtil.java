package it.niedermann.owncloud.notes.shared.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.regex.Pattern;

import it.niedermann.owncloud.notes.R;

/**
 * Provides basic functionality for Note operations.
 * Created by stefan on 06.10.15.
 */
@SuppressWarnings("WeakerAccess")
public class NoteUtil {

    private static final Pattern pLists = Pattern.compile("^\\s*[*+-]\\s+", Pattern.MULTILINE);
    private static final Pattern pHeadings = Pattern.compile("^#+\\s+(.*?)\\s*#*$", Pattern.MULTILINE);
    private static final Pattern pHeadingLine = Pattern.compile("^(?:=*|-*)$", Pattern.MULTILINE);
    private static final Pattern pEmphasis = Pattern.compile("(\\*+|_+)(.*?)\\1", Pattern.MULTILINE);
    private static final Pattern pSpace1 = Pattern.compile("^\\s+", Pattern.MULTILINE);
    private static final Pattern pSpace2 = Pattern.compile("\\s+$", Pattern.MULTILINE);

    public static final String EXCERPT_LINE_SEPARATOR = "   ";

    private NoteUtil() {

    }

    /**
     * Strips all MarkDown from the given String
     *
     * @param s String - MarkDown
     * @return Plain Text-String
     */
    @NonNull
    public static String removeMarkDown(@Nullable String s) {
        if (s == null)
            return "";
        String result = s;
        result = pLists.matcher(result).replaceAll("");
        result = pHeadings.matcher(result).replaceAll("$1");
        result = pHeadingLine.matcher(result).replaceAll("");
        result = pEmphasis.matcher(result).replaceAll("$2");
        result = pSpace1.matcher(result).replaceAll("");
        result = pSpace2.matcher(result).replaceAll("");
        return result;
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
        return removeMarkDown(line).trim().length() == 0;
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
        content = removeMarkDown(content.trim());
        if(TextUtils.isEmpty(content)) {
            return "";
        }
        if (!TextUtils.isEmpty(title)) {
            final String trimmedTitle = removeMarkDown(title.trim());
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
        return getLineWithoutMarkDown(content, 0);
    }

    /**
     * Reads the requested line and strips all MarkDown. If line is empty, it will go ahead to find the next not-empty line.
     *
     * @param content    String
     * @param lineNumber int
     * @return lineContent String
     */
    @NonNull
    public static String getLineWithoutMarkDown(@NonNull String content, int lineNumber) {
        String line = "";
        if (content.contains("\n")) {
            String[] lines = content.split("\n");
            int currentLine = lineNumber;
            while (currentLine < lines.length && NoteUtil.isEmptyLine(lines[currentLine])) {
                currentLine++;
            }
            if (currentLine < lines.length) {
                line = NoteUtil.removeMarkDown(lines[currentLine]);
            }
        } else {
            line = content;
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
