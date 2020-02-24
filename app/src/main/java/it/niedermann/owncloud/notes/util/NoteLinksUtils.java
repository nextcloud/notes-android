package it.niedermann.owncloud.notes.util;

import androidx.annotation.VisibleForTesting;

import java.util.HashSet;
import java.util.Objects;
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

        String noteRemoteIdsCondition = join("|", noteRemoteIdsToReplace);
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

    /**
     * Returns a new {@code String} composed of copies of the
     * {@code CharSequence elements} joined together with a copy of the
     * specified {@code delimiter}.
     *
     * <blockquote>For example,
     * <pre>{@code
     *     List<String> strings = new LinkedList<>();
     *     strings.add("Java");strings.add("is");
     *     strings.add("cool");
     *     String message = String.join(" ", strings);
     *     //message returned is: "Java is cool"
     *
     *     Set<String> strings = new LinkedHashSet<>();
     *     strings.add("Java"); strings.add("is");
     *     strings.add("very"); strings.add("cool");
     *     String message = String.join("-", strings);
     *     //message returned is: "Java-is-very-cool"
     * }</pre></blockquote>
     * <p>
     * Note that if an individual element is {@code null}, then {@code "null"} is added.
     *
     * @param delimiter a sequence of characters that is used to separate each
     *                  of the {@code elements} in the resulting {@code String}
     * @param elements  an {@code Iterable} that will have its {@code elements}
     *                  joined together.
     * @return a new {@code String} that is composed from the {@code elements}
     * argument
     * @throws NullPointerException If {@code delimiter} or {@code elements}
     *                              is {@code null}
     * @see String#join(CharSequence, Iterable)
     */
    private static String join(CharSequence delimiter, Iterable<String> elements) {
        Objects.requireNonNull(delimiter);
        Objects.requireNonNull(elements);

        StringBuilder builder = new StringBuilder();
        for (String item : elements) {
            builder.append(item);
            builder.append(delimiter);
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }
}
