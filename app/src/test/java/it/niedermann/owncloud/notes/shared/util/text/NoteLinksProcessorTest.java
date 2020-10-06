package it.niedermann.owncloud.notes.shared.util.text;

import junit.framework.TestCase;

import org.junit.Assert;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static it.niedermann.owncloud.notes.shared.util.text.NoteLinksProcessor.RELATIVE_LINK_WORKAROUND_PREFIX;

public class NoteLinksProcessorTest extends TestCase {

    public void testEmptyString() {
        TextProcessor sut = new NoteLinksProcessor(Collections.emptySet());

        String markdown = "";
        String result = sut.process(markdown);
        Assert.assertEquals("", result);
    }

    public void testDoNotChangeOtherMarkdownElements() {
        TextProcessor sut = new NoteLinksProcessor(Collections.emptySet());

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

    @SuppressWarnings("MarkdownUnresolvedFileReference")
    public void testDoNotReplaceNormalLinks() {
        TextProcessor sut = new NoteLinksProcessor(Collections.singleton(123456L));

        //language=md
        String markdown = "[normal link](https://example.com) and another [note link](123456)";
        String result = sut.process(markdown);
        Assert.assertEquals(String.format("[normal link](https://example.com) and another [note link](%s123456)", RELATIVE_LINK_WORKAROUND_PREFIX), result);
    }

    public void testReplaceOnlyNotesInDB() {
        Set<Long> remoteIdsOfExistingNotes = new HashSet<>();
        remoteIdsOfExistingNotes.add(123456L);
        remoteIdsOfExistingNotes.add(321456L);

        TextProcessor sut = new NoteLinksProcessor(remoteIdsOfExistingNotes);

        String markdown = "[link to real note](123456) and another [link to non-existing note](654321) and one more [another link to real note](321456)";

        String result = sut.process(markdown);

        String expected = String.format("[link to real note](%s123456) and another [link to non-existing note](654321) and one more [another link to real note](%s321456)", RELATIVE_LINK_WORKAROUND_PREFIX, RELATIVE_LINK_WORKAROUND_PREFIX);
        Assert.assertEquals(
                expected,
                result);
    }
}