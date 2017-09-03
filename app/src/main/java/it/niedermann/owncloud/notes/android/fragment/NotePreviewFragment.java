package it.niedermann.owncloud.notes.android.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.yydcdut.rxmarkdown.RxMDTextView;
import com.yydcdut.rxmarkdown.RxMarkdown;
import com.yydcdut.rxmarkdown.factory.TextFactory;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.activity.EditNoteActivity;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.util.MarkDownUtil;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class NotePreviewFragment extends Fragment implements NoteFragmentI {

    public static final String PARAM_NOTE = EditNoteActivity.PARAM_NOTE;

    private DBNote note = null;

    public static NotePreviewFragment newInstance(DBNote note) {
        NotePreviewFragment f = new NotePreviewFragment();
        Bundle b = new Bundle();
        b.putSerializable(PARAM_NOTE, note);
        f.setArguments(b);
        return f;
    }

    @Override
    public DBNote getNote() {
        return note;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem itemPreview = menu.findItem(R.id.menu_preview);
        itemPreview.setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_action_edit_24dp));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_single_note, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        note = (DBNote) getArguments().getSerializable(PARAM_NOTE);
        final RxMDTextView noteContent = (RxMDTextView) getActivity().findViewById(R.id.single_note_content);

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

        RxMarkdown.with(content, getActivity())
                .config(MarkDownUtil.getMarkDownConfiguration(getActivity().getApplicationContext()))
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
        ((TextView) getActivity().findViewById(R.id.single_note_content)).setMovementMethod(LinkMovementMethod.getInstance());
    }

}
