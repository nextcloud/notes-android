package it.niedermann.owncloud.notes.util;

import in.uncod.android.bypass.Bypass;

/**
 * Provides basic functionality for Note operations.
 * Created by stefan on 06.10.15.
 */
public class NoteUtil {
    private static final Bypass bypass = new Bypass();


    /**
     * Parses a MarkDown-String and returns a Spannable
     *
     * @param s String - MarkDown
     * @return Spannable
     */
    public static CharSequence parseMarkDown(String s) {
        /*
         * Appends two spaces at the end of every line to force a line break.
         *
         * @see #24
         */
        StringBuilder sb = new StringBuilder();
        for (String line : s.split("\n")) {
            sb.append(line);
            // If line is not a list item
            if (!line.trim().matches("^([\\-*]|[0-9]+\\.)(.)*")) {
                sb.append("  ");
            }
            sb.append("\n");
        }
        return bypass.markdownToSpannable(sb.toString());
    }

    /**
     * Strips all MarkDown from the given String
     *
     * @param s String - MarkDown
     * @return Plain Text-String
     */
    public static String removeMarkDown(String s) {
        return s == null ? "" : s.replaceAll("[#*-]", "").trim();
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
    public static boolean isEmptyLine(String line) {
        return removeMarkDown(line).trim().length() == 0;
    }

    /**
     * Generates an excerpt of a content String (reads second line which is not empty)
     *
     * @param content String
     * @return excerpt String
     */
    public static String generateNoteExcerpt(String content) {
        return getLineWithoutMarkDown(content, 1);
    }

    /**
     * Generates a title of a content String (reads fist linew which is not empty)
     *
     * @param content String
     * @return excerpt String
     */
    public static String generateNoteTitle(String content) {
        return getLineWithoutMarkDown(content, 0);
    }

    /**
     * Reads the requested line and strips all MarkDown. If line is empty, it will go ahead to find the next not-empty line.
     *
     * @param content    String
     * @param lineNumber int
     * @return lineContent String
     */
    public static String getLineWithoutMarkDown(String content, int lineNumber) {
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
}
