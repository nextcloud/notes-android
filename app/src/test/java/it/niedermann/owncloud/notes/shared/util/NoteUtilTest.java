package it.niedermann.owncloud.notes.shared.util;

import junit.framework.TestCase;

/**
 * Tests the NoteUtil
 * Created by stefan on 06.10.15.
 */
public class NoteUtilTest extends TestCase {
    public void testRemoveMarkDown() {
        assertEquals("Test", NoteUtil.removeMarkDown("Test"));
        assertEquals("Foo\nBar", NoteUtil.removeMarkDown("Foo\nBar"));
        assertEquals("Foo\nBar", NoteUtil.removeMarkDown("Foo\n  Bar"));
        assertEquals("Foo\nBar", NoteUtil.removeMarkDown("Foo   \nBar"));
        assertEquals("Foo-Bar", NoteUtil.removeMarkDown("Foo-Bar"));
        assertEquals("Foo*Bar", NoteUtil.removeMarkDown("Foo*Bar"));
        assertEquals("Foo/Bar", NoteUtil.removeMarkDown("Foo/Bar"));
        assertEquals("FooTestBar", NoteUtil.removeMarkDown("Foo*Test*Bar"));
        assertEquals("FooTestBar", NoteUtil.removeMarkDown("Foo**Test**Bar"));
        assertEquals("FooTestBar", NoteUtil.removeMarkDown("Foo***Test***Bar"));
        assertEquals("FooTest*Bar", NoteUtil.removeMarkDown("Foo*Test**Bar"));
        assertEquals("Foo*TestBar", NoteUtil.removeMarkDown("Foo***Test**Bar"));
        assertEquals("FooTestBar", NoteUtil.removeMarkDown("Foo_Test_Bar"));
        assertEquals("FooTestBar", NoteUtil.removeMarkDown("Foo__Test__Bar"));
        assertEquals("FooTestBar", NoteUtil.removeMarkDown("Foo___Test___Bar"));
        assertEquals("Foo\nHeader\nBar", NoteUtil.removeMarkDown("Foo\n# Header\nBar"));
        assertEquals("Foo\nHeader\nBar", NoteUtil.removeMarkDown("Foo\n### Header\nBar"));
        assertEquals("Foo\nHeader\nBar", NoteUtil.removeMarkDown("Foo\n# Header #\nBar"));
        assertEquals("Foo\nHeader\nBar", NoteUtil.removeMarkDown("Foo\n## Header ####\nBar"));
        assertEquals("Foo\nNo Header #\nBar", NoteUtil.removeMarkDown("Foo\nNo Header #\nBar"));
        assertEquals("Foo\nHeader\nBar", NoteUtil.removeMarkDown("Foo\nHeader\n=\nBar"));
        assertEquals("Foo\nHeader\nBar", NoteUtil.removeMarkDown("Foo\nHeader\n-----\nBar"));
        assertEquals("Foo\nHeader\n--=--\nBar", NoteUtil.removeMarkDown("Foo\nHeader\n--=--\nBar"));
        assertEquals("Foo\nAufzählung\nBar", NoteUtil.removeMarkDown("Foo\n* Aufzählung\nBar"));
        assertEquals("Foo\nAufzählung\nBar", NoteUtil.removeMarkDown("Foo\n+ Aufzählung\nBar"));
        assertEquals("Foo\nAufzählung\nBar", NoteUtil.removeMarkDown("Foo\n- Aufzählung\nBar"));
        assertEquals("Foo\nAufzählung\nBar", NoteUtil.removeMarkDown("Foo\n    - Aufzählung\nBar"));
        assertEquals("Foo\nAufzählung *\nBar", NoteUtil.removeMarkDown("Foo\n* Aufzählung *\nBar"));
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