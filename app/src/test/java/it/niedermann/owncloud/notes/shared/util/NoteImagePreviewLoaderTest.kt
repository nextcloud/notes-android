/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteImagePreviewLoaderTest {
    @Test
    fun recognizesAttachmentImageMarkdown() {
        assertTrue(NoteImagePreviewLoader.hasImagePreview("![image.jpg](attachments/image.jpg)"))
    }

    @Test
    fun recognizesAttachmentPathOutsideMarkdown() {
        assertTrue(NoteImagePreviewLoader.hasImagePreview("Image: attachments/image.jpg"))
    }

    @Test
    fun ignoresExternalImageMarkdown() {
        assertFalse(NoteImagePreviewLoader.hasImagePreview("![image.jpg](https://example.com/image.jpg)"))
    }
}
