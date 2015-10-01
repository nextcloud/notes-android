package it.niedermann.owncloud.notes.util;

import junit.framework.TestCase;

/**
 * Tests the URLValidatorAsyncTask
 * Created by stefan on 24.09.15.
 */
public class URLValidatorAsyncTaskTest extends TestCase {
    public void testIsHttp() {
        assertTrue(URLValidatorAsyncTask.isHttp("http://www.example.com/"));
        assertFalse(URLValidatorAsyncTask.isHttp("https://www.example.com/"));
    }
}
