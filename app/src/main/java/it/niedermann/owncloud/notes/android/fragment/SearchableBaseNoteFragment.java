package it.niedermann.owncloud.notes.android.fragment;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.ViewCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.util.DisplayUtils;

public abstract class SearchableBaseNoteFragment extends BaseNoteFragment {

    private TextView activeTextView;
    private int currentOccurrence = 1;
    private int occurrenceCount = 0;

    private SearchView searchView;

    private String searchQuery = null;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString("searchQuery", "");
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem searchMenuItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchMenuItem.getActionView();

        if (!TextUtils.isEmpty(searchQuery) && isNew) {
            searchMenuItem.expandActionView();
            searchView.setQuery(searchQuery, true);
            searchView.clearFocus();
        } else {
            searchMenuItem.collapseActionView();
        }


        final LinearLayout searchEditFrame = searchView.findViewById(R.id
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
                        hideSearchFabs();
                    } else {
                        showSearchFabs();
                    }

                    oldVisibility = currentVisibility;
                }
            }

        });

        FloatingActionButton next = getSearchNextButton();
        FloatingActionButton prev = getSearchPrevButton();

        if (next != null) {
            next.setOnClickListener(v -> {
                currentOccurrence++;
                jumpToOccurrence();
            });
        }

        if (prev != null) {
            prev.setOnClickListener(v -> {
                currentOccurrence--;
                jumpToOccurrence();
            });
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentOccurrence++;
                jumpToOccurrence();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuery = newText;
                colorWithText(newText);
                occurrenceCount = countOccurrences(getContent(), searchQuery);
                if (occurrenceCount > 1) {
                    showSearchFabs();
                } else {
                    hideSearchFabs();
                }
                currentOccurrence = 1;
                jumpToOccurrence();
                return true;
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (searchView != null && !TextUtils.isEmpty(searchView.getQuery().toString())) {
            outState.putString("searchQuery", searchView.getQuery().toString());
        }
    }

    void setActiveTextView(TextView textView) {
        activeTextView = textView;
    }

    private void colorWithText(String newText) {
        if (activeTextView != null && ViewCompat.isAttachedToWindow(activeTextView)) {
            activeTextView.setText(DisplayUtils.searchAndColor(activeTextView.getText().toString(), new SpannableString
                            (activeTextView.getText()), newText, getResources().getColor(R.color.primary)),
                    TextView.BufferType.SPANNABLE);
        }
    }

    private void showSearchFabs() {
        FloatingActionButton next = getSearchNextButton();
        FloatingActionButton prev = getSearchPrevButton();
        if (prev != null) {
            prev.show();
        }
        if (next != null) {
            next.show();
        }
    }

    private void hideSearchFabs() {
        FloatingActionButton next = getSearchNextButton();
        FloatingActionButton prev = getSearchPrevButton();
        if (prev != null) {
            prev.hide();
        }
        if (next != null) {
            next.hide();
        }
    }

    private void jumpToOccurrence() {
        if (searchQuery == null || searchQuery.isEmpty()) {
            // No search term
            return;
        }
        if (currentOccurrence < 1) {
            // if currentOccurrence is lower than 1, jump to last occurrence
            currentOccurrence = occurrenceCount;
            jumpToOccurrence();
            return;
        }
        String currentContent = getContent().toLowerCase();
        int indexOfNewText = indexOfNth(currentContent, searchQuery.toLowerCase(), 0, currentOccurrence);
        if (indexOfNewText <= 0) {
            // Search term is not n times in text
            // Go back to first search result
            if (currentOccurrence != 1) {
                currentOccurrence = 1;
                jumpToOccurrence();
            }
            return;
        }
        String textUntilFirstOccurrence = currentContent.substring(0, indexOfNewText);
        int numberLine = getLayout().getLineForOffset(textUntilFirstOccurrence.length());

        if (numberLine >= 0) {
            getScrollView().smoothScrollTo(0, getLayout().getLineTop(numberLine));
        }
    }

    private static int indexOfNth(String input, String value, int startIndex, int nth) {
        if (nth < 1)
            throw new IllegalArgumentException("Param 'nth' must be greater than 0!");
        if (nth == 1)
            return input.indexOf(value, startIndex);
        int idx = input.indexOf(value, startIndex);
        if (idx == -1)
            return -1;
        return indexOfNth(input, value, idx + 1, --nth);
    }

    private static int countOccurrences(String haystack, String needle) {
        if (haystack == null || haystack.isEmpty() || needle == null || needle.isEmpty()) {
            return 0;
        }
        haystack = haystack.toLowerCase();
        needle = needle.toLowerCase();
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {
            lastIndex = haystack.indexOf(needle, lastIndex);
            if (lastIndex != -1) {
                count++;
                lastIndex += needle.length();
            }
        }
        return count;
    }
}
