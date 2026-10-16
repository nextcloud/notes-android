/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.versions.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.xml.sax.SAXException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FileVersionsParserTest {

    @Test
    fun parseSkipsCollectionAndReadsVersions() {
        val versions = FileVersionsParser.parse(FILE_ID, MULTISTATUS.byteInputStream())

        assertEquals(2, versions.size)
        with(versions[0]) {
            assertEquals(FILE_ID, localId)
            assertEquals(1_700_000_000_000L, modifiedTimestamp)
            assertEquals(42L, fileLength)
            assertEquals("text/markdown", mimeType)
        }
        assertEquals(1_690_000_000_000L, versions[1].modifiedTimestamp)
    }

    @Test
    fun parseCollectionOnlyReturnsEmptyList() {
        val firstVersion = "<d:response><d:href>/remote.php/dav/versions/alice/versions/$FILE_ID/1700000000"
        val collectionOnly = MULTISTATUS.substringBefore(firstVersion) + "</d:multistatus>"

        assertTrue(FileVersionsParser.parse(FILE_ID, collectionOnly.byteInputStream()).isEmpty())
    }

    @Test(expected = SAXException::class)
    fun parseEmptyBodyFails() {
        FileVersionsParser.parse(FILE_ID, "".byteInputStream())
    }

    companion object {
        private const val FILE_ID = 1234L
        private val MULTISTATUS = """
            <?xml version="1.0"?>
            <d:multistatus xmlns:d="DAV:" xmlns:s="http://sabredav.org/ns" xmlns:oc="http://owncloud.org/ns">
            <d:response><d:href>/remote.php/dav/versions/alice/versions/$FILE_ID/</d:href>
            <d:propstat><d:prop><d:resourcetype><d:collection/></d:resourcetype></d:prop>
            <d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response>
            <d:response><d:href>/remote.php/dav/versions/alice/versions/$FILE_ID/1700000000</d:href>
            <d:propstat><d:prop>
            <d:getlastmodified>Tue, 14 Nov 2023 22:13:20 GMT</d:getlastmodified>
            <d:getcontentlength>42</d:getcontentlength>
            <d:resourcetype/>
            <d:getcontenttype>text/markdown</d:getcontenttype>
            </d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response>
            <d:response><d:href>/remote.php/dav/versions/alice/versions/$FILE_ID/1690000000</d:href>
            <d:propstat><d:prop>
            <d:getlastmodified>Sat, 22 Jul 2023 04:26:40 GMT</d:getlastmodified>
            </d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response>
            </d:multistatus>
        """.trimIndent()
    }
}
