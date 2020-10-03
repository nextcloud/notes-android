package it.niedermann.owncloud.notes.shared.util.text;

import java.util.LinkedList;

public class TextProcessorChain extends LinkedList<TextProcessor> {
    public String apply(String s) {
        for (TextProcessor textProcessor : this) {
            s = textProcessor.process(s);
        }
        return s;
    }
}
