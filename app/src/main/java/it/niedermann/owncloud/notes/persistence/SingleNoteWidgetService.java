package it.niedermann.owncloud.notes.persistence;

import android.content.Intent;
import android.widget.RemoteViewsService;

import it.niedermann.owncloud.notes.model.SingleNoteWidgetFactory;

/**
 * Created by dan0xii on 06/09/17.
 *
 */

public class SingleNoteWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new SingleNoteWidgetFactory(this.getApplicationContext(), intent);
    }
}
