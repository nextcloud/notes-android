package it.niedermann.owncloud.notes.shared.util.text;

import android.text.TextUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.VisibleForTesting;

public class NoteLinksProcessor extends TextProcessor {

    public static final String RELATIVE_LINK_WORKAROUND_PREFIX = "https://nextcloudnotes/notes/";

    @VisibleForTesting
    private static final String linksThatLookLikeNoteLinksRegEx = "\\[[^]]*]\\((\\d+)\\)";
    private static final String replaceNoteRemoteIdsRegEx = "\\[([^\\]]*)\\]\\((%s)\\)";

    private final Set<String> existingNoteRemoteIds;

    public NoteLinksProcessor(Set<String> existingNoteRemoteIds) {
        this.existingNoteRemoteIds = existingNoteRemoteIds;
    }

    /**
     * Replaces all links to other notes of the form `[<link-text>](<note-file-id>)`
     * in the markdown string with links to a dummy url.
     *
     * Why is this needed?
     *  See discussion in issue #623
     *
     * @return Markdown with all note-links replaced with dummy-url-links
     */
    @Override
    public String process(String s) {
        return replaceNoteLinksWithDummyUrls(s, existingNoteRemoteIds);
    }

    private static String replaceNoteLinksWithDummyUrls(String markdown, Set<String> existingNoteRemoteIds) {
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
}
