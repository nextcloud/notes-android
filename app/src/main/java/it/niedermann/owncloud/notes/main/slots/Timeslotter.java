/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.slots;

import android.content.Context;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.persistence.entity.Note;

public class Timeslotter {
    private final List<Timeslot> timeslots = new ArrayList<>();
    private final Calendar lastYear;
    private final Context context;

    public Timeslotter(@NonNull Context context) {
        this.context = context;
        Calendar now = Calendar.getInstance();
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);
        int offsetWeekStart = (now.get(Calendar.DAY_OF_WEEK) - now.getFirstDayOfWeek() + 7) % 7;
        timeslots.add(new Timeslot(context.getResources().getString(R.string.listview_updated_today), month, day));
        timeslots.add(new Timeslot(context.getResources().getString(R.string.listview_updated_yesterday), month, day - 1));
        timeslots.add(new Timeslot(context.getResources().getString(R.string.listview_updated_this_week), month, day - offsetWeekStart));
        timeslots.add(new Timeslot(context.getResources().getString(R.string.listview_updated_last_week), month, day - offsetWeekStart - 7));
        timeslots.add(new Timeslot(context.getResources().getString(R.string.listview_updated_this_month), month, 1));
        timeslots.add(new Timeslot(context.getResources().getString(R.string.listview_updated_last_month), month - 1, 1));
        lastYear = Calendar.getInstance();
        lastYear.set(now.get(Calendar.YEAR) - 1, 0, 1, 0, 0, 0);
    }

    public String getTimeslot(Note note) {
        if (note.getFavorite()) {
            return "";
        }
        final var modified = note.getModified();
        for (final var timeslot : timeslots) {
            if (!modified.before(timeslot.getTime())) {
                return timeslot.getLabel();
            }
        }
        if (!modified.before(this.lastYear)) {
            // use YEAR and MONTH in a format based on current locale
            return DateUtils.formatDateTime(context, modified.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NO_MONTH_DAY);
        } else {
            return Integer.toString(modified.get(Calendar.YEAR));
        }
    }
}