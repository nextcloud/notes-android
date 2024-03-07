/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import it.niedermann.owncloud.notes.shared.model.CategorySortingMethod;

public class NavigationCategorySortingMethodTest {

    @Test
    public void getId() {
        final var csm0 = CategorySortingMethod.SORT_MODIFIED_DESC;
        assertEquals(0, csm0.getId());
        final var csm1 = CategorySortingMethod.SORT_LEXICOGRAPHICAL_ASC;
        assertEquals(1, csm1.getId());
    }

    @Test
    public void getTitle() {
        final var csm0 = CategorySortingMethod.SORT_MODIFIED_DESC;
        assertEquals("MODIFIED DESC", csm0.getTitle());
        final var csm1 = CategorySortingMethod.SORT_LEXICOGRAPHICAL_ASC;
        assertEquals("TITLE COLLATE NOCASE ASC", csm1.getTitle());
    }

    @Test
    public void findById() {
        final var csm0 = CategorySortingMethod.SORT_MODIFIED_DESC;
        assertEquals(csm0, CategorySortingMethod.findById(0));
        final var csm1 = CategorySortingMethod.SORT_LEXICOGRAPHICAL_ASC;
        assertEquals(csm1, CategorySortingMethod.findById(1));
    }
}
