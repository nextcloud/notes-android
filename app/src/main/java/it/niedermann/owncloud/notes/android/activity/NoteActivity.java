package it.niedermann.owncloud.notes.android.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.yydcdut.rxmarkdown.RxMDTextView;
import com.yydcdut.rxmarkdown.RxMarkdown;
import com.yydcdut.rxmarkdown.factory.TextFactory;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.util.MarkDownUtil;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class NoteActivity extends AppCompatActivity {

    public static final String PARAM_NOTE = EditNoteActivity.PARAM_NOTE;

    private DBNote note = null;
    private RxMDTextView noteContent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_note);
        note = (DBNote) getIntent().getSerializableExtra(PARAM_NOTE);
        if (savedInstanceState != null) {
            note = (DBNote) savedInstanceState.getSerializable(PARAM_NOTE);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(note.getTitle());
            actionBar.setSubtitle(DateUtils.getRelativeDateTimeString(getApplicationContext(), note.getModified().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0));
        }
        noteContent = (RxMDTextView) findViewById(R.id.single_note_content);

        String content = note.getContent();
        /*
         * The following replaceAll adds links ()[] to all URLs that are not in an existing link.
         * This regular expression consists of three parts:
         * 1. (?<![(])
         *    negative look-behind: no opening bracket "(" directly before the URL
         *    This prevents replacement in target part of Markdown link: [](URL)
         * 2. (https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])
         *    URL pattern: matches all addresses beginning with http:// or https://
         * 3. (?![^\\[]*\\])
         *    negative look-ahead: no closing bracket "]" after the URL (otherwise there have to be an opening bracket "[" before)
         *    This prevents replacement in label part of Markdown link: [...URL...]()
         */
        content = content.replaceAll("(?<![(])(https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])(?![^\\[]*\\])", "[$1]($1)");

        RxMarkdown.with(content, this)
                .config(MarkDownUtil.getMarkDownConfiguration(getApplicationContext()))
                .factory(TextFactory.create())
                .intoObservable()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CharSequence>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v(getClass().getSimpleName(), "RxMarkdown error", e);
                    }

                    @Override
                    public void onNext(CharSequence charSequence) {
                        noteContent.setText(charSequence, TextView.BufferType.SPANNABLE);
                    }
                });
        noteContent.setText(content);
        findViewById(R.id.fab_edit).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.single_note_content)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(PARAM_NOTE, note);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}