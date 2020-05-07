package it.niedermann.owncloud.notes.android.fragment;

import android.util.Log;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

public class SearchableBaseNoteFragmentTest {

    @Test
    public void testCountOccurrencesFixed() {
        try {
            Method method = SearchableBaseNoteFragment.class.getDeclaredMethod("countOccurrences", String.class, String.class);
            method.setAccessible(true);

            for (int count = 0; count <= 15; ++count) {
                StringBuilder sb = new StringBuilder("Mike Chester Wang");
                Random rand = new Random();
                for (int i = 0; i < count; ++i) {
                    sb.append(sb);
                }

                long startTime = System.currentTimeMillis();
                int num = (int) method.invoke(null, sb.toString(), "Chester");
                long endTime = System.currentTimeMillis();
                System.out.println("Fixed Version");
                System.out.println("Total Time: " + (endTime - startTime) + " ms");
                System.out.println("Total Times: " + num);
                System.out.println("String Size: " + (sb.length() / 1024) + " K");
                assertEquals((int) Math.pow(2, count), num);
                System.out.println();

                if (endTime - startTime > 10) {
                    fail("The algorithm spends too much time.");
                }
            }

        } catch (Exception e) {
            fail(Arrays.toString(e.getStackTrace()));
            Log.e("Test Count Occurrences Fixed", Arrays.toString(e.getStackTrace()));
        }
    }

    @Test
    public void testCountOccurrencesRandom() {
        try {
            Method method = SearchableBaseNoteFragment.class.getDeclaredMethod("countOccurrences", String.class, String.class);
            method.setAccessible(true);

            for (int count = 10; count <= 15; ++count) {
                StringBuilder sb = new StringBuilder("Mike Chester Wang");
                Random rand = new Random();
                for (int i = 0; i < count * 100; ++i) {
                    sb.append(rand.nextDouble());
                    if (i % 100 == 0) {
                        sb.append("flag");
                    }
                }

                long startTime = System.currentTimeMillis();
                int num = (int) method.invoke(null, sb.toString(), String.valueOf(rand.nextInt(100)));
                long endTime = System.currentTimeMillis();
                System.out.println("Random Version");
                System.out.println("Total Time: " + (endTime - startTime) + " ms");
                System.out.println("Total Times: " + num);
                System.out.println("String Size: " + (sb.length() / 1024) + " K");
                System.out.println();

                if (endTime - startTime > 10) {
                    fail("The algorithm spends too much time.");
                }
            }

        } catch (Exception e) {
            fail(Arrays.toString(e.getStackTrace()));
            Log.e("Test Count Occurrences Random", Arrays.toString(e.getStackTrace()));
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
            Log.e("Test Null Or Empty Input", Arrays.toString(e.getStackTrace()));
        }
    }
}