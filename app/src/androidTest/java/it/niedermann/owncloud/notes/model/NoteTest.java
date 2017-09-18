package it.niedermann.owncloud.notes.model;

import junit.framework.TestCase;

import java.util.Calendar;

/**
 * Tests the Note Model
 * Created by stefan on 06.10.15.
 */
public class NoteTest extends TestCase {

    public void testMarkDownStrip() {
        CloudNote note = new CloudNote(0, Calendar.getInstance(), "#Title", "", false, null, null);
        assertTrue("Title".equals(note.getTitle()));
        note.setTitle("* Aufzählung");
        assertTrue("Aufzählung".equals(note.getTitle()));
    }
}
