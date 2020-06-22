package it.niedermann.owncloud.notes.shared.util;

import junit.framework.TestCase;

import org.junit.Assert;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static it.niedermann.owncloud.notes.shared.util.NoteLinksUtils.RELATIVE_LINK_WORKAROUND_PREFIX;


public class NoteLinksUtilsTest extends TestCase {

    public void testEmptyString() {
        String markdown = "";
        String result = NoteLinksUtils.replaceNoteLinksWithDummyUrls(markdown, Collections.emptySet());
        Assert.assertEquals("", result);
    }

    public void testDoNotChangeOtherMarkdownElements() {
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

        Assert.assertEquals(markdown, NoteLinksUtils.replaceNoteLinksWithDummyUrls(markdown, Collections.emptySet()));
    }

    @SuppressWarnings("MarkdownUnresolvedFileReference")
    public void testDoNotReplaceNormalLinks() {
        //language=md
        String markdown = "[normal link](https://example.com) and another [note link](123456)";
        String result = NoteLinksUtils.replaceNoteLinksWithDummyUrls(markdown, Collections.singleton("123456"));
        Assert.assertEquals(String.format("[normal link](https://example.com) and another [note link](%s123456)", RELATIVE_LINK_WORKAROUND_PREFIX), result);
    }

    public void testReplaceOnlyNotesInDB() {
        Set<String> remoteIdsOfExistingNotes = new HashSet<>();
        remoteIdsOfExistingNotes.add("123456");
        remoteIdsOfExistingNotes.add("321456");
        String markdown = "[link to real note](123456) and another [link to non-existing note](654321) and one more [another link to real note](321456)";

        String result = NoteLinksUtils.replaceNoteLinksWithDummyUrls(markdown, remoteIdsOfExistingNotes);

        String expected = String.format("[link to real note](%s123456) and another [link to non-existing note](654321) and one more [another link to real note](%s321456)", RELATIVE_LINK_WORKAROUND_PREFIX, RELATIVE_LINK_WORKAROUND_PREFIX);
        Assert.assertEquals(
                expected,
                result);
    }
}