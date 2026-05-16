/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AttachmentUrlUtilTest {

    @Test
    fun basicPathBuildsWebdavUrl() {
        val result = AttachmentUrlUtil.transformAttachmentPath(
            ".attachments.398123/charlie.jpg", "amal", "Notes", null
        )
        assertEquals(
            "/remote.php/dav/files/amal/Notes/.attachments.398123/charlie.jpg",
            result
        )
    }

    @Test
    fun categoryIsInsertedAsSubfolder() {
        val result = AttachmentUrlUtil.transformAttachmentPath(
            ".attachments.1/cat.png", "amal", "Notes", "Animals"
        )
        assertEquals(
            "/remote.php/dav/files/amal/Notes/Animals/.attachments.1/cat.png",
            result
        )
    }

    @Test
    fun emptyCategoryOmitsSubfolder() {
        val result = AttachmentUrlUtil.transformAttachmentPath(
            ".attachments.1/cat.png", "amal", "Notes", ""
        )
        assertEquals(
            "/remote.php/dav/files/amal/Notes/.attachments.1/cat.png",
            result
        )
    }

    @Test
    fun nestedCategoryPreservedVerbatim() {
        val result = AttachmentUrlUtil.transformAttachmentPath(
            ".attachments.1/cat.png", "amal", "Notes", "Animals/Cats"
        )
        assertEquals(
            "/remote.php/dav/files/amal/Notes/Animals/Cats/.attachments.1/cat.png",
            result
        )
    }

    @Test
    fun spacesInFilenamePreservedVerbatim() {
        val result = AttachmentUrlUtil.transformAttachmentPath(
            ".attachments.1/my photo.jpg", "amal", "Notes", null
        )
        assertEquals(
            "/remote.php/dav/files/amal/Notes/.attachments.1/my photo.jpg",
            result
        )
    }

    @Test
    fun nonAttachmentPathReturnedUnchanged() {
        val result = AttachmentUrlUtil.transformAttachmentPath(
            "https://example.com/img.png", "amal", "Notes", null
        )
        assertEquals("https://example.com/img.png", result)
    }

    @Test
    fun patternMatchesAttachmentImageMarkdown() {
        val matcher = AttachmentUrlUtil.ATTACHMENT_PATTERN.matcher(
            "text ![charlie.jpg](.attachments.398123/charlie.jpg) more"
        )
        assertTrue(matcher.find())
        assertEquals(".attachments.398123/charlie.jpg", matcher.group(2))
    }

    @Test
    fun patternIgnoresNonAttachmentImages() {
        val matcher = AttachmentUrlUtil.ATTACHMENT_PATTERN.matcher(
            "![alt](https://example.com/x.png)"
        )
        assertFalse(matcher.find())
    }
}
