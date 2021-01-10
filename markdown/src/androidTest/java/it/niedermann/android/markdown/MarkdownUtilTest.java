package it.niedermann.android.markdown;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import it.niedermann.android.markdown.model.EListType;
import it.niedermann.android.markdown.model.SearchSpan;

@RunWith(AndroidJUnit4.class)
public class MarkdownUtilTest extends TestCase {

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
            int startOfLine = MarkdownUtil.getStartOfLine(test, i);
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
            int endOfLine = MarkdownUtil.getEndOfLine(test, i);
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
        lines.put("  - [ ] a", true);
        lines.put("  - [x] a", true);
        lines.put("  * [ ] a", true);
        lines.put("  * [x] a", true);
        lines.put("  + [ ] a", true);
        lines.put("  + [x] a", true);
        lines.put("- [ ] a", true);
        lines.put("- [x] a", true);
        lines.put("* [ ] a", true);
        lines.put("* [x] a", true);
        lines.put("+ [ ] a", true);
        lines.put("+ [x] a", true);
        lines.put("  - [ ] ", true);
        lines.put("  - [x] ", true);
        lines.put("  * [ ] ", true);
        lines.put("  * [x] ", true);
        lines.put("  + [ ] ", true);
        lines.put("  + [x] ", true);
        lines.put("  - [ ]", true);
        lines.put("  - [x]", true);
        lines.put("  * [ ]", true);
        lines.put("  * [x]", true);
        lines.put("  + [ ]", true);
        lines.put("  + [x]", true);
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

        lines.forEach((key, value) -> assertEquals(value, (Boolean) MarkdownUtil.lineStartsWithCheckbox(key)));
    }

    @Test
    public void testTogglePunctuation() {
        Editable builder;

        // Add italic
        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
        assertEquals(13, MarkdownUtil.togglePunctuation(builder, 6, 11, "*"));
        assertEquals("Lorem *ipsum* dolor sit amet.", builder.toString());

        // Remove italic
        builder = new SpannableStringBuilder("Lorem *ipsum* dolor sit amet.");
        assertEquals(11, MarkdownUtil.togglePunctuation(builder, 7, 12, "*"));
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString());

        // Add bold
        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
        assertEquals(15, MarkdownUtil.togglePunctuation(builder, 6, 11, "**"));
        assertEquals("Lorem **ipsum** dolor sit amet.", builder.toString());

        // Remove bold
        builder = new SpannableStringBuilder("Lorem **ipsum** dolor sit amet.");
        assertEquals(11, MarkdownUtil.togglePunctuation(builder, 8, 13, "**"));
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString());

        // Add strike
        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
        assertEquals(15, MarkdownUtil.togglePunctuation(builder, 6, 11, "~~"));
        assertEquals("Lorem ~~ipsum~~ dolor sit amet.", builder.toString());

        // Remove strike
        builder = new SpannableStringBuilder("Lorem ~~ipsum~~ dolor sit amet.");
        assertEquals(11, MarkdownUtil.togglePunctuation(builder, 8, 13, "~~"));
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString());

        // Add italic at first position
        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
        assertEquals(7, MarkdownUtil.togglePunctuation(builder, 0, 5, "*"));
        assertEquals("*Lorem* ipsum dolor sit amet.", builder.toString());

        // Remove italic from first position
        builder = new SpannableStringBuilder("*Lorem* ipsum dolor sit amet.");
        assertEquals(5, MarkdownUtil.togglePunctuation(builder, 1, 6, "*"));
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString());

        // Add italic at last position
        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
        assertEquals(29, MarkdownUtil.togglePunctuation(builder, 22, 27, "*"));
        assertEquals("Lorem ipsum dolor sit *amet.*", builder.toString());

        // Remove italic from last position
        builder = new SpannableStringBuilder("Lorem ipsum dolor sit *amet.*");
        assertEquals(27, MarkdownUtil.togglePunctuation(builder, 23, 28, "*"));
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString());

        // Text is not directly surrounded by punctuation but contains it

        // Do nothing when the same punctuation is contained only one time
        builder = new SpannableStringBuilder("Lorem *ipsum dolor sit amet.");
        assertEquals(28, MarkdownUtil.togglePunctuation(builder, 0, 28, "*"));
        assertEquals("Lorem *ipsum dolor sit amet.", builder.toString());

        // Do nothing when the same punctuation is contained only one time
        builder = new SpannableStringBuilder("Lorem **ipsum dolor sit amet.");
        assertEquals(29, MarkdownUtil.togglePunctuation(builder, 0, 29, "**"));
        assertEquals("Lorem **ipsum dolor sit amet.", builder.toString());

        // Remove containing punctuation
        builder = new SpannableStringBuilder("Lorem *ipsum* dolor sit amet.");
        assertEquals(11, MarkdownUtil.togglePunctuation(builder, 6, 13, "*"));
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString());

        // Remove containing punctuation
        builder = new SpannableStringBuilder("Lorem *ipsum* dolor sit amet.");
        assertEquals(27, MarkdownUtil.togglePunctuation(builder, 0, 29, "*"));
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString());

        // Remove multiple containing punctuations
        builder = new SpannableStringBuilder("Lorem *ipsum* dolor *sit* amet.");
        assertEquals(27, MarkdownUtil.togglePunctuation(builder, 0, 31, "*"));
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString());

        // Special use-case: toggle from italic to bold and back

        // TODO Toggle italic on bold text
