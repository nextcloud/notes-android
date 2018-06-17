package it.niedermann.owncloud.notes.util;

import junit.framework.TestCase;

/**
 * Tests the NotesClientUtil
 * Created by stefan on 24.09.15.
 */
public class NotesClientUtilTest extends TestCase {
    public void testFormatURL() {
        assertEquals("https://example.com/", NotesClientUtil.formatURL("example.com/"));
        assertEquals("http://example.com/", NotesClientUtil.formatURL("http://example.com/"));
        assertEquals("https://example.com/", NotesClientUtil.formatURL("example.com/index.php"));
        assertEquals("https://example.com/", NotesClientUtil.formatURL("example.com/index.php/"));
        assertEquals("https://example.com/", NotesClientUtil.formatURL("example.com/index.php/apps"));
        assertEquals("https://example.com/", NotesClientUtil.formatURL("example.com/index.php/apps/notes"));
        assertEquals("https://example.com/", NotesClientUtil.formatURL("example.com/index.php/apps/notes/api"));
        assertEquals("https://example.com/", NotesClientUtil.formatURL("example.com/index.php/apps/notes/api/v0.2"));
        assertEquals("https://example.com/", NotesClientUtil.formatURL("example.com/index.php/apps/notes/api/v0.2/notes"));
        assertEquals("https://example.com/nextcloud/", NotesClientUtil.formatURL("example.com/nextcloud"));
        assertEquals("http://example.com:443/nextcloud/", NotesClientUtil.formatURL("http://example.com:443/nextcloud/index.php/apps/notes/api/v0.2/notes"));
    }

    public void testIsHttp() {
        assertTrue(NotesClientUtil.isHttp("http://example.com"));
        assertTrue(NotesClientUtil.isHttp("http://www.example.com/"));
        assertFalse(NotesClientUtil.isHttp("https://www.example.com/"));
        assertFalse(NotesClientUtil.isHttp(null));
    }

    public void testIsValidURLTest() {
        assertTrue(NotesClientUtil.isValidURL(null, "https://demo.owncloud.org/"));
        assertFalse(NotesClientUtil.isValidURL(null, "https://www.example.com/"));
        assertFalse(NotesClientUtil.isValidURL(null, "htp://www.example.com/"));
        assertFalse(NotesClientUtil.isValidURL(null, null));
    }
}