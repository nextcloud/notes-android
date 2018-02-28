package it.niedermann.owncloud.notes.android.appwidget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class NoteListWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new NoteListWidgetFactory(this.getApplicationContext(), intent);
    }
}
