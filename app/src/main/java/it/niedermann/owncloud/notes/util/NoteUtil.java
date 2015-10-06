package it.niedermann.owncloud.notes.util;

import android.text.Html;

import com.commonsware.cwac.anddown.AndDown;

/**
 * Provides basic functionality for Note operations.
 * Created by stefan on 06.10.15.
 */
public class NoteUtil {

    private static final AndDown and_down = new AndDown();

    /**
     * Parses a MarkDown-String and returns its HTML-Pendant
     *
     * @param s String MarkDown
     * @return String HTML
     */
    public static String parseMarkDown(String s) {
        return and_down.markdownToHtml(s);
    }

    /**
     * Strips all MarkDown from the given String
     *
     * @param s String - MarkDown
     * @return Plain Text-String
     */
    public static String removeMarkDown(String s) {
        return s == null ? "" : Html.fromHtml(and_down.markdownToHtml(s)).toString().trim();
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
