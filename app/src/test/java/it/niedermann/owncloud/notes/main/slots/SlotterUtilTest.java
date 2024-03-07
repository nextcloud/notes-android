/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.slots;

import static org.junit.Assert.assertEquals;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Calendar;
import java.util.List;

import it.niedermann.owncloud.notes.main.items.section.SectionItem;
import it.niedermann.owncloud.notes.persistence.entity.Note;

@RunWith(RobolectricTestRunner.class)
public class SlotterUtilTest {

    @Test
    public void fillListByInitials_shouldAddSectionItems() {
        final var notes = List.of(
                new Note(1L, Calendar.getInstance(), "Aaa", "", "", false, ""),
                new Note(1L, Calendar.getInstance(), "Abc", "", "", false, ""),
                new Note(1L, Calendar.getInstance(), "Bbb", "", "", false, ""),
                new Note(1L, Calendar.getInstance(), "Bcd", "", "", false, ""),
                new Note(1L, Calendar.getInstance(), "Def", "", "", false, "")
        );

        final var items = SlotterUtil.fillListByInitials(ApplicationProvider.getApplicationContext(), notes);
        assertEquals(2, items.stream().filter(item -> item instanceof SectionItem).count());
        assertEquals(SectionItem.class, items.get(2).getClass());
        assertEquals(SectionItem.class, items.get(5).getClass());
    }

    @Test
    public void fillListByInitials_shouldAcceptEmptyTitles() {
        final var notes = List.of(
                new Note(1L, Calendar.getInstance(), "", "", "", false, ""),
                new Note(2L, Calendar.getInstance(), "Foo", "", "", false, ""),
                new Note(3L, Calendar.getInstance(), "Bar", "", "", false, "")
        );

        final var items = SlotterUtil.fillListByInitials(ApplicationProvider.getApplicationContext(), notes);
    }
}
