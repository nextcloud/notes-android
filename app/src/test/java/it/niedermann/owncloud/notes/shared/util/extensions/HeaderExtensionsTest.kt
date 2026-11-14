/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util.extensions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HeaderExtensionsTest {

    @Test
    fun getHeaderMatchesExactCase() {
        assertEquals("\"abc\"", mapOf("ETag" to "\"abc\"").getHeader("ETag"))
    }

    @Test
    fun getHeaderMatchesLowercaseNameFromProxy() {
        val headers = mapOf("etag" to "\"abc\"", "last-modified" to "Mon, 30 Dec 2024 20:52:52 GMT")
        assertEquals("\"abc\"", headers.getHeader("ETag"))
        assertEquals("Mon, 30 Dec 2024 20:52:52 GMT", headers.getHeader("Last-Modified"))
    }

    @Test
    fun getHeaderReturnsNullWhenMissing() {
        assertNull(mapOf("Server" to "Apache").getHeader("ETag"))
    }
}
