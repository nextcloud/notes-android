package it.niedermann.owncloud.notes.edit;

import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.regex.Pattern;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.shared.util.ExtendedFabUtil;

public abstract class SearchableBaseNoteFragment extends BaseNoteFragment {

    private static final String TAG = SearchableBaseNoteFragment.class.getSimpleName();
    private static final String saved_instance_key_searchQuery = "searchQuery";
    private static final String saved_instance_key_currentOccurrence = "currentOccurrence";

    private int currentOccurrence = 1;
    private int occurrenceCount = 0;
    private SearchView searchView;
    private String searchQuery = null;
    private static final int delay = 50; // If the search string does not change after $delay ms, then the search task starts.

    @ColorInt
    private int color;

    @Override
    public void onStart() {
        this.color = getResources().getColor(R.color.defaultBrand);
        super.onStart();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(saved_instance_key_searchQuery, "");
            currentOccurrence = savedInstanceState.getInt(saved_instance_key_currentOccurrence, 1);
        }
    }

    @Override
    protected void onScroll(int scrollY, int oldScrollY) {
        super.onScroll(scrollY, oldScrollY);
        if (getSearchNextButton() == null || getSearchNextButton().getVisibility() != View.VISIBLE) {
            final ExtendedFloatingActionButton directFab = getDirectEditingButton();
            if (oldScrollY > 0 && scrollY > oldScrollY && directFab.isShown()) {
                ExtendedFabUtil.setExtendedFabVisibility(getDirectEditingButton(), false);
            } else if (scrollY < oldScrollY && !directFab.isShown()) {
                ExtendedFabUtil.setExtendedFabVisibility(getDirectEditingButton(), true);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO don't show fab if direct editing not available
        final ExtendedFloatingActionButton directEditingButton = getDirectEditingButton();
        directEditingButton.setExtended(false);
        directEditingButton.setOnLongClickListener(v -> {
            if (directEditingButton.isExtended()) {
                directEditingButton.shrink();
            } else {
                directEditingButton.extend();
            }
            return true;
        });
        directEditingButton.setOnClickListener(v -> {
            listener.changeMode(NoteFragmentListener.Mode.DIRECT_EDIT);
        });
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        final var searchMenuItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchMenuItem.getActionView();

        if (!TextUtils.isEmpty(searchQuery) && isNew) {
            searchMenuItem.expandActionView();
            searchView.setQuery(searchQuery, true);
            searchView.clearFocus();
        }

        searchMenuItem.collapseActionView();

        final var searchEditFrame = searchView.findViewById(R.id
                .search_edit_frame);

        searchEditFrame.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            int oldVisibility = -1;

            @Override
            public void onGlobalLayout() {
                final int currentVisibility = searchEditFrame.getVisibility();

                if (currentVisibility != oldVisibility) {
                    if (currentVisibility != View.VISIBLE) {
                        colorWithText("", null, color);
                        searchQuery = "";
                        hideSearchFabs();
                    } else {
                        jumpToOccurrence();
                        colorWithText(searchQuery, null, color);
                        occurrenceCount = countOccurrences(getContent(), searchQuery);
                        showSearchFabs();
                    }

                    oldVisibility = currentVisibility;
                }
            }

        });

        final var next = getSearchNextButton();
        final var prev = getSearchPrevButton();

        if (next != null) {
            next.setOnClickListener(v -> {
                currentOccurrence++;
                jumpToOccurrence();
                colorWithText(searchView.getQuery().toString(), currentOccurrence, color);
            });
        }

        if (prev != null) {
            prev.setOnClickListener(v -> {
                occurrenceCount = countOccurrences(getContent(), searchView.getQuery().toString());
                currentOccurrence--;
                jumpToOccurrence();
                colorWithText(searchView.getQuery().toString(), currentOccurrence, color);
            });
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            private DelayQueryRunnable delayQueryTask;
            private final Handler handler = new Handler();

            @Override
            public boolean onQueryTextSubmit(@NonNull String query) {
                currentOccurrence++;
                jumpToOccurrence();
                colorWithText(query, currentOccurrence, color);
                return true;
            }

            @Override
            public boolean onQueryTextChange(@NonNull String newText) {
                queryWithHandler(newText);
                return true;
            }

            private void queryMatch(@NonNull String newText) {
                searchQuery = newText;
                occurrenceCount = countOccurrences(getContent(), searchQuery);
                if (occurrenceCount > 1) {
                    showSearchFabs();
                } else {
                    hideSearchFabs();
                }
                currentOccurrence = 1;
                jumpToOccurrence();
                colorWithText(searchQuery, currentOccurrence, color);
            }

            private void queryWithHandler(@NonNull String newText) {
                if (delayQueryTask != null) {
                    delayQueryTask.cancel();
                    handler.removeCallbacksAndMessages(null);
                }
                delayQueryTask = new DelayQueryRunnable(newText);
                // If there is only one char in the search pattern, we should start the search immediately.
                handler.postDelayed(delayQueryTask, newText.length() > 1 ? delay : 0);
            }

            class DelayQueryRunnable implements Runnable {
                private String text;
                private boolean canceled = false;

                public DelayQueryRunnable(String text) {
                    this.text = text;
                }

                @Override
                public void run() {
                    if (canceled) {
                        return;
                    }
                    queryMatch(text);
                }

                public void cancel() {
                    canceled = true;
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (searchView != null && !TextUtils.isEmpty(searchView.getQuery().toString())) {
            outState.putString(saved_instance_key_searchQuery, searchView.getQuery().toString());
            outState.putInt(saved_instance_key_currentOccurrence, currentOccurrence);
        }
    }

    protected abstract void colorWithText(@NonNull String newText, @Nullable Integer current, @ColorInt int color);

    protected abstract Layout getLayout();

    protected abstract FloatingActionButton getSearchNextButton();

    protected abstract FloatingActionButton getSearchPrevButton();

    @NonNull
    protected abstract ExtendedFloatingActionButton getDirectEditingButton();


    private void showSearchFabs() {
        ExtendedFabUtil.setExtendedFabVisibility(getDirectEditingButton(), false);
        final var next = getSearchNextButton();
        final var prev = getSearchPrevButton();
        if (prev != null) {
            prev.show();
        }
        if (next != null) {
            next.show();
        }
    }

    private void hideSearchFabs() {
        final var next = getSearchNextButton();
        final var prev = getSearchPrevButton();
        if (prev != null) {
            prev.hide();
        }
        if (next != null) {
            next.hide();
        }
    }

    private void jumpToOccurrence() {
        final var layout = getLayout();
        if (layout == null) {
            Log.w(TAG, "getLayout() is null");
        } else if (getContent() == null || getContent().isEmpty()) {
            Log.w(TAG, "getContent is null or empty");
        } else if (currentOccurrence < 1) {
            // if currentOccurrence is lower than 1, jump to last occurrence
            currentOccurrence = occurrenceCount;
            jumpToOccurrence();
        } else if (searchQuery != null && !searchQuery.isEmpty()) {
            final String currentContent = getContent().toLowerCase();
            final int indexOfNewText = indexOfNth(currentContent, searchQuery.toLowerCase(), 0, currentOccurrence);
            if (indexOfNewText <= 0) {
                // Search term is not n times in text
                // Go back to first search result
                if (currentOccurrence != 1) {
                    currentOccurrence = 1;
                    jumpToOccurrence();
                }
                return;
            }
            final String textUntilFirstOccurrence = currentContent.substring(0, indexOfNewText);
            final int numberLine = layout.getLineForOffset(textUntilFirstOccurrence.length());

            if (numberLine >= 0) {
                final var scrollView = getScrollView();
                if (scrollView != null) {
                    scrollView.post(() -> scrollView.smoothScrollTo(0, layout.getLineTop(numberLine)));
                }
            }
        }
    }

    private static int indexOfNth(String input, String value, int startIndex, int nth) {
        if (nth < 1)
            throw new IllegalArgumentException("Param 'nth' must be greater than 0!");
        if (nth == 1)
            return input.indexOf(value, startIndex);
        final int idx = input.indexOf(value, startIndex);
        if (idx == -1)
            return -1;
        return indexOfNth(input, value, idx + 1, nth - 1);
    }

    private static int countOccurrences(String haystack, String needle) {
        if (haystack == null || haystack.isEmpty() || needle == null || needle.isEmpty()) {
            return 0;
        }
        // Use regrex which is faster before.
        // Such that the main thread will not stop for a long tilme
        // And so there will not an ANR problem
        final var matcher = Pattern.compile(needle, Pattern.CASE_INSENSITIVE | Pattern.LITERAL)
                .matcher(haystack);

        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    @CallSuper
    @Override
    public void applyBrand(int color) {
        this.color = color;

        final var util = BrandingUtil.of(color, requireContext());
        util.material.themeFAB(getSearchNextButton());
        util.material.themeFAB(getSearchPrevButton());
        util.material.themeExtendedFAB(getDirectEditingButton());
    }
}
