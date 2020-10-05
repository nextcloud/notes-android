package it.niedermann.owncloud.notes.shared.util.text;

import junit.framework.TestCase;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextProcessorChainTest extends TestCase {

    public void testApplyAllInOrder() {
        TextProcessorChain chain = new TextProcessorChain();
        chain.add(new SelfIdentifyingProcessor(1));
        chain.add(new SelfIdentifyingProcessor(2));

        Assert.assertEquals("SelfIdentifyingProcessor 1\nSelfIdentifyingProcessor 2", chain.apply(""));
    }

    static class SelfIdentifyingProcessor extends TextProcessor {
        private int id;

        public SelfIdentifyingProcessor(int id) {
            this.id = id;
        }

        @Override
        public String process(String s) {
            List<String> parts = new ArrayList<>(Arrays.asList(s.split("\n")));
            parts.add(String.format("%s %d", getClass().getSimpleName(), id));
            return String.join("\n", parts.toArray(new String[]{})).trim();
        }
    }
}