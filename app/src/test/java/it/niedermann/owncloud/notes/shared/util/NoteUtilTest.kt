/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util;

import android.os.Build;

import androidx.core.text.HtmlCompat;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import it.niedermann.android.markdown.MarkdownUtil;

/**
 * Tests the NoteUtil.
 */
@RunWith(RobolectricTestRunner.class)
public class NoteUtilTest extends TestCase {

    @Test
    public void testIsEmptyLine() {
        assertTrue(NoteUtil.isEmptyLine(" "));
        assertTrue(NoteUtil.isEmptyLine("\n"));
        assertTrue(NoteUtil.isEmptyLine("\n "));
        assertTrue(NoteUtil.isEmptyLine(" \n"));
        assertTrue(NoteUtil.isEmptyLine(" \n "));
        assertFalse(NoteUtil.isEmptyLine("a \n "));
    }

    @Test
    public void testGetLineWithoutMarkdown() {
        assertEquals("Test", NoteUtil.getLineWithoutMarkdown("Test", 0));
        assertEquals("Test", NoteUtil.getLineWithoutMarkdown("\nTest", 0));
        assertEquals("Foo", NoteUtil.getLineWithoutMarkdown("Foo\nBar", 0));
        assertEquals("Bar", NoteUtil.getLineWithoutMarkdown("Foo\nBar", 1));
        assertEquals("Foo", NoteUtil.getLineWithoutMarkdown("* Foo\n* Bar", 0));
        assertEquals("Bar", NoteUtil.getLineWithoutMarkdown("- Foo\nBar", 1));
        assertEquals("Foo", NoteUtil.getLineWithoutMarkdown("# Foo", 0));
    }

    @Test
    public void testGenerateNoteTitle() {
        assertEquals("Test", NoteUtil.generateNoteTitle("Test"));
        assertEquals("Test", NoteUtil.generateNoteTitle("Test\n"));
        assertEquals("Test", NoteUtil.generateNoteTitle("Test\nFoo"));
        assertEquals("Test", NoteUtil.generateNoteTitle("\nTest"));
        assertEquals("Test", NoteUtil.generateNoteTitle("\n\nTest"));

        // https://github.com/nextcloud/notes-android/issues/1104
        assertEquals("2021-03-24 - Example title", MarkdownUtil.removeMarkdown("2021-03-24 - Example title"));
    }

    @Test
    public void testGenerateNoteExcerpt() {
        // title is different from content → return max. 200 characters starting with the first line which is not empty
        assertEquals("Test", NoteUtil.generateNoteExcerpt("Test", "Title"));
        assertEquals("Test   Foo", NoteUtil.generateNoteExcerpt("Test\nFoo", "Title"));
        assertEquals("Test   Foo   Bar", NoteUtil.generateNoteExcerpt("Test\nFoo\nBar", "Title"));
        assertEquals("", NoteUtil.generateNoteExcerpt("", "Title"));

        // content actually starts with title → return max. 200 characters starting with the first character after the title
        assertEquals("", NoteUtil.generateNoteExcerpt("Title", "Title"));
        assertEquals("Foo", NoteUtil.generateNoteExcerpt("Title\nFoo", "Title"));
        assertEquals("Title   Bar", NoteUtil.generateNoteExcerpt("Title\nTitle\nBar", "Title"));
        assertEquals("", NoteUtil.generateNoteExcerpt("", "Title"));

        // some empty lines between the actual contents → Should be ignored
        assertEquals("", NoteUtil.generateNoteExcerpt("\nTitle", "Title"));
        assertEquals("Foo", NoteUtil.generateNoteExcerpt("\n\n\n\nTitle\nFoo", "Title"));
        assertEquals("Title   Bar", NoteUtil.generateNoteExcerpt("\nTitle\n\n\nTitle\nBar", "\n\nTitle"));
        assertEquals("", NoteUtil.generateNoteExcerpt("\n\n\n", "\nTitle"));

        // content has markdown while titles markdown is already stripped
        assertEquals("", NoteUtil.generateNoteExcerpt("# Title", "Title"));
        assertEquals("Foo", NoteUtil.generateNoteExcerpt("Title\n- Foo", "Title"));

        // title has markdown while contents markdown is stripped
        assertEquals("", NoteUtil.generateNoteExcerpt("Title", "# Title"));
        assertEquals("Foo", NoteUtil.generateNoteExcerpt("Title\nFoo", "- Title"));
        assertEquals("Title   Bar", NoteUtil.generateNoteExcerpt("Title\nTitle\nBar", "- Title"));

        // content and title have markdown
        assertEquals("", NoteUtil.generateNoteExcerpt("# Title", "# Title"));
        assertEquals("Foo", NoteUtil.generateNoteExcerpt("# Title\n- Foo", "- Title"));
        assertEquals("Title   Bar", NoteUtil.generateNoteExcerpt("- Title\nTitle\nBar", "- Title"));
    }

    /**
     * Has known issues on {@link Build.VERSION_CODES#LOLLIPOP_MR1} and
     * {@link Build.VERSION_CODES#M} due to incompatibilities of
     * {@link HtmlCompat#fromHtml(String, int)}
     */
    @Test
    @Config(sdk = {30})
    public void testGenerateNoteExcerpt_sdk_30() {
        // content has markdown while titles markdown is already stripped
        assertEquals("Title   Bar", NoteUtil.generateNoteExcerpt("# Title\n- Title\n- Bar", "Title"));
    }
}
