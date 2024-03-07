/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.quicksettings;

import android.content.Intent;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import it.niedermann.owncloud.notes.edit.EditNoteActivity;

/**
 * This {@link TileService} adds a quick settings tile that leads to the new note view.
 */
public class NewNoteTileService extends TileService {

    @Override
    public void onStartListening() {
        final var tile = getQsTile();
        tile.setState(Tile.STATE_ACTIVE);
        tile.updateTile();
    }

    @Override
    public void onClick() {
        // create new note intent
        final var newNoteIntent = new Intent(getApplicationContext(), EditNoteActivity.class);
        // ensure it won't open twice if already running
        newNoteIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        // ask to unlock the screen if locked, then start new note intent
        unlockAndRun(() -> startActivityAndCollapse(newNoteIntent));
    }
}
