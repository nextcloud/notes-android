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
        assertEquals("Title   Bar", NoteUtil.generateNoteExcerpt("# Title\n- Title\n- Bar", "Title"));

        // title has markdown while contents markdown is stripped
        assertEquals("", NoteUtil.generateNoteExcerpt("Title", "# Title"));
        assertEquals("Foo", NoteUtil.generateNoteExcerpt("Title\nFoo", "- Title"));
        assertEquals("Title   Bar", NoteUtil.generateNoteExcerpt("Title\nTitle\nBar", "- Title"));

        // content and title have markdown
        assertEquals("", NoteUtil.generateNoteExcerpt("# Title", "# Title"));
        assertEquals("Foo", NoteUtil.generateNoteExcerpt("# Title\n- Foo", "- Title"));
        assertEquals("Title   Bar", NoteUtil.generateNoteExcerpt("- Title\nTitle\nBar", "- Title"));
    }
}