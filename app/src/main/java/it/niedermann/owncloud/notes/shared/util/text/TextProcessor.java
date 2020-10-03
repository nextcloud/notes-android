package it.niedermann.owncloud.notes.shared.util.text;

abstract public class TextProcessor {
    /**
     * Applies a specified transformation on a text string and returns the updated string.
     * @param s Text to transform
     * @return Transformed text
     */
    abstract public String process(String s);
}