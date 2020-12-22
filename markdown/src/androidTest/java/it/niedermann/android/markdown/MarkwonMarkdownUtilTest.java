package it.niedermann.android.markdown;

import android.text.Editable;
import android.text.SpannableStringBuilder;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import it.niedermann.android.markdown.markwon.MarkwonMarkdownUtil;

@RunWith(AndroidJUnit4.class)
public class MarkwonMarkdownUtilTest extends TestCase {

    @Test
    public void testGetStartOfLine() {
        //language=md
        StringBuilder test = new StringBuilder(
                "# Test-Note\n" + // line start 0
                        "\n" + // line start 12
                        "- [ ] this is a test note\n" + // line start 13
                        "- [x] test\n" + // line start 39
                        "[test](https://example.com)\n" + // line start 50
                        "\n" + // line start 77
                        "\n" // line start 78
        );

        for (int i = 0; i < test.length(); i++) {
            int startOfLine = MarkwonMarkdownUtil.getStartOfLine(test, i);
            if (i <= 11) {
                assertEquals(0, startOfLine);
            } else if (i <= 12) {
                assertEquals(12, startOfLine);
            } else if (i <= 38) {
                assertEquals(13, startOfLine);
            } else if (i <= 49) {
                assertEquals(39, startOfLine);
            } else if (i <= 77) {
                assertEquals(50, startOfLine);
            } else if (i <= 78) {
                assertEquals(78, startOfLine);
            } else if (i <= 79) {
                assertEquals(79, startOfLine);
            }
        }
    }

    @Test
    public void testGetEndOfLine() {
        //language=md
        final CharSequence test = "# Test-Note\n" + // line 0 - 11
                "\n" + // line 12 - 12
                "- [ ] this is a test note\n" + // line 13 - 38
                "- [x] test\n" + // line start 39 - 49
                "[test](https://example.com)\n" + // line 50 - 77
                "\n" + // line 77 - 78
                "\n"; // line 78 - 79

        for (int i = 0; i < test.length(); i++) {
            int endOfLine = MarkwonMarkdownUtil.getEndOfLine(test, i);
            if (i <= 11) {
                assertEquals(11, endOfLine);
            } else if (i <= 12) {
                assertEquals(12, endOfLine);
            } else if (i <= 38) {
                assertEquals(38, endOfLine);
            } else if (i <= 49) {
                assertEquals(49, endOfLine);
            } else if (i <= 77) {
                assertEquals(77, endOfLine);
            } else if (i <= 78) {
                assertEquals(78, endOfLine);
            } else if (i <= 79) {
                assertEquals(79, endOfLine);
            }
        }
    }

    @Test
    public void testLineStartsWithCheckbox() {
        final Map<String, Boolean> lines = new HashMap<>();
        lines.put("- [ ] ", true);
        lines.put("- [x] ", true);
        lines.put("* [ ] ", true);
        lines.put("* [x] ", true);
        lines.put("+ [ ] ", true);
        lines.put("+ [x] ", true);
        lines.put("- [ ]", true);
        lines.put("- [x]", true);
        lines.put("* [ ]", true);
        lines.put("* [x]", true);
        lines.put("+ [ ]", true);
        lines.put("+ [x]", true);

        lines.put("-[ ] ", false);
        lines.put("-[x] ", false);
        lines.put("*[ ] ", false);
        lines.put("*[x] ", false);
        lines.put("+[ ] ", false);
        lines.put("+[x] ", false);
        lines.put("-[ ]", false);
        lines.put("-[x]", false);
        lines.put("*[ ]", false);
        lines.put("*[x]", false);
        lines.put("+[ ]", false);
        lines.put("+[x]", false);

        lines.put("- [] ", false);
        lines.put("* [] ", false);
        lines.put("+ [] ", false);
        lines.put("- []", false);
        lines.put("* []", false);
        lines.put("+ []", false);

        lines.put("-[] ", false);
        lines.put("*[] ", false);
        lines.put("+[] ", false);
        lines.put("-[]", false);
        lines.put("*[]", false);
        lines.put("+[]", false);

        lines.forEach((key, value) -> assertEquals(value, (Boolean) MarkwonMarkdownUtil.lineStartsWithCheckbox(key)));
    }

    @Test
    public void testTogglePunctuation() {
        Editable builder;

        // Add italic
        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
        assertEquals(13, MarkwonMarkdownUtil.togglePunctuation(builder, 6, 11, "*"));
        assertEquals("Lorem *ipsum* dolor sit amet.", builder.toString());

        // Remove italic
        builder = new SpannableStringBuilder("Lorem *ipsum* dolor sit amet.");
        assertEquals(11, MarkwonMarkdownUtil.togglePunctuation(builder, 7, 12, "*"));
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString());

