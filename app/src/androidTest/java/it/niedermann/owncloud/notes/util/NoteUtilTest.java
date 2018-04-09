package it.niedermann.owncloud.notes.util;

import junit.framework.TestCase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
        try {
            Method m = NoteUtil.class.getDeclaredMethod("isEmptyLine");
            m.setAccessible(true);
            assertTrue((Boolean) m.invoke(null, " "));
            assertTrue((Boolean) m.invoke(null, "\n"));
            assertTrue((Boolean) m.invoke(null, "\n "));
            assertTrue((Boolean) m.invoke(null, " \n"));
            assertTrue((Boolean) m.invoke(null, " \n "));
            assertFalse((Boolean) m.invoke(null, "a \n "));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void testGetLineWithoutMarkDown() {
        try {
            Method m = NoteUtil.class.getDeclaredMethod("isEmptyLine");
            m.setAccessible(true);
            assertEquals("Test", (String) m.invoke(null, "Test", 0));
            assertEquals("Test", (String) m.invoke(null, "\nTest", 0));
            assertEquals("Foo", (String) m.invoke(null, "Foo\nBar", 0));
            assertEquals("Bar", (String) m.invoke(null, "Foo\nBar", 1));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void testGenerateNoteTitle() {
        assertEquals("Test", NoteUtil.generateNoteTitle("Test"));
        assertEquals("Test", NoteUtil.generateNoteTitle("Test\n"));
        assertEquals("Test", NoteUtil.generateNoteTitle("Test\nFoo"));
        assertEquals("Test", NoteUtil.generateNoteTitle("\nTest"));
        assertEquals("Test", NoteUtil.generateNoteTitle("\n\nTest"));
    }

    public void testGenerateNoteExcerpt() {
        assertEquals("", NoteUtil.generateNoteExcerpt("Test"));
        assertEquals("Foo", NoteUtil.generateNoteExcerpt("Test\nFoo"));
        assertEquals("Foo   Bar", NoteUtil.generateNoteExcerpt("Test\nFoo\nBar"));
        assertEquals("", NoteUtil.generateNoteExcerpt(""));
    }
}