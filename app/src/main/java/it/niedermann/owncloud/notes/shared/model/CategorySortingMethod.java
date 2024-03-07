/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.model;

public enum CategorySortingMethod {
    SORT_MODIFIED_DESC(0, "MODIFIED DESC"),
    SORT_LEXICOGRAPHICAL_ASC(1, "TITLE COLLATE NOCASE ASC");

    private final int id;
    private final String title;  // sorting method OrderBy for SQL

    /**
     * Constructor
     * @param title given sorting method OrderBy
     */
    CategorySortingMethod(int id, String title) {
        this.id = id;
        this.title = title;
    }

    /**
     * Retrieve the sorting method id represented in database
     * @return the sorting method id for the enum item
     */
    public int getId() {
        return this.id;
    }

    /**
     * Retrieve the sorting method order for SQL
     * @return the sorting method order for the enum item
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Retrieve the corresponding enum value with given the index (ordinal)
     * @param id the id of the corresponding enum value stored in DB
     * @return the corresponding enum item with the index (ordinal)
     */
    public static CategorySortingMethod findById(int id) {
        for (final var csm : values()) {
            if (csm.getId() == id) {
                return csm;
            }
        }
        return SORT_MODIFIED_DESC;
    }
}
