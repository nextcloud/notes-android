package it.niedermann.owncloud.notes.model;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.recyclerview.widget.RecyclerView;

public class SectionItemDecoration extends RecyclerView.ItemDecoration {

    @NonNull
    private final ItemAdapter adapter;
    private final int sectionLeft;
    private final int sectionTop;
    private final int sectionRight;
    private final int sectionBottom;

    public SectionItemDecoration(@NonNull ItemAdapter adapter, @Px int sectionLeft, @Px int sectionTop, @Px int sectionRight, @Px int sectionBottom) {
        this.adapter = adapter;
        this.sectionLeft = sectionLeft;
        this.sectionTop = sectionTop;
        this.sectionRight = sectionRight;
        this.sectionBottom = sectionBottom;
    }

    @CallSuper
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        final int position = parent.getChildAdapterPosition(view);
        if (adapter.getItemViewType(position) == ItemAdapter.TYPE_SECTION) {
            outRect.left = sectionLeft;
            outRect.top = sectionTop;
            outRect.right = sectionRight;
            outRect.bottom = sectionBottom;
        }
    }
}
