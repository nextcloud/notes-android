package it.niedermann.owncloud.notes.android.activity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.yydcdut.rxmarkdown.RxMDConfiguration;
import com.yydcdut.rxmarkdown.RxMDEditText;
import com.yydcdut.rxmarkdown.RxMDTextView;
import com.yydcdut.rxmarkdown.RxMarkdown;
import com.yydcdut.rxmarkdown.callback.OnLinkClickCallback;
import com.yydcdut.rxmarkdown.factory.EditFactory;
import com.yydcdut.rxmarkdown.factory.TextFactory;
import com.yydcdut.rxmarkdown.loader.DefaultLoader;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.util.MarkDownUtil;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import com.yydcdut.rxmarkdown.RxMDEditText;
import com.yydcdut.rxmarkdown.RxMarkdown;
import com.yydcdut.rxmarkdown.factory.EditFactory;


public class SingleNoteWidget extends AppWidgetProvider {

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_single_note);


/**        final RxMDTextView content = new RxMDTextView(context);

        RxMarkdown.with("test string for markdown", context.getApplicationContext())
                .config(MarkDownUtil.getMarkDownConfiguration(context))
                .factory(TextFactory.create())
         .intoObservable()
         .subscribeOn(Schedulers.computation())
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe(new Subscriber<CharSequence>() {
        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {
        Log.d("SingleNoteWidget", "onError: " + e);
        }

        @Override
        public void onNext(CharSequence charSequence) {
            content.setText(charSequence, TextView.BufferType.SPANNABLE);
        }
        });

        content.setText("## testestetstet");

        Log.d("SingleNoteWidget", "RxMD: " + content); */
        //views.setTextViewText(R.id.single_note_content, "## Markdown here");


        // Construct the RemoteViews object
        //        views.setTextViewText(0, "test");
//        Intent intent = new Intent(context, CreateNoteActivity.class);
  //      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    //    views.setOnClickPendingIntent(R.id.widget_create_note, pendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
}
