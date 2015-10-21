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
    }
}
