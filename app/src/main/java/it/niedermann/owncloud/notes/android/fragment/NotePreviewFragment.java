package it.niedermann.owncloud.notes.android.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.SearchView;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yydcdut.rxmarkdown.RxMDTextView;
import com.yydcdut.rxmarkdown.RxMarkdown;
import com.yydcdut.rxmarkdown.syntax.text.TextFactory;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.util.DisplayUtils;
import it.niedermann.owncloud.notes.util.MarkDownUtil;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class NotePreviewFragment extends BaseNoteFragment {

    @BindView(R.id.single_note_content)
    RxMDTextView noteContent;

    private String searchQuery = null;
    private SearchView searchView;

    private static boolean isNewFragment = false;
    public static NotePreviewFragment newInstance(long noteId) {
        NotePreviewFragment f = new NotePreviewFragment();
        Bundle b = new Bundle();
        b.putLong(PARAM_NOTE_ID, noteId);
        isNewFragment = true;
        f.setArguments(b);
        return f;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_edit).setVisible(true);
        menu.findItem(R.id.menu_preview).setVisible(false);
        MenuItem searchMenuItem = menu.findItem(R.id.search);

        searchView = (android.support.v7.widget.SearchView) searchMenuItem.getActionView();

        if (!TextUtils.isEmpty(searchQuery)) {
            if (!isNewFragment) {
                searchMenuItem.expandActionView();
            } else {
                searchMenuItem.collapseActionView();
            }
            searchView.setQuery(searchQuery, true);
            searchView.clearFocus();
        } else {
            searchMenuItem.collapseActionView();
        }

        final LinearLayout searchEditFrame = searchView.findViewById(android.support.v7.appcompat.R.id
                .search_edit_frame);

        searchEditFrame.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            int oldVisibility = -1;
            @Override
            public void onGlobalLayout() {
                int currentVisibility = searchEditFrame.getVisibility();

                if (currentVisibility != oldVisibility) {
                    if (currentVisibility != View.VISIBLE) {
                        colorWithText("");
                        searchQuery = "";
                    }

                    oldVisibility = currentVisibility;
                }
            }

        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuery = newText;
                colorWithText(newText);
                return true;
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_single_note, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ButterKnife.bind(this, getView());

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString("searchQuery", "");
        }

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
        noteContent.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected String getContent() {
        return note.getContent();
    }

    private void colorWithText(String newText) {
        if (ViewCompat.isAttachedToWindow(noteContent)) {
            noteContent.setText(DisplayUtils.searchAndColor(noteContent.getText().toString(), new SpannableString
                            (noteContent.getText()), newText, getResources().getColor(R.color.primary)),
                    TextView.BufferType.SPANNABLE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (searchView != null && !TextUtils.isEmpty(searchView.getQuery().toString())) {
            outState.putString("searchQuery", searchView.getQuery().toString());
        }
    }

}
