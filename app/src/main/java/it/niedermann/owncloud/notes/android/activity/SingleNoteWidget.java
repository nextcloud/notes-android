package it.niedermann.owncloud.notes.android.activity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.yydcdut.rxmarkdown.RxMDEditText;
import com.yydcdut.rxmarkdown.RxMDTextView;
import com.yydcdut.rxmarkdown.RxMarkdown;
import com.yydcdut.rxmarkdown.factory.EditFactory;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.util.MarkDownUtil;
import rx.Subscriber;

public class SingleNoteWidget extends AppWidgetProvider {
    private RxMDEditText editTextField = null;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_single_note);


//        Intent intent = new Intent(context, CreateNoteActivity.class);

  //      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    //    views.setOnClickPendingIntent(R.id.widget_create_note, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);



    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // final RxMDTextView textView = (RxMDTextView) findViewById(R.id.txt_md_show);

        RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget_single_note);

        view.setTextViewText(0, "test");

        editTextField.setText("test");
        RxMarkdown.live(editTextField)
                .config(MarkDownUtil.getMarkDownConfiguration(context))
                .factory(EditFactory.create())
                .intoObservable()
                .subscribe(new Subscriber<CharSequence>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(CharSequence charSequence) {
                        editTextField.setText(charSequence, TextView.BufferType.SPANNABLE);
                    }
                });


        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
}
