package it.niedermann.owncloud.notes.shared.util;

import junit.framework.TestCase;

/**
 * Tests the NoteUtil
 * Created by stefan on 06.10.15.
 */
public class NoteUtilTest extends TestCase {

    public void testIsEmptyLine() {
        assertTrue(NoteUtil.isEmptyLine(" "));
        assertTrue(NoteUtil.isEmptyLine("\n"));
        assertTrue(NoteUtil.isEmptyLine("\n "));
        assertTrue(NoteUtil.isEmptyLine(" \n"));
        assertTrue(NoteUtil.isEmptyLine(" \n "));
        assertFalse(NoteUtil.isEmptyLine("a \n "));
    }

    public void testGetLineWithoutMarkdown() {
        assertEquals("Test", NoteUtil.getLineWithoutMarkdown("Test", 0));
        assertEquals("Test", NoteUtil.getLineWithoutMarkdown("\nTest", 0));
        assertEquals("Foo", NoteUtil.getLineWithoutMarkdown("Foo\nBar", 0));
        assertEquals("Bar", NoteUtil.getLineWithoutMarkdown("Foo\nBar", 1));
    }

    public void testGenerateNoteTitle() {
        assertEquals("Test", NoteUtil.generateNoteTitle("Test"));
        assertEquals("Test", NoteUtil.generateNoteTitle("Test\n"));
        assertEquals("Test", NoteUtil.generateNoteTitle("Test\nFoo"));
        assertEquals("Test", NoteUtil.generateNoteTitle("\nTest"));
        assertEquals("Test", NoteUtil.generateNoteTitle("\n\nTest"));
    }

    //  title is different from content → return max. 200 characters starting with the first line which is not empty
    // CS304 issue link: https://github.com/stefan-niedermann/nextcloud-notes/issues/1087
    public void testGenerateNoteExcerpt1() {
        assertEquals("Word count: 4   Test", NoteUtil.generateNoteExcerpt("Test", "Title"));
        assertEquals("Word count: 8   Test   Foo", NoteUtil.generateNoteExcerpt("Test\nFoo", "Title"));
        assertEquals("Word count: 12   Test   Foo   Bar", NoteUtil.generateNoteExcerpt("Test\nFoo\nBar", "Title"));
        assertEquals("Word count: 0", NoteUtil.generateNoteExcerpt("", "Title"));
    }

    //  content actually starts with title → return max. 200 characters starting with the first character after the title
    // CS304 issue link: https://github.com/stefan-niedermann/nextcloud-notes/issues/1087
    public void testGenerateNoteExcerpt2() {
        assertEquals("Word count: 5   ", NoteUtil.generateNoteExcerpt("Title", "Title"));
        assertEquals("Word count: 9   Foo", NoteUtil.generateNoteExcerpt("Title\nFoo", "Title"));
        assertEquals("Word count: 15   Title   Bar", NoteUtil.generateNoteExcerpt("Title\nTitle\nBar", "Title"));
        assertEquals("Word count: 0", NoteUtil.generateNoteExcerpt("", "Title"));
    }
}