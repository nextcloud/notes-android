package it.niedermann.owncloud.notes.persistence;

import android.content.Intent;
import android.widget.RemoteViewsService;

import it.niedermann.owncloud.notes.model.NoteListWidgetFactory;

/**
 * Created by dan0xii on 13/09/2017.
 */

public class NoteListWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new NoteListWidgetFactory(this.getApplicationContext(), intent);
    }
}
