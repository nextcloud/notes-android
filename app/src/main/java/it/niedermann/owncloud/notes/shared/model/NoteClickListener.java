/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.model;

import android.view.View;

public interface NoteClickListener {
    void onNoteClick(int position, View v);

    void onNoteFavoriteClick(int position, View v);
}