//        builder = new SpannableStringBuilder("Lorem **ipsum** dolor sit amet.");
//        assertEquals(17, MarkdownUtil.togglePunctuation(builder, 8, 13, "*"));
//        assertEquals("Lorem ***ipsum*** dolor sit amet.", builder.toString());

        // TODO Toggle bold on italic text
//        builder = new SpannableStringBuilder("Lorem *ipsum* dolor sit amet.");
//        assertEquals(17, MarkdownUtil.togglePunctuation(builder, 7, 12, "**"));
//        assertEquals("Lorem ***ipsum*** dolor sit amet.", builder.toString());

        // TODO Toggle bold to italic
//        builder = new SpannableStringBuilder("Lorem **ipsum** dolor sit amet.");
//        assertEquals(33, MarkdownUtil.togglePunctuation(builder, 0, 31, "*"));
//        assertEquals("Lorem ***ipsum*** dolor sit amet.", builder.toString());

        // TODO Toggle multiple bold parts to italic
//        builder = new SpannableStringBuilder("Lorem **ipsum** dolor **sit** amet.");
//        assertEquals(38, MarkdownUtil.togglePunctuation(builder, 0, 34, "*"));
//        assertEquals("Lorem ***ipsum*** dolor ***sit*** amet.", builder.toString());

        // TODO Toggle italic and bold to bold
//        builder = new SpannableStringBuilder("Lorem ***ipsum*** dolor sit amet.");
//        assertEquals(13, MarkdownUtil.togglePunctuation(builder, 0, 14, "*"));
//        assertEquals("Lorem **ipsum** dolor sit amet.", builder.toString());

        // TODO Toggle italic and bold to italic
//        builder = new SpannableStringBuilder("Lorem ***ipsum*** dolor sit amet.");
//        assertEquals(12, MarkdownUtil.togglePunctuation(builder, 9, 14, "**"));
//        assertEquals("Lorem *ipsum* dolor sit amet.", builder.toString());

        // TODO Toggle multiple italic and bold to bold
//        builder = new SpannableStringBuilder("Lorem ***ipsum*** dolor ***sit*** amet.");
//        assertEquals(34, MarkdownUtil.togglePunctuation(builder, 0, 38, "*"));
//        assertEquals("Lorem **ipsum** dolor **sit** amet.", builder.toString());

        // TODO Toggle multiple italic and bold to italic
