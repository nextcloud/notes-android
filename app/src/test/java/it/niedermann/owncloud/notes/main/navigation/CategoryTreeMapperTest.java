/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.navigation;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import it.niedermann.owncloud.notes.persistence.entity.CategoryWithNotesCount;
import it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 36)
public class CategoryTreeMapperTest {

    private Context context;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void nothingExpandedShowsRootsSortedAndCollapsed() {
        final var items = CategoryTreeMapper.map(context, Collections.emptySet(), getSaneCategories(), 56, 0);

        assertEquals(5, items.size());
        assertEquals(ENavigationCategoryType.RECENT, items.get(0).type);
        assertEquals(ENavigationCategoryType.FAVORITES, items.get(1).type);

        assertEquals("Bar", items.get(2).label);
        assertEquals(0, items.get(2).depth);
        assertEquals(CategoryExpandState.COLLAPSED, items.get(2).expandState);
        assertEquals(NavigationAdapter.ICON_MULTIPLE, items.get(2).icon);
        // Bar direct + all descendants: 30 + 10 + 5 + 10 + 8 + 2
        assertEquals(65, (int) items.get(2).count);

        assertEquals("Baz", items.get(3).label);
        assertEquals(CategoryExpandState.NOT_EXPANDABLE, items.get(3).expandState);
        assertEquals("Foo", items.get(4).label);
    }

    @Test
    public void unknownExpandedCategoryIsIgnored() {
        final var items = CategoryTreeMapper.map(context, Set.of("ThisDoesNotExist"), getSaneCategories(), 56, 0);

        assertEquals(5, items.size());
        assertEquals("Bar", items.get(2).label);
        assertEquals("Baz", items.get(3).label);
        assertEquals("Foo", items.get(4).label);
    }

    @Test
    public void expandingRootRevealsDirectSubCategories() {
        final var items = CategoryTreeMapper.map(context, Set.of("Bar"), getSaneCategories(), 56, 0);

        assertEquals(9, items.size());
        assertEquals("Bar", items.get(2).label);
        assertEquals(CategoryExpandState.EXPANDED, items.get(2).expandState);
        assertEquals(NavigationAdapter.ICON_MULTIPLE_OPEN, items.get(2).icon);
        assertEquals(30, (int) items.get(2).count);

        assertEquals("aaa", items.get(3).label);
        assertEquals(1, items.get(3).depth);
        assertEquals(CategoryExpandState.COLLAPSED, items.get(3).expandState);
        assertEquals(8, (int) items.get(3).count);

        assertEquals("abc", items.get(4).label);
        assertEquals(15, (int) items.get(4).count);
        assertEquals("ddd", items.get(5).label);
        assertEquals(CategoryExpandState.NOT_EXPANDABLE, items.get(5).expandState);
        assertEquals("xyz", items.get(6).label);

        assertEquals("Baz", items.get(7).label);
        assertEquals(0, items.get(7).depth);
        assertEquals("Foo", items.get(8).label);
    }

    @Test
    public void expandingSubCategoryRevealsDeeperLevel() {
        final var items = CategoryTreeMapper.map(context, Set.of("Bar", "Bar/abc"), getSaneCategories(), 56, 0);

        assertEquals(10, items.size());
        assertEquals("abc", items.get(4).label);
        assertEquals(CategoryExpandState.EXPANDED, items.get(4).expandState);
        assertEquals(10, (int) items.get(4).count);

        assertEquals("def", items.get(5).label);
        assertEquals(2, items.get(5).depth);
        assertEquals(CategoryExpandState.NOT_EXPANDABLE, items.get(5).expandState);
        assertEquals(5, (int) items.get(5).count);
    }

    @Test
    public void deepCategoryWithoutDirectNotesCreatesSyntheticParents() {
        final var input = List.of(new CategoryWithNotesCount(1, "Bar/abc/def", 5));

        final var collapsed = CategoryTreeMapper.map(context, Collections.emptySet(), input, 0, 0);
        assertEquals(3, collapsed.size());
        assertEquals("Bar", collapsed.get(2).label);
        assertEquals(5, (int) collapsed.get(2).count);

        final var expanded = CategoryTreeMapper.map(context, Set.of("Bar", "Bar/abc"), input, 0, 0);
        assertEquals(5, expanded.size());
        assertEquals("Bar", expanded.get(2).label);
        assertEquals(0, (int) expanded.get(2).count);
        assertEquals("abc", expanded.get(3).label);
        assertEquals("def", expanded.get(4).label);
        assertEquals(2, expanded.get(4).depth);
    }

    /**
     * A sibling category whose name breaks the alphabetical adjacency of a parent and its children
     * (e.g. "Work-2" sorts between "Work" and "Work/…") must not produce a duplicated parent entry.
     */
    @Test
    public void siblingBreakingAdjacencyDoesNotDuplicateParent() {
        final var input = List.of(
                new CategoryWithNotesCount(1, "Work", 5),
                new CategoryWithNotesCount(1, "Work-2", 1),
                new CategoryWithNotesCount(1, "Work/ProjectA", 4),
                new CategoryWithNotesCount(1, "Work/ProjectB", 2)
        );

        final var items = CategoryTreeMapper.map(context, Set.of("Work"), input, 12, 0);

        assertEquals(6, items.size());
        assertEquals("Work", items.get(2).label);
        assertEquals(CategoryExpandState.EXPANDED, items.get(2).expandState);
        assertEquals("ProjectA", items.get(3).label);
        assertEquals(1, items.get(3).depth);
        assertEquals("ProjectB", items.get(4).label);
        assertEquals("Work-2", items.get(5).label);
        assertEquals(0, items.get(5).depth);
    }

    private static List<CategoryWithNotesCount> getSaneCategories() {
        return List.of(
                new CategoryWithNotesCount(1, "Foo", 13),
                new CategoryWithNotesCount(1, "Bar", 30),
                new CategoryWithNotesCount(1, "Bar/abc", 10),
                new CategoryWithNotesCount(1, "Bar/abc/def", 5),
                new CategoryWithNotesCount(1, "Bar/xyz/zyx", 10),
                new CategoryWithNotesCount(1, "Bar/aaa/bbb", 8),
                new CategoryWithNotesCount(1, "Bar/ddd", 2),
                new CategoryWithNotesCount(1, "Baz", 13)
        );
    }
}
