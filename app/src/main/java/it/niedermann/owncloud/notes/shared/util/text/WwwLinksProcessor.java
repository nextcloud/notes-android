package it.niedermann.owncloud.notes.shared.util.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WwwLinksProcessor extends TextProcessor {

    public static final String WWW_URLS_PROTOCOL_PREFIX = "http://";
    private static final String replaceWwwUrlsRegEx = "\\[([^]]*)]\\((www\\..+)\\)";

    /**
     * Prefixes all links, that not not start with a protocol identifier, but with "www." with http://
     *
     * See https://github.com/stefan-niedermann/nextcloud-notes/issues/949
     *
     * @return Markdown with all pseudo-links replaced through actual HTTP-links
     */
    @Override
    public String process(String s) {
        return replaceWwwUrls(s);
    }

    private static String replaceWwwUrls(String markdown) {
        Pattern replacePattern = Pattern.compile(replaceWwwUrlsRegEx);
        Matcher replaceMatcher = replacePattern.matcher(markdown);
        return replaceMatcher.replaceAll(String.format("[$1](%s$2)", WWW_URLS_PROTOCOL_PREFIX));
    }
}
