package it.niedermann.owncloud.notes.util;

import org.junit.Test;

import it.niedermann.owncloud.notes.util.CategorySortingMethod;

import static org.junit.Assert.*;

public class CategorySortingMethodTest {

@Test
public void getCSMID() {
        CategorySortingMethod csm0 = CategorySortingMethod.SORT_LEXICOGRAPHICAL_ASC;
        assertEquals(0, csm0.getCSMID());
        CategorySortingMethod csm1 = CategorySortingMethod.SORT_MODIFIED_DESC;
        assertEquals(1, csm1.getCSMID());
        }
        }