//        builder = new SpannableStringBuilder("Lorem ***ipsum*** dolor ***sit*** amet.");
//        assertEquals(30, MarkdownUtil.togglePunctuation(builder, 0, 38, "**"));
//        assertEquals("Lorem *ipsum* dolor *sit* amet.", builder.toString());
    }

    @Test
    public void testInsertLink() {
        Editable builder;

        // Add link without clipboardUrl to normal text
        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
        assertEquals(14, MarkdownUtil.insertLink(builder, 6, 11, null));
        assertEquals("Lorem [ipsum]() dolor sit amet.", builder.toString());

        // Add link without clipboardUrl to url
        builder = new SpannableStringBuilder("Lorem https://example.com dolor sit amet.");
        assertEquals(7, MarkdownUtil.insertLink(builder, 6, 25, null));
        assertEquals("Lorem [](https://example.com) dolor sit amet.", builder.toString());

        // TODO Add link without clipboardUrl to empty selection before space character
//        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
//        assertEquals(13, MarkdownUtil.insertLink(builder, 11, 11, null));
//        assertEquals("Lorem ipsum []() dolor sit amet.", builder.toString());

        // TODO Add link without clipboardUrl to empty selection after space character
//        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
//        assertEquals(13, MarkdownUtil.insertLink(builder, 12, 12, null));
//        assertEquals("Lorem ipsum []() dolor sit amet.", builder.toString());

        // TODO Add link without clipboardUrl to empty selection in word
//        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
//        assertEquals(20, MarkdownUtil.insertLink(builder, 14, 14, null));
//        assertEquals("Lorem ipsum [dolor]() sit amet.", builder.toString());

        // Add link with clipboardUrl to normal text
        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
        assertEquals(33, MarkdownUtil.insertLink(builder, 6, 11, "https://example.com"));
        assertEquals("Lorem [ipsum](https://example.com) dolor sit amet.", builder.toString());

        // Add link with clipboardUrl to url
        builder = new SpannableStringBuilder("Lorem https://example.com dolor sit amet.");
        assertEquals(46, MarkdownUtil.insertLink(builder, 6, 25, "https://example.de"));
        assertEquals("Lorem [https://example.com](https://example.de) dolor sit amet.", builder.toString());

        // TODO Add link with clipboardUrl to empty selection before space character
//        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
//        assertEquals(13, MarkdownUtil.insertLink(builder, 11, 11, "https://example.de"));
//        assertEquals("Lorem ipsum []("https://example.de") dolor sit amet.", builder.toString());

        // TODO Add link with clipboardUrl to empty selection after space character
//        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
//        assertEquals(13, MarkdownUtil.insertLink(builder, 12, 12, "https://example.de"));
//        assertEquals("Lorem ipsum []("https://example.de") dolor sit amet.", builder.toString());

        // TODO Add link with clipboardUrl to empty selection in word
//        builder = new SpannableStringBuilder("Lorem ipsum dolor sit amet.");
//        assertEquals(18, MarkdownUtil.insertLink(builder, 14, 14, "https://example.de"));
//        assertEquals("Lorem ipsum [dolor]("https://example.de") sit amet.", builder.toString());
    }

    @Test
    public void testRemoveContainingPunctuation() {
        try {
            final Method m = MarkdownUtil.class.getDeclaredMethod("removeContainingPunctuation", Editable.class, int.class, int.class, String.class);
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
            final Method m = MarkdownUtil.class.getDeclaredMethod("selectionIsSurroundedByPunctuation", CharSequence.class, int.class, int.class, String.class);
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
            final Method m = MarkdownUtil.class.getDeclaredMethod("getContainedPunctuationCount", CharSequence.class, int.class, int.class, String.class);
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
            final Method m = MarkdownUtil.class.getDeclaredMethod("selectionIsInLink", CharSequence.class, int.class, int.class);
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
        assertEquals("- ", MarkdownUtil.getListItemIfIsEmpty("- "));
        assertEquals("+ ", MarkdownUtil.getListItemIfIsEmpty("+ "));
        assertEquals("* ", MarkdownUtil.getListItemIfIsEmpty("* "));
        assertEquals("1. ", MarkdownUtil.getListItemIfIsEmpty("1. "));

        assertNull(MarkdownUtil.getListItemIfIsEmpty("- Test"));
        assertNull(MarkdownUtil.getListItemIfIsEmpty("+ Test"));
        assertNull(MarkdownUtil.getListItemIfIsEmpty("* Test"));
        assertNull(MarkdownUtil.getListItemIfIsEmpty("1. s"));
        assertNull(MarkdownUtil.getListItemIfIsEmpty("1.  "));
    }

    @Test
    public void testLineStartsWithOrderedList() {
        assertEquals(1, MarkdownUtil.getOrderedListNumber("1. Test"));
        assertEquals(2, MarkdownUtil.getOrderedListNumber("2. Test"));
        assertEquals(3, MarkdownUtil.getOrderedListNumber("3. Test"));
        assertEquals(10, MarkdownUtil.getOrderedListNumber("10. Test"));
        assertEquals(11, MarkdownUtil.getOrderedListNumber("11. Test"));
        assertEquals(12, MarkdownUtil.getOrderedListNumber("12. Test"));
        assertEquals(1, MarkdownUtil.getOrderedListNumber("1. 1"));
        assertEquals(1, MarkdownUtil.getOrderedListNumber("1. Test 1"));

        assertEquals(-1, MarkdownUtil.getOrderedListNumber(""));
        assertEquals(-1, MarkdownUtil.getOrderedListNumber("1."));
        assertEquals(-1, MarkdownUtil.getOrderedListNumber("1. "));
        assertEquals(-1, MarkdownUtil.getOrderedListNumber("11. "));
        assertEquals(-1, MarkdownUtil.getOrderedListNumber("-1. Test"));
        assertEquals(-1, MarkdownUtil.getOrderedListNumber(" 1. Test"));
    }

    @Test
    public void testSetCheckboxStatus() {
        for (EListType listType : EListType.values()) {
            final String origin_1 = listType.checkboxUnchecked + " Item";
            final String expected_1 = listType.checkboxChecked + " Item";
            assertEquals(expected_1, MarkdownUtil.setCheckboxStatus(origin_1, 0, true));

            final String origin_2 = listType.checkboxChecked + " Item";
            final String expected_2 = listType.checkboxChecked + " Item";
            assertEquals(expected_2, MarkdownUtil.setCheckboxStatus(origin_2, 0, true));

            final String origin_3 = listType.checkboxChecked + " Item";
            final String expected_3 = listType.checkboxChecked + " Item";
            assertEquals(expected_3, MarkdownUtil.setCheckboxStatus(origin_3, -1, true));

            final String origin_4 = listType.checkboxChecked + " Item";
            final String expected_4 = listType.checkboxChecked + " Item";
            assertEquals(expected_4, MarkdownUtil.setCheckboxStatus(origin_4, 3, true));

            final String origin_5 = "" +
                    listType.checkboxChecked + " Item\n" +
                    listType.checkboxChecked + " Item";
            final String expected_5 = "" +
                    listType.checkboxChecked + " Item\n" +
                    listType.checkboxUnchecked + " Item";
            assertEquals(expected_5, MarkdownUtil.setCheckboxStatus(origin_5, 1, false));

            // Checkboxes in fenced code block aren't rendered by Markwon and therefore don't count to the checkbox index
            final String origin_6 = "" +
                    listType.checkboxChecked + " Item\n" +
                    "```\n" +
                    listType.checkboxUnchecked + " Item\n" +
                    "```\n" +
                    listType.checkboxUnchecked + " Item";
            final String expected_6 = "" +
                    listType.checkboxChecked + " Item\n" +
                    "```\n" +
                    listType.checkboxUnchecked + " Item\n" +
                    "```\n" +
                    listType.checkboxChecked + " Item";
            assertEquals(expected_6, MarkdownUtil.setCheckboxStatus(origin_6, 1, true));

            // Checkbox in partial nested fenced code block does not count as rendered checkbox
            final String origin_7 = "" +
                    listType.checkboxChecked + " Item\n" +
                    "````\n" +
                    "```\n" +
                    listType.checkboxUnchecked + " Item\n" +
                    "````\n" +
                    listType.checkboxUnchecked + " Item";
            final String expected_7 = "" +
                    listType.checkboxChecked + " Item\n" +
                    "````\n" +
                    "```\n" +
                    listType.checkboxUnchecked + " Item\n" +
                    "````\n" +
                    listType.checkboxChecked + " Item";
            assertEquals(expected_7, MarkdownUtil.setCheckboxStatus(origin_7, 1, true));

            // Checkbox in complete nested fenced code block does not count as rendered checkbox
            final String origin_8 = "" +
                    listType.checkboxChecked + " Item\n" +
                    "````\n" +
                    "```\n" +
                    listType.checkboxUnchecked + " Item\n" +
                    "```\n" +
                    "````\n" +
                    listType.checkboxUnchecked + " Item";
            final String expected_8 = "" +
                    listType.checkboxChecked + " Item\n" +
                    "````\n" +
                    "```\n" +
                    listType.checkboxUnchecked + " Item\n" +
                    "```\n" +
                    "````\n" +
                    listType.checkboxChecked + " Item";
            assertEquals(expected_8, MarkdownUtil.setCheckboxStatus(origin_8, 1, true));

            // If checkbox has no content, it doesn't get rendered by Markwon and therefore can not be checked
            final String origin_9 = "" +
                    listType.checkboxChecked + " Item\n" +
                    "````\n" +
                    "```\n" +
                    listType.checkboxUnchecked + " Item\n" +
                    "```\n" +
                    "````\n" +
                    listType.checkboxUnchecked + " \n" +
                    listType.checkboxUnchecked + " Item";
            final String expected_9 = "" +
                    listType.checkboxChecked + " Item\n" +
                    "````\n" +
                    "```\n" +
                    listType.checkboxUnchecked + " Item\n" +
                    "```\n" +
                    "````\n" +
                    listType.checkboxUnchecked + " \n" +
                    listType.checkboxChecked + " Item";
            assertEquals(expected_9, MarkdownUtil.setCheckboxStatus(origin_9, 1, true));
        }
    }

    @Test
    public void testRemoveSpans() {
        try {
            final Method removeSpans = MarkdownUtil.class.getDeclaredMethod("removeSpans", Spannable.class, Class.class);
            removeSpans.setAccessible(true);

            final Context context = ApplicationProvider.getApplicationContext();

            final Editable editable_1 = new SpannableStringBuilder("Lorem Ipsum dolor sit amet");
            editable_1.setSpan(new SearchSpan(Color.RED, Color.GRAY, false, false), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            editable_1.setSpan(new ForegroundColorSpan(Color.BLUE), 6, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            editable_1.setSpan(new SearchSpan(Color.BLUE, Color.GREEN, true, false), 12, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            removeSpans.invoke(null, editable_1, SearchSpan.class);
            assertEquals(0, editable_1.getSpans(0, editable_1.length(), SearchSpan.class).length);
            assertEquals(1, editable_1.getSpans(0, editable_1.length(), ForegroundColorSpan.class).length);

            final Editable editable_2 = new SpannableStringBuilder("Lorem Ipsum dolor sit amet");
            editable_2.setSpan(new SearchSpan(Color.GRAY, Color.RED, false, true), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            editable_2.setSpan(new ForegroundColorSpan(Color.BLUE), 2, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            editable_2.setSpan(new SearchSpan(Color.BLUE, Color.GREEN, true, false), 3, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            removeSpans.invoke(null, editable_2, SearchSpan.class);
            assertEquals(0, editable_2.getSpans(0, editable_2.length(), SearchSpan.class).length);
            assertEquals(1, editable_2.getSpans(0, editable_2.length(), ForegroundColorSpan.class).length);
            assertEquals(2, editable_2.getSpanStart(editable_2.getSpans(0, editable_2.length(), ForegroundColorSpan.class)[0]));
            assertEquals(7, editable_2.getSpanEnd(editable_2.getSpans(0, editable_2.length(), ForegroundColorSpan.class)[0]));

            final Editable editable_3 = new SpannableStringBuilder("Lorem Ipsum dolor sit amet");
            editable_3.setSpan(new ForegroundColorSpan(Color.BLUE), 2, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            removeSpans.invoke(null, editable_3, SearchSpan.class);
            assertEquals(0, editable_3.getSpans(0, editable_3.length(), SearchSpan.class).length);
            assertEquals(1, editable_3.getSpans(0, editable_3.length(), ForegroundColorSpan.class).length);
            assertEquals(2, editable_3.getSpanStart(editable_3.getSpans(0, editable_3.length(), ForegroundColorSpan.class)[0]));
            assertEquals(7, editable_3.getSpanEnd(editable_3.getSpans(0, editable_3.length(), ForegroundColorSpan.class)[0]));

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRemoveMarkdown() {
        assertEquals("Test", MarkdownUtil.removeMarkdown("Test"));
        assertEquals("Foo\nBar", MarkdownUtil.removeMarkdown("Foo\nBar"));
        assertEquals("Foo\nBar", MarkdownUtil.removeMarkdown("Foo\n  Bar"));
        assertEquals("Foo\nBar", MarkdownUtil.removeMarkdown("Foo   \nBar"));
        assertEquals("Foo-Bar", MarkdownUtil.removeMarkdown("Foo-Bar"));
        assertEquals("Foo*Bar", MarkdownUtil.removeMarkdown("Foo*Bar"));
        assertEquals("Foo/Bar", MarkdownUtil.removeMarkdown("Foo/Bar"));
        assertEquals("FooTestBar", MarkdownUtil.removeMarkdown("Foo*Test*Bar"));
        assertEquals("FooTestBar", MarkdownUtil.removeMarkdown("Foo**Test**Bar"));
        assertEquals("FooTestBar", MarkdownUtil.removeMarkdown("Foo***Test***Bar"));
        assertEquals("FooTest*Bar", MarkdownUtil.removeMarkdown("Foo*Test**Bar"));
        assertEquals("Foo*TestBar", MarkdownUtil.removeMarkdown("Foo***Test**Bar"));
        assertEquals("FooTestBar", MarkdownUtil.removeMarkdown("Foo_Test_Bar"));
        assertEquals("FooTestBar", MarkdownUtil.removeMarkdown("Foo__Test__Bar"));
        assertEquals("FooTestBar", MarkdownUtil.removeMarkdown("Foo___Test___Bar"));
        assertEquals("Foo\nHeader\nBar", MarkdownUtil.removeMarkdown("Foo\n# Header\nBar"));
        assertEquals("Foo\nHeader\nBar", MarkdownUtil.removeMarkdown("Foo\n### Header\nBar"));
        assertEquals("Foo\nHeader\nBar", MarkdownUtil.removeMarkdown("Foo\n# Header #\nBar"));
        assertEquals("Foo\nHeader\nBar", MarkdownUtil.removeMarkdown("Foo\n## Header ####\nBar"));
        assertEquals("Foo\nNo Header #\nBar", MarkdownUtil.removeMarkdown("Foo\nNo Header #\nBar"));
        assertEquals("Foo\nHeader\nBar", MarkdownUtil.removeMarkdown("Foo\nHeader\n=\nBar"));
        assertEquals("Foo\nHeader\nBar", MarkdownUtil.removeMarkdown("Foo\nHeader\n-----\nBar"));
        assertEquals("Foo\nHeader\n--=--\nBar", MarkdownUtil.removeMarkdown("Foo\nHeader\n--=--\nBar"));
        assertEquals("Foo\nAufzählung\nBar", MarkdownUtil.removeMarkdown("Foo\n* Aufzählung\nBar"));
        assertEquals("Foo\nAufzählung\nBar", MarkdownUtil.removeMarkdown("Foo\n+ Aufzählung\nBar"));
        assertEquals("Foo\nAufzählung\nBar", MarkdownUtil.removeMarkdown("Foo\n- Aufzählung\nBar"));
        assertEquals("Foo\nAufzählung\nBar", MarkdownUtil.removeMarkdown("Foo\n    - Aufzählung\nBar"));
        assertEquals("Foo\nAufzählung *\nBar", MarkdownUtil.removeMarkdown("Foo\n* Aufzählung *\nBar"));
        assertEquals("Title", MarkdownUtil.removeMarkdown("# Title"));
        assertEquals("Aufzählung", MarkdownUtil.removeMarkdown("* Aufzählung"));
//        assertEquals("Foo Link Bar", MarkdownUtil.removeMarkdown("Foo [Link](https://example.com) Bar"));
    }
}