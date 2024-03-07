/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.edit;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

public class SearchableBaseNoteFragmentTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testCountOccurrencesFixed() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final var method = SearchableBaseNoteFragment.class.getDeclaredMethod("countOccurrences", String.class, String.class);
        method.setAccessible(true);

        for (int count = 0; count <= 15; ++count) {
            final StringBuilder sb = new StringBuilder("Mike Chester Wang");
            for (int i = 0; i < count; ++i) {
                sb.append(sb);
            }

            final long startTime = System.currentTimeMillis();
            final int num = (int) method.invoke(null, sb.toString(), "Chester");
            final long endTime = System.currentTimeMillis();
            System.out.println("Fixed Version");
            System.out.println("Total Time: " + (endTime - startTime) + " ms");
            System.out.println("Total Times: " + num);
            System.out.println("String Size: " + (sb.length() / 1024.0) + " K");
            Assert.assertEquals((int) Math.pow(2, count), num);
            System.out.println();
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testNullOrEmptyInput() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final var method = SearchableBaseNoteFragment.class.getDeclaredMethod("countOccurrences", String.class, String.class);
        method.setAccessible(true);

        int num;
        num = (int) method.invoke(null, null, "Hi");
        Assert.assertEquals(0, num);
        num = (int) method.invoke(null, "Hi my name is Mike Chester Wang", null);
        Assert.assertEquals(0, num);
        num = (int) method.invoke(null, "", "Hi");
        Assert.assertEquals(0, num);
        num = (int) method.invoke(null, "Hi my name is Mike Chester Wang", "");
        Assert.assertEquals(0, num);
    }
}
