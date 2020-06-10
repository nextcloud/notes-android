package it.niedermann.owncloud.notes.model;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class GridItemDecoration extends RecyclerView.ItemDecoration {

    @NonNull
    private final ItemAdapter adapter;
    private final int gutter;

    public GridItemDecoration(@NonNull ItemAdapter adapter, int gutter) {
        this.adapter = adapter;
        this.gutter = gutter;
    }

    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        final int position = parent.getChildAdapterPosition(view);
        final StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();

        if (adapter.getItemViewType(position) == ItemAdapter.TYPE_SECTION) {
            lp.setFullSpan(true);
        } else {
            final int spanIndex = lp.getSpanIndex();

            if (position >= 0) {
                // First row gets some spacing at the top
                if (position < adapter.getFirstPositionOfViewType(ItemAdapter.TYPE_SECTION)) {
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
