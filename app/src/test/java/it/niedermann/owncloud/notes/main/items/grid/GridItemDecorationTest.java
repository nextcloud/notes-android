/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.items.grid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;

import it.niedermann.owncloud.notes.main.items.ItemAdapter;

@RunWith(RobolectricTestRunner.class)
public class GridItemDecorationTest {

    private final ItemAdapter itemAdapter = mock(ItemAdapter.class);
    private final RecyclerView recyclerView = mock(RecyclerView.class);
    private final View view = mock(View.class);
    private final StaggeredGridLayoutManager.LayoutParams layoutParams = mock(StaggeredGridLayoutManager.LayoutParams.class);

    @Test
    public void getItemOffsets() {
        when(view.getLayoutParams()).thenReturn(layoutParams);
        when(itemAdapter.getFirstPositionOfViewType(anyInt())).thenReturn(0);
        when(itemAdapter.getItemViewType(anyInt())).then((arg) -> Arrays.asList(0, 4, 9).contains(arg.getArgument(0, Integer.class))
                ? ItemAdapter.TYPE_SECTION
                : ItemAdapter.TYPE_NOTE_ONLY_TITLE);

        assertThrows("Requires at least one column", IllegalArgumentException.class, () -> new GridItemDecoration(itemAdapter, 0, 5, 5, 5, 5, 5));

        final var oneColumn = new GridItemDecoration(itemAdapter, 1, 5, 5, 5, 5, 5);

        testAssertion(oneColumn, 0, 0, true, 5, 5, 5, 5);
        testAssertion(oneColumn, 0, 1, false, 0, 5, 5, 5);
        testAssertion(oneColumn, 0, 2, false, 0, 5, 5, 5);
        testAssertion(oneColumn, 0, 3, false, 0, 5, 5, 5);
        testAssertion(oneColumn, 0, 4, true, 5, 5, 5, 5);
        testAssertion(oneColumn, 0, 5, false, 0, 5, 5, 5);
        testAssertion(oneColumn, 0, 6, false, 0, 5, 5, 5);
        testAssertion(oneColumn, 0, 7, false, 0, 5, 5, 5);
        testAssertion(oneColumn, 0, 8, false, 0, 5, 5, 5);
        testAssertion(oneColumn, 0, 9, true, 5, 5, 5, 5);

        final var twoColumns = new GridItemDecoration(itemAdapter, 2, 5, 5, 5, 5, 5);

        testAssertion(twoColumns, 0, 0, true, 5, 5, 5, 5);
        testAssertion(twoColumns, 0, 1, false, 0, 5, 5, 5);
        testAssertion(twoColumns, 1, 2, false, 0, 0, 5, 5);
        testAssertion(twoColumns, 0, 3, false, 0, 5, 5, 5);
        testAssertion(twoColumns, 0, 4, true, 5, 5, 5, 5);
        testAssertion(twoColumns, 0, 5, false, 0, 5, 5, 5);
        testAssertion(twoColumns, 1, 6, false, 0, 0, 5, 5);
        testAssertion(twoColumns, 0, 7, false, 0, 5, 5, 5);
        testAssertion(twoColumns, 0, 8, false, 0, 5, 5, 5);
        testAssertion(twoColumns, 0, 9, true, 5, 5, 5, 5);

        final var threeColumns = new GridItemDecoration(itemAdapter, 3, 5, 5, 5, 5, 5);

        testAssertion(threeColumns, 0, 0, true, 5, 5, 5, 5);
        testAssertion(threeColumns, 0, 1, false, 0, 5, 5, 5);
        testAssertion(threeColumns, 1, 2, false, 0, 0, 5, 5);
        testAssertion(threeColumns, 2, 3, false, 0, 0, 5, 5);
        testAssertion(threeColumns, 0, 4, true, 5, 5, 5, 5);
        testAssertion(threeColumns, 0, 5, false, 0, 5, 5, 5);
        testAssertion(threeColumns, 1, 6, false, 0, 0, 5, 5);
        testAssertion(threeColumns, 2, 7, false, 0, 0, 5, 5);
        testAssertion(threeColumns, 0, 8, false, 0, 5, 5, 5);
        testAssertion(threeColumns, 0, 9, true, 5, 5, 5, 5);
    }

    @SuppressWarnings("SameParameterValue")
    private void testAssertion(GridItemDecoration gid, int spanIndex, int position, boolean fullSpan, int top, int left, int right, int bottom) {
        when(layoutParams.getSpanIndex()).thenReturn(spanIndex);
        when(recyclerView.getChildAdapterPosition(any())).thenReturn(position);
        final var result = new Rect();
        gid.getItemOffsets(result, view, recyclerView, mock(RecyclerView.State.class));

        if (fullSpan) {
            verify(layoutParams).setFullSpan(true);
        }
        reset(layoutParams);
        assertEquals(top, result.top);
        assertEquals(left, result.left);
        assertEquals(right, result.right);
        assertEquals(bottom, result.bottom);
    }
}