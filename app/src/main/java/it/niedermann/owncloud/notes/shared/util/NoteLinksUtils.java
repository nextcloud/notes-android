package it.niedermann.owncloud.notes.shared.util;

import it.niedermann.owncloud.notes.shared.util.text.NoteLinksProcessor;

public class NoteLinksUtils {

    /**
     * Tests if the given link is a note-link (which was transformed in {@link it.niedermann.owncloud.notes.shared.util.text.NoteLinksProcessor}) or not
     *
     * @param link Link under test
     * @return true if the link is a note-link
     */
    public static boolean isNoteLink(String link) {
        return link.startsWith(NoteLinksProcessor.RELATIVE_LINK_WORKAROUND_PREFIX);
    }

    /**
     * Extracts the remoteId back from links that were transformed in {@link it.niedermann.owncloud.notes.shared.util.text.NoteLinksProcessor}.
     *
     * @param link Link that was transformed in {@link it.niedermann.owncloud.notes.shared.util.text.NoteLinksProcessor}
     * @return the remoteId of the linked note
     */
    public static long extractNoteRemoteId(String link) {
        return Long.parseLong(link.replace(NoteLinksProcessor.RELATIVE_LINK_WORKAROUND_PREFIX, ""));
    }
}
