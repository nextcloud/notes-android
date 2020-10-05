package it.niedermann.owncloud.notes.shared.util.text;

import junit.framework.TestCase;

import org.junit.Assert;

public class WwwLinksProcessorTest extends TestCase {

    public void testEmptyString() {
        TextProcessor sut = new WwwLinksProcessor();

        String markdown = "";
        String result = sut.process(markdown);
        Assert.assertEquals("", result);
    }

    public void testDoNotChangeOtherMarkdownElements() {
        TextProcessor sut = new WwwLinksProcessor();

        //language=md
        String markdown = "\n" +
                "# heading  \n" +
                "  \n" +
                "This is a _markdown_ document.  \n" +
                "  \n" +
                "But\n" +
                " - there  \n" +
                " - are  \n" +
                " - no  \n" +
                "\n" +
                "link elements.\n" +
                "\n" +
                "----\n" +
                "**Everything** else could be in here.\n" +
                "\n";

        Assert.assertEquals(markdown, sut.process(markdown));
    }

    public void testDoNotReplaceNormalLinks() {
        TextProcessor sut = new WwwLinksProcessor();

        //language=md
        String markdown = "[normal link](https://example.com) and another [www link](www.example.com) and one more [normal link](https://www.example.com)";
        String result = sut.process(markdown);
        Assert.assertEquals("[normal link](https://example.com) and another [www link](https://www.example.com) and one more [normal link](https://www.example.com)", result);
    }
}