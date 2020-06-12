package it.niedermann.owncloud.notes.model;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class GridItemDecoration extends SectionItemDecoration {

    @NonNull
    private final ItemAdapter adapter;
    private final int spanCount;
    private final int gutter;

    public GridItemDecoration(@NonNull ItemAdapter adapter, int spanCount, @Px int sectionLeft, @Px int sectionTop, @Px int sectionRight, @Px int sectionBottom, @Px int gutter) {
        super(adapter, sectionLeft, sectionTop, sectionRight, sectionBottom);
        this.spanCount = spanCount;
        this.adapter = adapter;
        this.gutter = gutter;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        final int position = parent.getChildAdapterPosition(view);
        final StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();

        if (adapter.getItemViewType(position) == ItemAdapter.TYPE_SECTION) {
            lp.setFullSpan(true);
        } else {
            final int spanIndex = lp.getSpanIndex();

            if (position >= 0) {
                // First row gets some spacing at the top
                if (position < spanCount && position < adapter.getFirstPositionOfViewType(ItemAdapter.TYPE_SECTION)) {
                    outRect.top = gutter;
                }

                // First column gets some spacing at the left and the right side
                if (spanIndex == 0) {
                    outRect.left = gutter;
                }

                // All columns get some spacing at the bottom and at the right side
                outRect.right = gutter;
                outRect.bottom = gutter;
            }
        }
    }
}
