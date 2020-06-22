package it.niedermann.owncloud.notes.widget.singlenote;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class SingleNoteWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new SingleNoteWidgetFactory(this.getApplicationContext(), intent);
    }
}
