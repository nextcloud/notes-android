package it.niedermann.owncloud.notes.util;

import junit.framework.TestCase;

/**
 * Tests the NoteUtil
 * Created by stefan on 06.10.15.
 */
public class NoteUtilTest extends TestCase {
    public void testRemoveMarkDown() {
        assertTrue("Aufzählung".equals(NoteUtil.removeMarkDown("* Aufzählung")));
        assertTrue("Header".equals(NoteUtil.removeMarkDown("# Header")));
    }

    public void testIsEmptyLine() {
        assertTrue(NoteUtil.isEmptyLine(" "));
        assertTrue(NoteUtil.isEmptyLine("\n"));
        assertTrue(NoteUtil.isEmptyLine("\n "));
        assertTrue(NoteUtil.isEmptyLine(" \n"));
        assertTrue(NoteUtil.isEmptyLine(" \n "));
        assertFalse(NoteUtil.isEmptyLine("a \n "));
    }

    public void testGetLineWithoutMarkDown() {
        assertEquals("Test", NoteUtil.getLineWithoutMarkDown("Test", 0));
        assertEquals("Test", NoteUtil.getLineWithoutMarkDown("\nTest", 0));
        assertEquals("Foo", NoteUtil.getLineWithoutMarkDown("Foo\nBar", 0));
        assertEquals("Bar", NoteUtil.getLineWithoutMarkDown("Foo\nBar", 1));
    }

    public void testGenerateNoteTitle() {
        assertEquals("Test", NoteUtil.generateNoteTitle("Test"));
        assertEquals("Test", NoteUtil.generateNoteTitle("Test\n"));
        assertEquals("Test", NoteUtil.generateNoteTitle("Test\nFoo"));
        assertEquals("Test", NoteUtil.generateNoteTitle("\nTest"));
        assertEquals("Test", NoteUtil.generateNoteTitle("\n\nTest"));
    }

    public void testGenerateNoteExcerpt() {
        assertEquals("Test", NoteUtil.generateNoteExcerpt("Test"));
        assertEquals("Foo", NoteUtil.generateNoteExcerpt("Test\nFoo"));
        assertEquals("Foo", NoteUtil.generateNoteExcerpt("Test\nFoo\nBar"));
        assertEquals("", NoteUtil.generateNoteExcerpt(""));
    }
}