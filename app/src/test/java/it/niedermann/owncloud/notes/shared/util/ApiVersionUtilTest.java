package it.niedermann.owncloud.notes.shared.util;

import android.os.Build;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import it.niedermann.owncloud.notes.shared.model.ApiVersion;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.P})
public class ApiVersionUtilTest extends TestCase {

    @Test
    public void testSanitizeVersionString_invalid_one() {
        assertEquals(0, ApiVersionUtil.parse(null).size());
        assertEquals(0, ApiVersionUtil.parse("").size());
        assertEquals(0, ApiVersionUtil.parse(" ").size());
        assertEquals(0, ApiVersionUtil.parse("{}").size());
        assertEquals(0, ApiVersionUtil.parse("[]").size());
    }

    @Test
    public void testSanitizeVersionString_valid_one() {
        Collection<ApiVersion> result;
        ApiVersion current;

        result = ApiVersionUtil.parse("[0.2]");
        assertEquals(1, result.size());
        current = result.iterator().next();
        assertEquals(0, current.getMajor());
        assertEquals(2, current.getMinor());

        result = ApiVersionUtil.parse("[1.0]");
        assertEquals(1, result.size());
        current = result.iterator().next();
        assertEquals(1, current.getMajor());
        assertEquals(0, current.getMinor());

        result = ApiVersionUtil.parse("[\"0.2\"]");
        assertEquals(1, result.size());
        current = result.iterator().next();
        assertEquals(0, current.getMajor());
        assertEquals(2, current.getMinor());

        result = ApiVersionUtil.parse("[\"1.0\"]");
        assertEquals(1, result.size());
        current = result.iterator().next();
        assertEquals(1, current.getMajor());
        assertEquals(0, current.getMinor());

        result = ApiVersionUtil.parse("1.0");
        assertEquals(1, result.size());
        current = result.iterator().next();
        assertEquals(1, current.getMajor());
        assertEquals(0, current.getMinor());
    }

    @Test
    public void testSanitizeVersionString_invalid_many() {
        Collection<ApiVersion> result;
        ApiVersion current;
        Iterator<ApiVersion> iterator;

        result = ApiVersionUtil.parse("[0.2, foo]");
        assertEquals(1, result.size());
        iterator = result.iterator();
        current = iterator.next();
        assertEquals(0, current.getMajor());
        assertEquals(2, current.getMinor());

        result = ApiVersionUtil.parse("[foo, 1.1]");
        assertEquals(1, result.size());
        iterator = result.iterator();
        current = iterator.next();
        assertEquals(1, current.getMajor());
        assertEquals(1, current.getMinor());

        assertEquals(0, ApiVersionUtil.parse("[foo, bar]").size());

        result = ApiVersionUtil.parse("[, 1.1]");
        assertEquals(1, result.size());
        iterator = result.iterator();
        current = iterator.next();
        assertEquals(1, current.getMajor());
        assertEquals(1, current.getMinor());

        result = ApiVersionUtil.parse("[1.1, ?]");
        assertEquals(1, result.size());
        iterator = result.iterator();
        current = iterator.next();
        assertEquals(1, current.getMajor());
        assertEquals(1, current.getMinor());
    }

