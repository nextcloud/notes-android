/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.core.app.ApplicationProvider;

import com.nextcloud.android.sso.exceptions.UnknownErrorException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import it.niedermann.owncloud.notes.main.navigation.NavigationItem;
import it.niedermann.owncloud.notes.persistence.entity.CategoryWithNotesCount;
import it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType;

@RunWith(RobolectricTestRunner.class)
public class MainViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private Context context;

    private Method fromCategoriesWithNotesCount;

    @Before
    public void setup() throws NoSuchMethodException {
        context = ApplicationProvider.getApplicationContext();
        fromCategoriesWithNotesCount = MainViewModel.class.getDeclaredMethod("fromCategoriesWithNotesCount", Context.class, String.class, List.class, Integer.TYPE, Integer.TYPE);
        fromCategoriesWithNotesCount.setAccessible(true);
    }

    @Test
    public void fromCategoriesWithNotesCount_nothing_expanded() throws InvocationTargetException, IllegalAccessException {
        //noinspection unchecked
        final var navigationItems = (List<NavigationItem>) fromCategoriesWithNotesCount.invoke(null, context, "", getSaneCategoriesWithNotesCount(), 56, 0);

        assertNotNull(navigationItems);
        assertEquals(5, navigationItems.size());
        assertEquals(ENavigationCategoryType.RECENT, navigationItems.get(0).type);
        assertEquals(ENavigationCategoryType.FAVORITES, navigationItems.get(1).type);
        assertEquals("Foo", navigationItems.get(2).label);
        assertEquals("Bar", navigationItems.get(3).label);
        assertEquals("Baz", navigationItems.get(4).label);
    }

    @Test
    public void fromCategoriesWithNotesCount_Bar_expanded() throws InvocationTargetException, IllegalAccessException {
        //noinspection unchecked
        final var navigationItems = (List<NavigationItem>) fromCategoriesWithNotesCount.invoke(null, context, "Bar", getSaneCategoriesWithNotesCount(), 56, 0);

        assertNotNull(navigationItems);
        assertEquals(9, navigationItems.size());
        assertEquals(ENavigationCategoryType.RECENT, navigationItems.get(0).type);
        assertEquals(ENavigationCategoryType.FAVORITES, navigationItems.get(1).type);
        assertEquals("Foo", navigationItems.get(2).label);
        assertEquals("Bar", navigationItems.get(3).label);
        assertEquals("abc", navigationItems.get(4).label);
        assertEquals("xyz", navigationItems.get(5).label);
        assertEquals("aaa", navigationItems.get(6).label);
        assertEquals("ddd", navigationItems.get(7).label);
        assertEquals("Baz", navigationItems.get(8).label);
    }

    @Test
    public void fromCategoriesWithNotesCount_invalid_expanded() throws InvocationTargetException, IllegalAccessException {
        //noinspection unchecked
        final var navigationItems = (List<NavigationItem>) fromCategoriesWithNotesCount.invoke(null, context, "ThisCategoryDoesNotExist", getSaneCategoriesWithNotesCount(), 56, 0);

        assertNotNull(navigationItems);
        assertEquals(5, navigationItems.size());
        assertEquals(ENavigationCategoryType.RECENT, navigationItems.get(0).type);
        assertEquals(ENavigationCategoryType.FAVORITES, navigationItems.get(1).type);
        assertEquals("Foo", navigationItems.get(2).label);
        assertEquals("Bar", navigationItems.get(3).label);
        assertEquals("Baz", navigationItems.get(4).label);
    }

    /**
     * Expanded sub categories are not supported and should therefore be treated like an unknown category
     */
    @Test
    public void fromCategoriesWithNotesCount_subcategory_expanded() throws InvocationTargetException, IllegalAccessException {
        //noinspection unchecked
        final var navigationItems = (List<NavigationItem>) fromCategoriesWithNotesCount.invoke(null, context, "Bar/abc", getSaneCategoriesWithNotesCount(), 56, 0);

        assertNotNull(navigationItems);
        assertEquals(5, navigationItems.size());
        assertEquals(ENavigationCategoryType.RECENT, navigationItems.get(0).type);
        assertEquals(ENavigationCategoryType.FAVORITES, navigationItems.get(1).type);
        assertEquals("Foo", navigationItems.get(2).label);
        assertEquals("Bar", navigationItems.get(3).label);
        assertEquals("Baz", navigationItems.get(4).label);
    }

    @Test
    public void fromCategoriesWithNotesCount_only_deep_category_without_favorites() throws InvocationTargetException, IllegalAccessException {
        //noinspection unchecked
        final var navigationItems = (List<NavigationItem>) fromCategoriesWithNotesCount.invoke(null, context, "Bar/abc", List.of(
                new CategoryWithNotesCount(1, "Bar/abc/def", 5)
        ), 0, 0);

        assertNotNull(navigationItems);
        assertEquals(3, navigationItems.size());
        assertEquals(ENavigationCategoryType.RECENT, navigationItems.get(0).type);
        assertEquals(ENavigationCategoryType.FAVORITES, navigationItems.get(1).type);
        assertEquals("Bar", navigationItems.get(2).label);
    }

    @Test
    public void containsNonInfrastructureRelatedItems() {
        //noinspection ConstantConditions
        final var vm = new MainViewModel(ApplicationProvider.getApplicationContext(), null);
        assertFalse(vm.containsNonInfrastructureRelatedItems(null));
        assertFalse(vm.containsNonInfrastructureRelatedItems(Collections.emptyList()));

        assertFalse(vm.containsNonInfrastructureRelatedItems(List.of(new UnknownErrorException("Software caused connection abort"))));
        assertFalse(vm.containsNonInfrastructureRelatedItems(List.of(new UnknownErrorException("failed to connect to example.com (port 443) from /:: (port 39885) after 5000ms: connect failed: ENETUNREACH (Network is unreachable)"))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new UnknownErrorException("Foo bar"))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new Exception("Software caused connection abort"))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new Exception("Software caused connection abort"), new UnknownErrorException("Software caused connection abort"))));

        assertFalse(vm.containsNonInfrastructureRelatedItems(List.of(new RuntimeException("Software caused connection abort"))));
        assertFalse(vm.containsNonInfrastructureRelatedItems(List.of(new RuntimeException("failed to connect to example.com (port 443) from /:: (port 39885) after 5000ms: connect failed: ENETUNREACH (Network is unreachable)"))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new RuntimeException("Foo bar"))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new Exception("Software caused connection abort"))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new Exception("Software caused connection abort"), new RuntimeException("Software caused connection abort"))));

        assertFalse(vm.containsNonInfrastructureRelatedItems(List.of(new RuntimeException(new UnknownErrorException("Software caused connection abort")))));
        assertFalse(vm.containsNonInfrastructureRelatedItems(List.of(new RuntimeException(new UnknownErrorException("failed to connect to example.com (port 443) from /:: (port 39885) after 5000ms: connect failed: ENETUNREACH (Network is unreachable)")))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new RuntimeException(new UnknownErrorException("Foo bar")))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new Exception("Software caused connection abort"))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new Exception("Software caused connection abort"), new RuntimeException(new UnknownErrorException("Software caused connection abort")))));

        assertFalse(vm.containsNonInfrastructureRelatedItems(List.of(new RuntimeException("Foo", new UnknownErrorException("Software caused connection abort")))));
        assertFalse(vm.containsNonInfrastructureRelatedItems(List.of(new RuntimeException("Foo", new UnknownErrorException("failed to connect to example.com (port 443) from /:: (port 39885) after 5000ms: connect failed: ENETUNREACH (Network is unreachable)")))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new RuntimeException("Foo", new UnknownErrorException("Foo bar")))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new Exception("Software caused connection abort"))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new Exception("Software caused connection abort"), new RuntimeException("Foo", new UnknownErrorException("Software caused connection abort")))));

        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new RuntimeException("Foo", new Exception("Software caused connection abort")))));
    }

    private static List<CategoryWithNotesCount> getSaneCategoriesWithNotesCount() {
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
