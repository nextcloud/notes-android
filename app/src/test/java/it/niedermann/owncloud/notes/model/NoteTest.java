package it.niedermann.owncloud.notes.model;

import junit.framework.TestCase;

import it.niedermann.owncloud.notes.util.NoteUtil;

/**
 * Tests the Note Model
 * Created by stefan on 06.10.15.
 */
public class NoteTest extends TestCase {

    public void testMarkDownStrip() {
        assertEquals("Title", NoteUtil.removeMarkDown("# Title"));
        assertEquals("Aufzählung", NoteUtil.removeMarkDown("* Aufzählung"));
    }
}