    @Test
    public void testSanitizeVersionString_valid_many() {
        Collection<ApiVersion> result;
        ApiVersion current;
        Iterator<ApiVersion> iterator;

        result = ApiVersionUtil.parse("[0.2, 1.0]");
        assertEquals(2, result.size());
        iterator = result.iterator();
        current = iterator.next();
        assertEquals(0, current.getMajor());
        assertEquals(2, current.getMinor());
        current = iterator.next();
        assertEquals(1, current.getMajor());
        assertEquals(0, current.getMinor());

        result = ApiVersionUtil.parse("[\"0.2\", \"1.0\"]");
        assertEquals(2, result.size());
        iterator = result.iterator();
        current = iterator.next();
        assertEquals(0, current.getMajor());
        assertEquals(2, current.getMinor());
        current = iterator.next();
        assertEquals(1, current.getMajor());
        assertEquals(0, current.getMinor());

        result = ApiVersionUtil.parse("[0.2,1.0]");
        assertEquals(2, result.size());
        iterator = result.iterator();
        current = iterator.next();
        assertEquals(0, current.getMajor());
        assertEquals(2, current.getMinor());
        current = iterator.next();
        assertEquals(1, current.getMajor());
        assertEquals(0, current.getMinor());

        result = ApiVersionUtil.parse("[\"0.2\",\"1.0\"]");
        assertEquals(2, result.size());
        iterator = result.iterator();
        current = iterator.next();
        assertEquals(0, current.getMajor());
        assertEquals(2, current.getMinor());
        current = iterator.next();
        assertEquals(1, current.getMajor());
        assertEquals(0, current.getMinor());

        result = ApiVersionUtil.parse("[0.2, \"1.0\"]");
        assertEquals(2, result.size());
        iterator = result.iterator();
        current = iterator.next();
        assertEquals(0, current.getMajor());
        assertEquals(2, current.getMinor());
        current = iterator.next();
        assertEquals(1, current.getMajor());
        assertEquals(0, current.getMinor());

        result = ApiVersionUtil.parse("[0.2,\"1.0\"]");
        assertEquals(2, result.size());
        iterator = result.iterator();
        current = iterator.next();
        assertEquals(0, current.getMajor());
        assertEquals(2, current.getMinor());
        current = iterator.next();
        assertEquals(1, current.getMajor());
        assertEquals(0, current.getMinor());
    }

    @Test
    public void testSerialize() {
        assertNull(ApiVersionUtil.serialize(null));
        assertNull(ApiVersionUtil.serialize(Collections.emptyList()));

        assertEquals("[0.2]", ApiVersionUtil.serialize(Collections.singleton(ApiVersion.API_VERSION_0_2)));
        assertEquals("[1.0]", ApiVersionUtil.serialize(Collections.singleton(ApiVersion.API_VERSION_1_0)));

        assertEquals("[1.0]", ApiVersionUtil.serialize(Arrays.asList(ApiVersion.API_VERSION_1_0, null)));
        assertEquals("[1.0]", ApiVersionUtil.serialize(Arrays.asList(null, ApiVersion.API_VERSION_1_0)));

        assertEquals("[0.2,1.0]", ApiVersionUtil.serialize(Arrays.asList(ApiVersion.API_VERSION_0_2, ApiVersion.API_VERSION_1_0)));

        // TODO sure...?
        assertEquals("[1.0,1.0]", ApiVersionUtil.serialize(Arrays.asList(ApiVersion.API_VERSION_1_0, ApiVersion.API_VERSION_1_0)));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testGetPreferredApiVersion() {
        assertNull(ApiVersionUtil.getPreferredApiVersion(null));
        assertNull(ApiVersionUtil.getPreferredApiVersion(""));
        assertNull(ApiVersionUtil.getPreferredApiVersion("[]"));
        assertNull(ApiVersionUtil.getPreferredApiVersion("foo"));

        ApiVersion result;

        result = ApiVersionUtil.getPreferredApiVersion("[0.2]");
        assertEquals(0, result.getMajor());
        assertEquals(2, result.getMinor());

        result = ApiVersionUtil.getPreferredApiVersion("[1.1]");
        assertEquals(1, result.getMajor());
        assertEquals(1, result.getMinor());

        result = ApiVersionUtil.getPreferredApiVersion("[0.2,1.1]");
        assertEquals(1, result.getMajor());
        assertEquals(1, result.getMinor());

        result = ApiVersionUtil.getPreferredApiVersion("[1.1,0.2]");
        assertEquals(1, result.getMajor());
        assertEquals(1, result.getMinor());

        result = ApiVersionUtil.getPreferredApiVersion("[10.0,1.1,1.0,0.2]");
        assertEquals(1, result.getMajor());
        assertEquals(1, result.getMinor());

        result = ApiVersionUtil.getPreferredApiVersion("[1.1,1.5,1.0]");
        assertEquals(1, result.getMajor());
        assertEquals(5, result.getMinor());

        result = ApiVersionUtil.getPreferredApiVersion("[1.1,,foo,1.0]");
        assertEquals(1, result.getMajor());
        assertEquals(1, result.getMinor());
    }
}