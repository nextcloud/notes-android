/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.navigation;

/**
 * Describes whether a navigation item representing a category can be expanded to reveal its
 * sub categories, and if so whether it is currently expanded.
 */
public enum CategoryExpandState {
    NOT_EXPANDABLE,
    COLLAPSED,
    EXPANDED
}