        // Add bold
        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
        assertEquals(15, MarkwonMarkdownUtil.togglePunctuation(builder, 6, 11, "**"));
        assertEquals("Lorem **ipsum** dolor sit amet.", builder.toString());

        // Remove bold
        builder = new SpannableStringBuilder("Lorem **ipsum** dolor sit amet.");
        assertEquals(11, MarkwonMarkdownUtil.togglePunctuation(builder, 8, 13, "**"));
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString());

        // Add strike
        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
        assertEquals(15, MarkwonMarkdownUtil.togglePunctuation(builder, 6, 11, "~~"));
        assertEquals("Lorem ~~ipsum~~ dolor sit amet.", builder.toString());

        // Remove strike
        builder = new SpannableStringBuilder("Lorem ~~ipsum~~ dolor sit amet.");
        assertEquals(11, MarkwonMarkdownUtil.togglePunctuation(builder, 8, 13, "~~"));
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString());

        // Add italic at first position
        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
        assertEquals(7, MarkwonMarkdownUtil.togglePunctuation(builder, 0, 5, "*"));
        assertEquals("*Lorem* ipsum dolor sit amet.", builder.toString());

        // Remove italic from first position
        builder = new SpannableStringBuilder("*Lorem* ipsum dolor sit amet.");
        assertEquals(5, MarkwonMarkdownUtil.togglePunctuation(builder, 1, 6, "*"));
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString());

        // Add italic at last position
        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
        assertEquals(29, MarkwonMarkdownUtil.togglePunctuation(builder, 22, 27, "*"));
        assertEquals("Lorem ipsum dolor sit *amet.*", builder.toString());

        // Remove italic from last position
        builder = new SpannableStringBuilder("Lorem ipsum dolor sit *amet.*");
        assertEquals(27, MarkwonMarkdownUtil.togglePunctuation(builder, 23, 28, "*"));
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString());

        // Text is not directly surrounded by punctuation but contains it

        // Do nothing when the same punctuation is contained only one time
        builder = new SpannableStringBuilder("Lorem *ipsum dolor sit amet.");
        assertEquals(28, MarkwonMarkdownUtil.togglePunctuation(builder, 0, 28, "*"));
        assertEquals("Lorem *ipsum dolor sit amet.", builder.toString());

        // Do nothing when the same punctuation is contained only one time
        builder = new SpannableStringBuilder("Lorem **ipsum dolor sit amet.");
        assertEquals(29, MarkwonMarkdownUtil.togglePunctuation(builder, 0, 29, "**"));
        assertEquals("Lorem **ipsum dolor sit amet.", builder.toString());

        // Remove containing punctuation
        builder = new SpannableStringBuilder("Lorem *ipsum* dolor sit amet.");
        assertEquals(11, MarkwonMarkdownUtil.togglePunctuation(builder, 6, 13, "*"));
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString());

        // Remove containing punctuation
        builder = new SpannableStringBuilder("Lorem *ipsum* dolor sit amet.");
        assertEquals(27, MarkwonMarkdownUtil.togglePunctuation(builder, 0, 29, "*"));
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString());

        // Remove multiple containing punctuations
        builder = new SpannableStringBuilder("Lorem *ipsum* dolor *sit* amet.");
        assertEquals(27, MarkwonMarkdownUtil.togglePunctuation(builder, 0, 31, "*"));
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString());

        // Special use-case: toggle from italic to bold and back

        // TODO Toggle italic on bold text
//        builder = new SpannableStringBuilder("Lorem **ipsum** dolor sit amet.");
//        assertEquals(17, MarkwonMarkdownUtil.togglePunctuation(builder, 8, 13, "*"));
//        assertEquals("Lorem ***ipsum*** dolor sit amet.", builder.toString());

        // TODO Toggle bold on italic text
//        builder = new SpannableStringBuilder("Lorem *ipsum* dolor sit amet.");
//        assertEquals(17, MarkwonMarkdownUtil.togglePunctuation(builder, 7, 12, "**"));
//        assertEquals("Lorem ***ipsum*** dolor sit amet.", builder.toString());

        // TODO Toggle bold to italic
