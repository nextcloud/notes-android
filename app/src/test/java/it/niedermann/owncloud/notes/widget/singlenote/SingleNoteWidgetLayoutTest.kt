/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.widget.singlenote

import android.content.Context
import android.view.LayoutInflater
import android.widget.ListView
import androidx.test.core.app.ApplicationProvider
import it.niedermann.owncloud.notes.R
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SingleNoteWidgetLayoutTest {

    @Test
    fun noteTextScrollsToWidgetEdge() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val widget = LayoutInflater.from(context).inflate(R.layout.widget_single_note, null)

        assertFalse(widget.findViewById<ListView>(R.id.single_note_widget_lv).clipToPadding)
        assertTrue(widget.clipToOutline)
    }
}
