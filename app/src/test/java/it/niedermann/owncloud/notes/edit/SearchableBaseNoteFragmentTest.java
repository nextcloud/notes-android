package it.niedermann.owncloud.notes.edit;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

public class SearchableBaseNoteFragmentTest {

    @SuppressWarnings("ConstantConditions")
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
                Assert.assertEquals((int) Math.pow(2, count), num);
                System.out.println();
            }

        } catch (Exception e) {
            Assert.fail(Arrays.toString(e.getStackTrace()));
            System.out.println("Test Count Occurrences Fixed" + Arrays.toString(e.getStackTrace()));
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testNullOrEmptyInput() {
        try {
            Method method = SearchableBaseNoteFragment.class.getDeclaredMethod("countOccurrences", String.class, String.class);
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

        } catch (Exception e) {
            Assert.fail(Arrays.toString(e.getStackTrace()));
            System.out.println("Test Null Or Empty Input" + Arrays.toString(e.getStackTrace()));
        }
    }
}