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
     * @param s String - MarkDown
     * @return Spannable
     */
    public static String parseMarkDown(String s) {
        return bypass.markdownToSpannable(s).toString();
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
     * " " -> empty
     * "\n" -> empty
     * "\n " -> empty
     * " \n" -> empty
     * " \n " -> empty
     *
     * @param line String - a single Line which ends with \n
     * @return boolean isEmpty
     */
    public static boolean isEmptyLine(String line) {
        return removeMarkDown(line).trim().length() == 0;
    }
}
