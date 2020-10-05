package it.niedermann.owncloud.notes.shared.util.text;

import java.util.regex.Pattern;

public class WwwLinksProcessor extends TextProcessor {

    private static final String WWW_URLS_PROTOCOL_PREFIX = "https://";
    private static final String REGEX_REPLACE_WWW_URLS = "\\[([^]]*)]\\((www\\..+)\\)";

    /**
     * Prefixes all links, that not not start with a protocol identifier, but with "www." with http://
     * <p>
     * See https://github.com/stefan-niedermann/nextcloud-notes/issues/949
     *
     * @return Markdown with all pseudo-links replaced through actual HTTP-links
     */
    @Override
    public String process(String s) {
        return Pattern
                .compile(REGEX_REPLACE_WWW_URLS)
                .matcher(s)
                .replaceAll(String.format("[$1](%s$2)", WWW_URLS_PROTOCOL_PREFIX));
    }
}
