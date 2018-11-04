package it.niedermann.owncloud.notes.util;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.regex.Pattern;

import it.niedermann.owncloud.notes.R;

/**
 * Provides basic functionality for Note operations.
 * Created by stefan on 06.10.15.
 */
public class NoteUtil {
    private static final Pattern pLists = Pattern.compile("^\\s*[*+-]\\s+", Pattern.MULTILINE);
    private static final Pattern pHeadings = Pattern.compile("^#+\\s+(.*?)\\s*#*$", Pattern.MULTILINE);
    private static final Pattern pHeadingLine = Pattern.compile("^(?:=*|-*)$", Pattern.MULTILINE);
    private static final Pattern pEmphasis = Pattern.compile("(\\*+|_+)(.*?)\\1", Pattern.MULTILINE);
    private static final Pattern pSpace1 = Pattern.compile("^\\s+", Pattern.MULTILINE);
    private static final Pattern pSpace2 = Pattern.compile("\\s+$", Pattern.MULTILINE);


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
        s = pLists.matcher(s).replaceAll("");
        s = pHeadings.matcher(s).replaceAll("$1");
        s = pHeadingLine.matcher(s).replaceAll("");
        s = pEmphasis.matcher(s).replaceAll("$2");
        s = pSpace1.matcher(s).replaceAll("");
        s = pSpace2.matcher(s).replaceAll("");
        return s;
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
    private static boolean isEmptyLine(@Nullable String line) {
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
    private static String truncateString(@NonNull String str, int len) {
        return str.substring(0, Math.min(len, str.length()));
    }

    /**
     * Generates an excerpt of a content String (reads second line which is not empty)
     *
     * @param content String
     * @return excerpt String
     */
    @NonNull
    public static String generateNoteExcerpt(@NonNull String content) {
        if (content.contains("\n"))
            return truncateString(removeMarkDown(content.replaceFirst("^.*\n", "")), 200).replace("\n", "   ");
        else
            return "";
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
    static String generateNoteTitle(@NonNull String content) {
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
    private static String getLineWithoutMarkDown(@NonNull String content, int lineNumber) {
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
}
