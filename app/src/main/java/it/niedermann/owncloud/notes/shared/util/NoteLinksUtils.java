package it.niedermann.owncloud.notes.shared.util;

import android.text.TextUtils;

import androidx.annotation.VisibleForTesting;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoteLinksUtils {

    @VisibleForTesting
    static final String RELATIVE_LINK_WORKAROUND_PREFIX = "https://nextcloudnotes/notes/";

    private static final String linksThatLookLikeNoteLinksRegEx = "\\[[^]]*]\\((\\d+)\\)";
    private static final String replaceNoteRemoteIdsRegEx = "\\[([^\\]]*)\\]\\((%s)\\)";

    /**
     * Replaces all links to other notes of the form `[<link-text>](<note-file-id>)`
     * in the markdown string with links to a dummy url.
     *
     * Why is this needed?
     *  See discussion in issue #623
     *
     * @return Markdown with all note-links replaced with dummy-url-links
     */
    public static String replaceNoteLinksWithDummyUrls(String markdown, Set<String> existingNoteRemoteIds) {
        Pattern noteLinkCandidates = Pattern.compile(linksThatLookLikeNoteLinksRegEx);
        Matcher matcher = noteLinkCandidates.matcher(markdown);

        Set<String> noteRemoteIdsToReplace = new HashSet<>();
        while (matcher.find()) {
            String presumedNoteId = matcher.group(1);
            if (existingNoteRemoteIds.contains(presumedNoteId)) {
                noteRemoteIdsToReplace.add(presumedNoteId);
            }
        }

        String noteRemoteIdsCondition = TextUtils.join("|", noteRemoteIdsToReplace);
        Pattern replacePattern = Pattern.compile(String.format(replaceNoteRemoteIdsRegEx, noteRemoteIdsCondition));
        Matcher replaceMatcher = replacePattern.matcher(markdown);
        return replaceMatcher.replaceAll(String.format("[$1](%s$2)", RELATIVE_LINK_WORKAROUND_PREFIX));
    }

    /**
     * Tests if the given link is a note-link (which was transformed in {@link #replaceNoteLinksWithDummyUrls}) or not
     *
     * @param link Link under test
     * @return true if the link is a note-link
     */
    public static boolean isNoteLink(String link) {
        return link.startsWith(RELATIVE_LINK_WORKAROUND_PREFIX);
    }

    /**
     * Extracts the remoteId back from links that were transformed in {@link #replaceNoteLinksWithDummyUrls}.
     *
     * @param link Link that was transformed in {@link #replaceNoteLinksWithDummyUrls}
     * @return the remoteId of the linked note
     */
    public static long extractNoteRemoteId(String link) {
        return Long.parseLong(link.replace(RELATIVE_LINK_WORKAROUND_PREFIX, ""));
    }
}