//        builder = new SpannableStringBuilder("Lorem **ipsum** dolor sit amet.");
//        assertEquals(29, MarkwonMarkdownUtil.togglePunctuation(builder, 0, 31, "*"));
//        assertEquals("Lorem *ipsum* dolor sit amet.", builder.toString());

        // TODO Toggle multiple bold parts to italic
//        builder = new SpannableStringBuilder("Lorem **ipsum** dolor **sit** amet.");
//        assertEquals(31, MarkwonMarkdownUtil.togglePunctuation(builder, 0, 35, "*"));
//        assertEquals("Lorem *ipsum* dolor *sit* amet.", builder.toString());
    }

    @Test
    public void testInsertLink() {
        Editable builder;

        // Add link without clipboardUrl to normal text
        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
        assertEquals(14, MarkwonMarkdownUtil.insertLink(builder, 6, 11, null));
        assertEquals("Lorem [ipsum]() dolor sit amet.", builder.toString());

        // Add link without clipboardUrl to url
        builder = new SpannableStringBuilder("Lorem https://example.com dolor sit amet.");
        assertEquals(7, MarkwonMarkdownUtil.insertLink(builder, 6, 25, null));
        assertEquals("Lorem [](https://example.com) dolor sit amet.", builder.toString());

        // TODO Add link without clipboardUrl to empty selection before space character
//        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
//        assertEquals(13, MarkwonMarkdownUtil.insertLink(builder, 11, 11, null));
//        assertEquals("Lorem ipsum []() dolor sit amet.", builder.toString());

        // TODO Add link without clipboardUrl to empty selection after space character
//        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
//        assertEquals(13, MarkwonMarkdownUtil.insertLink(builder, 12, 12, null));
//        assertEquals("Lorem ipsum []() dolor sit amet.", builder.toString());

        // TODO Add link without clipboardUrl to empty selection in word
//        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
//        assertEquals(20, MarkwonMarkdownUtil.insertLink(builder, 14, 14, null));
//        assertEquals("Lorem ipsum [dolor]() sit amet.", builder.toString());

        // Add link with clipboardUrl to normal text
        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
        assertEquals(33, MarkwonMarkdownUtil.insertLink(builder, 6, 11, "https://example.com"));
        assertEquals("Lorem [ipsum](https://example.com) dolor sit amet.", builder.toString());

        // Add link with clipboardUrl to url
        builder = new SpannableStringBuilder("Lorem https://example.com dolor sit amet.");
        assertEquals(46, MarkwonMarkdownUtil.insertLink(builder, 6, 25, "https://example.de"));
        assertEquals("Lorem [https://example.com](https://example.de) dolor sit amet.", builder.toString());

        // TODO Add link with clipboardUrl to empty selection before space character
//        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
//        assertEquals(13, MarkwonMarkdownUtil.insertLink(builder, 11, 11, "https://example.de"));
//        assertEquals("Lorem ipsum []("https://example.de") dolor sit amet.", builder.toString());

        // TODO Add link with clipboardUrl to empty selection after space character
//        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
//        assertEquals(13, MarkwonMarkdownUtil.insertLink(builder, 12, 12, "https://example.de"));
//        assertEquals("Lorem ipsum []("https://example.de") dolor sit amet.", builder.toString());

        // TODO Add link with clipboardUrl to empty selection in word
//        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
//        assertEquals(18, MarkwonMarkdownUtil.insertLink(builder, 14, 14, "https://example.de"));
//        assertEquals("Lorem ipsum [dolor]("https://example.de") sit amet.", builder.toString());
    }

    @Test
    public void testRemoveContainingPunctuation() {
        try {
            final Method m = MarkwonMarkdownUtil.class.getDeclaredMethod("removeContainingPunctuation", Editable.class, int.class, int.class, String.class);
            m.setAccessible(true);
            Editable builder;

            builder = new SpannableStringBuilder("Lorem *ipsum* dolor");
            m.invoke(null, builder, 0, 19, "*");
            assertEquals("Lorem ipsum dolor", builder.toString());

            builder = new SpannableStringBuilder("*Lorem ipsum dolor*");
            m.invoke(null, builder, 0, 19, "*");
            assertEquals("Lorem ipsum dolor", builder.toString());

            builder = new SpannableStringBuilder("**Lorem ipsum**");
            m.invoke(null, builder, 0, 15, "**");
            assertEquals("Lorem ipsum", builder.toString());

            builder = new SpannableStringBuilder("*Lorem* *ipsum*");
            m.invoke(null, builder, 0, 15, "*");
            assertEquals("Lorem ipsum", builder.toString());

            builder = new SpannableStringBuilder("Lorem* ipsum");
            m.invoke(null, builder, 0, 12, "*");
            assertEquals("Lorem ipsum", builder.toString());

            builder = new SpannableStringBuilder("*Lorem* ipsum*");
            m.invoke(null, builder, 0, 14, "*");
            assertEquals("Lorem ipsum", builder.toString());

            builder = new SpannableStringBuilder("**Lorem ipsum**");
            m.invoke(null, builder, 0, 15, "*");
            assertEquals("Lorem ipsum", builder.toString());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testSelectionIsSurroundedByPunctuation() {
        try {
            final Method m = MarkwonMarkdownUtil.class.getDeclaredMethod("selectionIsSurroundedByPunctuation", CharSequence.class, int.class, int.class, String.class);
            m.setAccessible(true);
            assertTrue((Boolean) m.invoke(null, "*Lorem ipsum*", 1, 12, "*"));
            assertTrue((Boolean) m.invoke(null, "**Lorem ipsum**", 2, 13, "*"));
            assertTrue((Boolean) m.invoke(null, "**Lorem ipsum**", 2, 13, "**"));

            assertFalse((Boolean) m.invoke(null, "*Lorem ipsum*", 0, 12, "*"));
            assertFalse((Boolean) m.invoke(null, "*Lorem ipsum*", 1, 13, "*"));
            assertFalse((Boolean) m.invoke(null, "*Lorem ipsum*", 0, 13, "*"));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testGetContainedPunctuationCount() {
        try {
            final Method m = MarkwonMarkdownUtil.class.getDeclaredMethod("getContainedPunctuationCount", CharSequence.class, int.class, int.class, String.class);
            m.setAccessible(true);
            assertEquals(0, (int) m.invoke(null, "*Lorem ipsum*", 1, 12, "*"));
            assertEquals(1, (int) m.invoke(null, "*Lorem ipsum*", 1, 13, "*"));
            assertEquals(2, (int) m.invoke(null, "*Lorem ipsum*", 0, 13, "*"));
            assertEquals(0, (int) m.invoke(null, "*Lorem ipsum*", 0, 13, "**"));
            assertEquals(0, (int) m.invoke(null, "*Lorem ipsum**", 0, 13, "**"));
            assertEquals(1, (int) m.invoke(null, "*Lorem ipsum**", 0, 14, "**"));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testSelectionIsInLink() {
        try {
            final Method m = MarkwonMarkdownUtil.class.getDeclaredMethod("selectionIsInLink", CharSequence.class, int.class, int.class);
            m.setAccessible(true);

            assertTrue((Boolean) m.invoke(null, "Lorem [ipsum](https://example.com) dolor sit amet.", 7, 12));
            assertTrue((Boolean) m.invoke(null, "Lorem [ipsum](https://example.com) dolor sit amet.", 6, 34));
            assertTrue((Boolean) m.invoke(null, "Lorem [ipsum](https://example.com) dolor sit amet.", 14, 33));
            assertTrue((Boolean) m.invoke(null, "Lorem [ipsum](https://example.com) dolor sit amet.", 12, 14));
            assertTrue((Boolean) m.invoke(null, "Lorem [ipsum](https://example.com) dolor sit amet.", 0, 7));
            assertTrue((Boolean) m.invoke(null, "Lorem [ipsum](https://example.com) dolor sit amet.", 33, 34));

            assertTrue((Boolean) m.invoke(null, "Lorem [](https://example.com) dolor sit amet.", 6, 28));
            assertTrue((Boolean) m.invoke(null, "Lorem [](https://example.com) dolor sit amet.", 7, 28));
            assertTrue((Boolean) m.invoke(null, "Lorem [](https://example.com) dolor sit amet.", 8, 28));
            assertTrue((Boolean) m.invoke(null, "Lorem [](https://example.com) dolor sit amet.", 9, 28));
            assertTrue((Boolean) m.invoke(null, "Lorem [](https://example.com) dolor sit amet.", 6, 29));
            assertTrue((Boolean) m.invoke(null, "Lorem [](https://example.com) dolor sit amet.", 7, 29));
            assertTrue((Boolean) m.invoke(null, "Lorem [](https://example.com) dolor sit amet.", 8, 29));
            assertTrue((Boolean) m.invoke(null, "Lorem [](https://example.com) dolor sit amet.", 9, 29));

            assertTrue((Boolean) m.invoke(null, "Lorem [ipsum]() dolor sit amet.", 6, 12));
            assertTrue((Boolean) m.invoke(null, "Lorem [ipsum]() dolor sit amet.", 6, 13));
            assertTrue((Boolean) m.invoke(null, "Lorem [ipsum]() dolor sit amet.", 6, 14));
            assertTrue((Boolean) m.invoke(null, "Lorem [ipsum]() dolor sit amet.", 6, 15));
            assertTrue((Boolean) m.invoke(null, "Lorem [ipsum]() dolor sit amet.", 7, 12));
            assertTrue((Boolean) m.invoke(null, "Lorem [ipsum]() dolor sit amet.", 7, 13));
            assertTrue((Boolean) m.invoke(null, "Lorem [ipsum]() dolor sit amet.", 7, 14));
            assertTrue((Boolean) m.invoke(null, "Lorem [ipsum]() dolor sit amet.", 7, 15));

            assertFalse((Boolean) m.invoke(null, "Lorem [ipsum](https://example.com) dolor sit amet.", 0, 6));
            assertFalse((Boolean) m.invoke(null, "Lorem [ipsum](https://example.com) dolor sit amet.", 34, 50));
            assertFalse((Boolean) m.invoke(null, "Lorem [ipsum](https://example.com) dolor sit amet.", 41, 44));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetListItemIfIsEmpty() {
        assertEquals("- ", MarkwonMarkdownUtil.getListItemIfIsEmpty("- "));
        assertEquals("+ ", MarkwonMarkdownUtil.getListItemIfIsEmpty("+ "));
        assertEquals("* ", MarkwonMarkdownUtil.getListItemIfIsEmpty("* "));
        assertEquals("1. ", MarkwonMarkdownUtil.getListItemIfIsEmpty("1. "));

        assertNull(MarkwonMarkdownUtil.getListItemIfIsEmpty("- Test"));
        assertNull(MarkwonMarkdownUtil.getListItemIfIsEmpty("+ Test"));
        assertNull(MarkwonMarkdownUtil.getListItemIfIsEmpty("* Test"));
        assertNull(MarkwonMarkdownUtil.getListItemIfIsEmpty("1. s"));
        assertNull(MarkwonMarkdownUtil.getListItemIfIsEmpty("1.  "));
    }

    @Test
    public void testLineStartsWithOrderedList() {
        assertEquals(1, MarkwonMarkdownUtil.getOrderedListNumber("1. Test"));
        assertEquals(2, MarkwonMarkdownUtil.getOrderedListNumber("2. Test"));
        assertEquals(3, MarkwonMarkdownUtil.getOrderedListNumber("3. Test"));
        assertEquals(10, MarkwonMarkdownUtil.getOrderedListNumber("10. Test"));
        assertEquals(11, MarkwonMarkdownUtil.getOrderedListNumber("11. Test"));
        assertEquals(12, MarkwonMarkdownUtil.getOrderedListNumber("12. Test"));
        assertEquals(1, MarkwonMarkdownUtil.getOrderedListNumber("1. 1"));
        assertEquals(1, MarkwonMarkdownUtil.getOrderedListNumber("1. Test 1"));

        assertEquals(-1, MarkwonMarkdownUtil.getOrderedListNumber(""));
        assertEquals(-1, MarkwonMarkdownUtil.getOrderedListNumber("1."));
        assertEquals(-1, MarkwonMarkdownUtil.getOrderedListNumber("1. "));
        assertEquals(-1, MarkwonMarkdownUtil.getOrderedListNumber("11. "));
        assertEquals(-1, MarkwonMarkdownUtil.getOrderedListNumber("-1. Test"));
        assertEquals(-1, MarkwonMarkdownUtil.getOrderedListNumber(" 1. Test"));
    }

//    @Test
//    public void testRemoveSpans() {
//        try {
//            final Method m = MarkwonMarkdownUtil.class.getDeclaredMethod("removeSpans", Editable.class, Class.class);
//            m.setAccessible(true);
//
//            Editable editable;
//
//            editable = new SpannableStringBuilder("Lorem Ipsum dolor sit amet");
//            editable.setSpan(SearchSpan.class, 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            editable.setSpan(ForegroundColorSpan.class, 6, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            editable.setSpan(SearchSpan.class, 12, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            m.invoke(null, editable, SearchSpan.class);
//            assertEquals(0, editable.getSpans(0, editable.length(), SearchSpan.class).length);
//            assertEquals(1, editable.getSpans(0, editable.length(), ForegroundColorSpan.class).length);
//
//        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//            e.printStackTrace();
//        }
//    }
}