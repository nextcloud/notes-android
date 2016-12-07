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
    private ActionBar actionBar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_note);
        note = (DBNote) getIntent().getSerializableExtra(PARAM_NOTE);
        if (savedInstanceState != null) {
            note = (DBNote) savedInstanceState.getSerializable(PARAM_NOTE);
        }
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(note.getTitle());
            actionBar.setSubtitle(DateUtils.getRelativeDateTimeString(getApplicationContext(), note.getModified().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0));
        }
        noteContent = (RxMDTextView) findViewById(R.id.single_note_content);

        RxMarkdown.with(note.getContent(), this)
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
                        Log.v("Note View -------------", e.getStackTrace().toString());
                    }

                    @Override
                    public void onNext(CharSequence charSequence) {
                        noteContent.setText(charSequence, TextView.BufferType.SPANNABLE);
                    }
                });
        noteContent.setText(note.getContent());
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