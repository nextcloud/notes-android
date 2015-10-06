package it.niedermann.owncloud.notes.util;

import junit.framework.TestCase;

/**
 * Tests the NotesClientUtil
 * Created by stefan on 24.09.15.
 */
public class NotesClientUtilTest extends TestCase {
    public void testIsHttp() {
        assertTrue(NotesClientUtil.isHttp("http://example.com"));
        assertTrue(NotesClientUtil.isHttp("http://www.example.com/"));
        assertFalse(NotesClientUtil.isHttp("https://www.example.com/"));
        assertFalse(NotesClientUtil.isHttp(null));
    }

    public void testIsValidURLTest() {
        assertTrue(NotesClientUtil.isValidURL("http://www.example.com/"));
        assertTrue(NotesClientUtil.isValidURL("https://www.example.com"));
        assertFalse(NotesClientUtil.isValidURL("htp://www.example.com/"));
        assertFalse(NotesClientUtil.isValidURL(null));
    }
}
