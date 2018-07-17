package it.niedermann.owncloud.notes.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.activity.EditNoteActivity;
import it.niedermann.owncloud.notes.android.activity.SplashscreenActivity;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;

/**
 * Creates a notification with the note title and note content.
 */
public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int noteId = intent.getIntExtra(EditNoteActivity.PARAM_NOTE_ID, 0);
        DBNote note = NoteSQLiteOpenHelper.getInstance(context).getNote(noteId);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, SplashscreenActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_splashscreen)
                .setContentTitle(note.getTitle())
                .setContentText(note.getExcerpt())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(noteId, builder.build());
    }
}
