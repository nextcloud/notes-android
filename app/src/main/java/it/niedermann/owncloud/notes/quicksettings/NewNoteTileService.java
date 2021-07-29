package it.niedermann.owncloud.notes.quicksettings;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import it.niedermann.owncloud.notes.edit.EditNoteActivity;

/**
 * This {@link TileService} adds a quick settings tile that leads to the new note view.
 */
@TargetApi(Build.VERSION_CODES.N)
public class NewNoteTileService extends TileService {

    @Override
    public void onStartListening() {
        Tile tile = getQsTile();
        tile.setState(Tile.STATE_ACTIVE);

        tile.updateTile();
    }

    @Override
    public void onClick() {
        // create new note intent
        final Intent newNoteIntent = new Intent(getApplicationContext(), EditNoteActivity.class);
        // ensure it won't open twice if already running
        newNoteIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        // ask to unlock the screen if locked, then start new note intent
        unlockAndRun(() -> startActivityAndCollapse(newNoteIntent));
    }
}
