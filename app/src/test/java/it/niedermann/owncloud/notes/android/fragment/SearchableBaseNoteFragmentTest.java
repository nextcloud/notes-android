package it.niedermann.owncloud.notes.android.fragment;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SearchableBaseNoteFragmentTest {

    @Test
    public void testCountOccurrencesFixed() {
        try {
            Method method = SearchableBaseNoteFragment.class.getDeclaredMethod("countOccurrences", String.class, String.class);
            method.setAccessible(true);

            for (int count = 0; count <= 15; ++count) {
                StringBuilder sb = new StringBuilder("Mike Chester Wang");
                for (int i = 0; i < count; ++i) {
                    sb.append(sb);
                }

                long startTime = System.currentTimeMillis();
                int num = (int) method.invoke(null, sb.toString(), "Chester");
                long endTime = System.currentTimeMillis();
                System.out.println("Fixed Version");
                System.out.println("Total Time: " + (endTime - startTime) + " ms");
                System.out.println("Total Times: " + num);
                System.out.println("String Size: " + (sb.length() / 1024.0) + " K");
                assertEquals((int) Math.pow(2, count), num);
                System.out.println();
            }

        } catch (Exception e) {
            fail(Arrays.toString(e.getStackTrace()));
            System.out.println("Test Count Occurrences Fixed" + Arrays.toString(e.getStackTrace()));
        }
    }

    @Test
    public void testNullOrEmptyInput() {
        try {
            Method method = SearchableBaseNoteFragment.class.getDeclaredMethod("countOccurrences", String.class, String.class);
            method.setAccessible(true);

            int num;
            num = (int) method.invoke(null, null, "Hi");
            assertEquals(0, num);
            num = (int) method.invoke(null, "Hi my name is Mike Chester Wang", null);
            assertEquals(0, num);
            num = (int) method.invoke(null, "", "Hi");
            assertEquals(0, num);
            num = (int) method.invoke(null, "Hi my name is Mike Chester Wang", "");
            assertEquals(0, num);

        } catch (Exception e) {
            fail(Arrays.toString(e.getStackTrace()));
            System.out.println("Test Null Or Empty Input" + Arrays.toString(e.getStackTrace()));
        }
    }
}