package it.niedermann.owncloud.notes.shared.util;

import org.junit.Test;

import it.niedermann.owncloud.notes.shared.model.CategorySortingMethod;

import static org.junit.Assert.*;

public class CategorySortingMethodTest {

    @Test
    public void getCSMID() {
        CategorySortingMethod csm0 = CategorySortingMethod.SORT_MODIFIED_DESC;
        assertEquals(0, csm0.getCSMID());
        CategorySortingMethod csm1 = CategorySortingMethod.SORT_LEXICOGRAPHICAL_ASC;
        assertEquals(1, csm1.getCSMID());
    }

    @Test
    public void getSOrder() {
        CategorySortingMethod csm0 = CategorySortingMethod.SORT_MODIFIED_DESC;
        assertEquals("MODIFIED DESC", csm0.getSorder());
        CategorySortingMethod csm1 = CategorySortingMethod.SORT_LEXICOGRAPHICAL_ASC;
        assertEquals("TITLE COLLATE NOCASE ASC", csm1.getSorder());
    }

    @Test
    public void getCSM() {
        CategorySortingMethod csm0 = CategorySortingMethod.SORT_MODIFIED_DESC;
        assertEquals(csm0, CategorySortingMethod.getCSM(0));
        CategorySortingMethod csm1 = CategorySortingMethod.SORT_LEXICOGRAPHICAL_ASC;
        assertEquals(csm1, CategorySortingMethod.getCSM(1));
    }
}