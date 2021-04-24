package it.niedermann.android.markdown;

import org.junit.Test;

import static org.junit.Assert.assertEquals;



// CS304 issue link: https://github.com/stefan-niedermann/nextcloud-notes/issues/1104
public class MarkdownUnitTest {
    @Test
    public void photoTest() {
        String md = "![photo](http://127.0.0.1:8090/upload/2021/04/image)";
        assertEquals("\n",MarkdownUtil.removeMarkdown(md));
    }

    @Test
    public void titleTest() {
        String md = "# title";
        assertEquals("title\n",MarkdownUtil.removeMarkdown(md));
    }

    @Test
    public void spcialFontTest1() {
        String md = "~~**test**~~  ";
        assertEquals("test\n",MarkdownUtil.removeMarkdown(md));
    }

    @Test
    public void spcialSymbolTest2() {
        String md = "test - title";
        assertEquals("test - title\n",MarkdownUtil.removeMarkdown(md));
    }
}