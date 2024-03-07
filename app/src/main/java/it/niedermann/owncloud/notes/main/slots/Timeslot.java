/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.slots;

import java.util.Calendar;

public class Timeslot {
    private final String label;
    private final Calendar time;

    Timeslot(String label, int month, int day) {
        this.label = label;
        this.time = Calendar.getInstance();
        this.time.set(this.time.get(Calendar.YEAR), month, day, 0, 0, 0);
    }

    public String getLabel() {
        return label;
    }

    public Calendar getTime() {
        return time;
    }
}