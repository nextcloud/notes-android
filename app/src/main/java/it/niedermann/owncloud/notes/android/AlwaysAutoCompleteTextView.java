package it.niedermann.owncloud.notes.android;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.WindowManager;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

/**
 * Extension of the {@link AppCompatAutoCompleteTextView}, but this one is always open, i.e. you can see the list of suggestions even the TextView is empty.
 */
public class AlwaysAutoCompleteTextView extends AppCompatAutoCompleteTextView {

    private static final String TAG = AlwaysAutoCompleteTextView.class.getSimpleName();

    private int myThreshold;

    public AlwaysAutoCompleteTextView(Context context) {
        super(context);
    }

    public AlwaysAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AlwaysAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setThreshold(int threshold) {
        myThreshold = Math.max(threshold, 0);
    }

    @Override
    public boolean enoughToFilter() {
        return getText().length() >= myThreshold;
    }

    @Override
    public int getThreshold() {
        return myThreshold;
    }

    public void showFullDropDown() {
        try {
            performFiltering(getText(), 0);
            showDropDown();
        } catch (WindowManager.BadTokenException e) {
            // https://github.com/stefan-niedermann/nextcloud-notes/issues/366
            e.printStackTrace();
            Log.e(TAG, "Exception", e);
        }
    }
}