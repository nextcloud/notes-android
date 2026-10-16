/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.versions.repository

import android.util.Xml
import it.niedermann.owncloud.notes.versions.model.FileVersion
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.InputStream
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

object FileVersionsParser {

    private const val DAV_NAMESPACE = "DAV:"
    private const val RESPONSE = "response"
    private const val HREF = "href"
    private const val LAST_MODIFIED = "getlastmodified"
    private const val CONTENT_LENGTH = "getcontentlength"
    private const val CONTENT_TYPE = "getcontenttype"
    private val TEXT_PROPERTIES = setOf(HREF, LAST_MODIFIED, CONTENT_LENGTH, CONTENT_TYPE)

    fun parse(fileId: Long, input: InputStream): List<FileVersion> {
        val responses = readResponses(input)
        if (responses.isEmpty()) {
            throw XmlPullParserException("No DAV response found in multistatus")
        }
        return responses.drop(1).map { it.toFileVersion(fileId) }
    }

    private fun readResponses(input: InputStream): List<Map<String, String>> {
        val parser = Xml.newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
            setInput(input, null)
        }
        val responses = mutableListOf<MutableMap<String, String>>()
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG || parser.namespace != DAV_NAMESPACE) continue
            when (parser.name) {
                RESPONSE -> responses.add(mutableMapOf())
                in TEXT_PROPERTIES -> responses.lastOrNull()?.put(parser.name, parser.nextText())
            }
        }
        return responses
    }

    private fun Map<String, String>.toFileVersion(fileId: Long) = FileVersion(
        fileId = fileId,
        versionId = getValue(HREF).trimEnd('/').substringAfterLast('/'),
        modified = Date.from(
            ZonedDateTime.parse(getValue(LAST_MODIFIED), DateTimeFormatter.RFC_1123_DATE_TIME).toInstant()
        ),
        size = get(CONTENT_LENGTH)?.toLongOrNull(),
        mimeType = get(CONTENT_TYPE)
    )
}
