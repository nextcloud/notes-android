/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.items.grid;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import it.niedermann.owncloud.notes.main.items.ItemAdapter;
import it.niedermann.owncloud.notes.main.items.section.SectionItemDecoration;

public class GridItemDecoration extends SectionItemDecoration {

    @NonNull
    private final ItemAdapter adapter;
    private final int spanCount;
    private final int gutter;

    public GridItemDecoration(@NonNull ItemAdapter adapter, int spanCount, @Px int sectionLeft, @Px int sectionTop, @Px int sectionRight, @Px int sectionBottom, @Px int gutter) {
        super(adapter, sectionLeft, sectionTop, sectionRight, sectionBottom);
        if(spanCount < 1) {
            throw new IllegalArgumentException("Requires at least one span");
        }
        this.spanCount = spanCount;
        this.adapter = adapter;
        this.gutter = gutter;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        final int position = parent.getChildAdapterPosition(view);
        if (position >= 0) {
            final var lp = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();

            if (adapter.getItemViewType(position) == ItemAdapter.TYPE_SECTION) {
                lp.setFullSpan(true);
            } else {
                final int spanIndex = lp.getSpanIndex();

                // First row gets some spacing at the top
                final int firstSectionPosition = adapter.getFirstPositionOfViewType(ItemAdapter.TYPE_SECTION);
                if (position < spanCount && (firstSectionPosition < 0 || position < firstSectionPosition)) {
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
