package it.niedermann.owncloud.notes.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import android.content.Context;
import android.util.Log;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.SavedStateHandle;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import it.niedermann.owncloud.notes.main.navigation.NavigationItem;
import it.niedermann.owncloud.notes.persistence.entity.CategoryWithNotesCount;
import it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType;

@RunWith(RobolectricTestRunner.class)
public class MainViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private MainViewModel viewModel;
    private Context context;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        viewModel = new MainViewModel(ApplicationProvider.getApplicationContext(), mock(SavedStateHandle.class));
    }

    @Test
    public void fromCategoriesWithNotesCount() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final var fromCategoriesWithNotesCount = MainViewModel.class.getDeclaredMethod("fromCategoriesWithNotesCount", Context.class, String.class, List.class, Integer.TYPE, Integer.TYPE);
        fromCategoriesWithNotesCount.setAccessible(true);

        final var categoriesWithNotesCount = List.of(
                new CategoryWithNotesCount(1, "Foo", 13),
                new CategoryWithNotesCount(1, "Bar", 30),
                new CategoryWithNotesCount(1, "Bar/abc", 10),
                new CategoryWithNotesCount(1, "Bar/abc/def", 5),
                new CategoryWithNotesCount(1, "Bar/xyz/zyx", 10),
                new CategoryWithNotesCount(1, "Bar/aaa/bbb", 8),
                new CategoryWithNotesCount(1, "Bar/ddd", 2),
                new CategoryWithNotesCount(1, "Baz", 13)
        );

        //noinspection unchecked
        final var allCollapsed = (List<NavigationItem>) fromCategoriesWithNotesCount.invoke(null, context, "", categoriesWithNotesCount, 56, 0);

        assertNotNull(allCollapsed);
        assertEquals(5, allCollapsed.size());
        assertEquals(ENavigationCategoryType.RECENT, allCollapsed.get(0).type);
        assertEquals(ENavigationCategoryType.FAVORITES, allCollapsed.get(1).type);
        assertEquals("Foo", allCollapsed.get(2).label);
        assertEquals("Bar", allCollapsed.get(3).label);
        assertEquals("Baz", allCollapsed.get(4).label);

        //noinspection unchecked
        final var barExpanded = (List<NavigationItem>) fromCategoriesWithNotesCount.invoke(null, context, "Bar", categoriesWithNotesCount, 56, 0);

        assertNotNull(barExpanded);
        assertEquals(9, barExpanded.size());
        assertEquals(ENavigationCategoryType.RECENT, barExpanded.get(0).type);
        assertEquals(ENavigationCategoryType.FAVORITES, barExpanded.get(1).type);
        assertEquals("Foo", barExpanded.get(2).label);
        assertEquals("Bar", barExpanded.get(3).label);
        assertEquals("Bar/abc", barExpanded.get(4).label);
        assertEquals("Bar/xyz", barExpanded.get(5).label);
        assertEquals("Bar/aaa", barExpanded.get(6).label);
        assertEquals("Bar/ddd", barExpanded.get(7).label);
        assertEquals("Baz", barExpanded.get(8).label);
    }
}