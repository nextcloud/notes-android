package it.niedermann.android.markdown;

import androidx.core.text.HtmlCompat;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ListTagHandlerTest extends TestCase {

    //language=html
    private static final String SAMPLE_HTML_WIDGET_TEST = "<h1>Widget-Test</h1>" +
            "<p><a href=\"https://nextcloud.com\">Link</a></p>" +
            "<p><strong>bold</strong> <em>italic</em> <em><strong>itabold</strong></em> ~~strike~~</p>" +
            "<ol start=\"3\">" +
            "<li>Item" +
            "<ol>" +
            "<li>Subitem</li>" +
            "<li>Subitem" +
            "<ul>" +
            "<li>Subi</li>" +
            "<li>Subi</li>" +
            "</ul>" +
            "</li>" +
            "<li>Test</li>" +
            "</ol>" +
            "</li>" +
            "<li>Item</li>" +
            "<li>Item</li>" +
            "</ol>" +
            "<ul>" +
            "<li>Unordered</li>" +
            "<li>Unordered</li>" +
            "</ul>" +
            "<p>☐ Unchecked<br>☒ Checked</p>";

    //language=html
    private static final String SAMPLE_HTML_MARKDOWN_SYNTAX =
            "<h2>This is a header.</h2>" +
                    "<ol>" +
                    "<li>This is the first list item.</li>" +
                    "<li>This is the second list item.</li>" +
                    "</ol>" +
                    "<p>Here's some example code:</p>" +
                    "<pre><code>return shell_exec(&quot;echo $input | $markdown_script&quot;);" +
                    "</code></pre>" +
                    "</blockquote>" +
                    "<p>Any decent text editor should make email-style quoting easy. For<br>example, with BBEdit, you can make a selection and choose Increase<br>Quote Level from the Text menu.</p>" +
                    "<h3>Lists</h3>" +
                    "<p>Markdown supports ordered (numbered) and unordered (bulleted) lists.</p>" +
                    "<p>Unordered lists use asterisks, pluses, and hyphens -- interchangably<br>-- as list markers:</p>" +
                    "<ul>" +
                    "<li>Red</li>" +
                    "<li>Green</li>" +
                    "<li>Blue</li>" +
                    "</ul>" +
                    "<p>is equivalent to:</p>" +
                    "<ul>" +
                    "<li>Red</li>" +
                    "<li>Green</li>" +
                    "<li>Blue</li>" +
                    "</ul>" +
                    "<p>and:</p>" +
                    "<ul>" +
                    "<li>Red</li>" +
                    "<li>Green</li>" +
                    "<li>Blue</li>" +
                    "</ul>" +
                    "<p>Ordered lists use numbers followed by periods:</p>" +
                    "<ol>" +
                    "<li>Bird</li>" +
                    "<li>McHale</li>" +
                    "<li>Parish</li>" +
                    "</ol>" +
                    "<p>It's important to note that the actual numbers you use to mark the<br>list have no effect on the HTML output Markdown produces. The HTML<br>Markdown produces from the above list is:</p>" +
                    "<p>If you instead wrote the list in Markdown like this:</p>" +
                    "<ol>" +
                    "<li>Bird</li>" +
                    "<li>McHale</li>" +
                    "<li>Parish</li>" +
                    "</ol>" +
                    "<p>or even:</p>" +
                    "<ol start=\"3\">" +
                    "<li>Bird</li>" +
                    "<li>McHale</li>" +
                    "<li>Parish</li>" +
                    "</ol>";

    @Test
    public void testMarkOrderedListTags() {
        assertTrue(SAMPLE_HTML_WIDGET_TEST.contains("<ol start=\"3\">"));
        assertTrue(SAMPLE_HTML_WIDGET_TEST.contains("</ol>"));
        assertTrue(SAMPLE_HTML_WIDGET_TEST.contains("</ul>"));
        assertTrue(SAMPLE_HTML_WIDGET_TEST.contains("</ul>"));
        assertTrue(SAMPLE_HTML_WIDGET_TEST.contains("</li>"));
        assertTrue(SAMPLE_HTML_WIDGET_TEST.contains("</li>"));

        final String markedSampleHtml = ListTagHandler.prepareTagHandling(SAMPLE_HTML_WIDGET_TEST);

        assertFalse(markedSampleHtml.contains("<ol start=\"3\">"));
        assertFalse(markedSampleHtml.contains("</ol>"));
        assertFalse(markedSampleHtml.contains("</ul>"));
        assertFalse(markedSampleHtml.contains("</ul>"));
        assertFalse(markedSampleHtml.contains("</li>"));
        assertFalse(markedSampleHtml.contains("</li>"));
    }

    @Test
    public void testHandleTag() {
        final ListTagHandler handler = new ListTagHandler();

        assertEquals("\n• Item ", HtmlCompat.fromHtml(ListTagHandler.prepareTagHandling("<ul><li>Item</li></ul>"), 0, null, handler).toString());

        final String[] lines = HtmlCompat.fromHtml(ListTagHandler.prepareTagHandling(SAMPLE_HTML_WIDGET_TEST), 0, null, handler).toString().split("\n");

        assertEquals("Widget-Test", lines[0]);
        assertEquals("", lines[1]);
        assertEquals("Link", lines[2]);
        assertEquals("", lines[3]);
        assertEquals("bold italic itabold ~~strike~~", lines[4]);
        assertEquals("", lines[5]);
        assertEquals("", lines[6]);
        assertEquals("1. Item ", lines[7]);
        assertEquals("\t\t1. Subitem ", lines[8]);
        assertEquals("\t\t2. Subitem ", lines[9]);
        assertEquals("\t\t\t\t• Subi ", lines[10]);
        assertEquals("\t\t\t\t• Subi ", lines[11]);
        assertEquals("\t\t3. Test ", lines[12]);
        assertEquals("2. Item ", lines[13]);
        assertEquals("3. Item ", lines[14]);
        assertEquals("• Unordered ", lines[15]);
        assertEquals("• Unordered ", lines[16]);
        assertEquals("", lines[17]);
        assertEquals("☐ Unchecked", lines[18]);
        assertEquals("☒ Checked", lines[19]);

        HtmlCompat.fromHtml(ListTagHandler.prepareTagHandling(SAMPLE_HTML_MARKDOWN_SYNTAX), 0, null, handler);
    }
}