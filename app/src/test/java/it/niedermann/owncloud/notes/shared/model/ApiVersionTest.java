/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ApiVersionTest {

    @Test
    public void shouldOnlyCompareMajorApiVersions() {
        final var apiVersion = new ApiVersion("1.0", 1, 0);

        assertEquals(1, apiVersion.compareTo(ApiVersion.API_VERSION_0_2));
        assertEquals(0, apiVersion.compareTo(ApiVersion.API_VERSION_1_0));
        assertEquals(0, apiVersion.compareTo(ApiVersion.API_VERSION_1_2));
    }

    @Test
    public void shouldOnlyEqualMajorApiVersions() {
        final var apiVersion = new ApiVersion("1.0", 1, 0);

        assertNotEquals(apiVersion, ApiVersion.API_VERSION_0_2);
        assertEquals(apiVersion, ApiVersion.API_VERSION_1_0);
        assertEquals(apiVersion, ApiVersion.API_VERSION_1_2);
    }

    @Test
    public void shouldSupportFileSuffixChangesWithApi1_3andAbove() {
        assertFalse(ApiVersion.API_VERSION_0_2.supportsFileSuffixChange());
        assertFalse(ApiVersion.API_VERSION_1_0.supportsFileSuffixChange());
        assertFalse(ApiVersion.API_VERSION_1_2.supportsFileSuffixChange());
        assertTrue(ApiVersion.API_VERSION_1_3.supportsFileSuffixChange());
    }

    @Test
    public void shouldSupportNotesPathChangesWithApi1_2andAbove() {
        assertFalse(ApiVersion.API_VERSION_0_2.supportsNotesPathChange());
        assertFalse(ApiVersion.API_VERSION_1_0.supportsNotesPathChange());
        assertTrue(ApiVersion.API_VERSION_1_2.supportsNotesPathChange());
        assertTrue(ApiVersion.API_VERSION_1_3.supportsNotesPathChange());
    }
}
