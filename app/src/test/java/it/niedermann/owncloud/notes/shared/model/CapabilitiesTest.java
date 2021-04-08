package it.niedermann.owncloud.notes.shared.model;

import android.graphics.Color;
import android.os.Build;

import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.P})
public class CapabilitiesTest {

    @Test
    public void testDefaultWithoutApiVersion() throws NextcloudHttpRequestFailedException {
        final String response = "" +
                "{" +
                "    'ocs':{" +
                "        'meta':{" +
                "            'status':'ok'," +
                "            'statuscode':200," +
                "            'message':'OK'" +
                "        }," +
                "        'data':{" +
                "            'version':{" +
                "                'major':18," +
                "                'minor':0," +
                "                'micro':4," +
                "                'string':'18.0.4'," +
                "                'edition':''," +
                "                'extendedSupport':false" +
                "            }," +
                "            'capabilities':{" +
                "                'theming':{" +
                "                    'color':'#1E4164'," +
                "                    'color-text':'#ffffff'" +
                "                }" +
                "            }" +
                "        }" +
                "    }" +
                "}";
        final Capabilities capabilities = new Capabilities(response, null);
        assertNull(capabilities.getETag());
        assertNull(capabilities.getApiVersion());
        assertEquals(Integer.valueOf(Color.parseColor("#1E4164")), capabilities.getColor());
        assertEquals(Integer.valueOf(Color.parseColor("#ffffff")), capabilities.getTextColor());
    }

    @Test
    public void testDefaultWithApiVersion() throws NextcloudHttpRequestFailedException {
        final String response = "" +
                "{" +
                "    'ocs':{" +
                "        'meta':{" +
                "            'status':'ok'," +
                "            'statuscode':200," +
                "            'message':'OK'" +
                "        }," +
                "        'data':{" +
                "            'version':{" +
                "                'major':18," +
                "                'minor':0," +
                "                'micro':4," +
                "                'string':'18.0.4'," +
                "                'edition':''," +
                "                'extendedSupport':false" +
                "            }," +
                "            'capabilities':{" +
                "                'notes':{" +
                "                    'api_version': '1.0'" +
                "                }," +
                "                'theming':{" +
                "                    'color':'#1E4164'," +
                "                    'color-text':'#ffffff'" +
                "                }" +
                "            }" +
                "        }" +
                "    }" +
                "}";
        final Capabilities capabilities = new Capabilities(response, null);
        assertNull(capabilities.getETag());
        assertEquals("1.0", capabilities.getApiVersion());
        assertEquals(Integer.valueOf(Color.parseColor("#1E4164")), capabilities.getColor());
        assertEquals(Integer.valueOf(Color.parseColor("#ffffff")), capabilities.getTextColor());
    }

    @Test
    public void etagShouldAlwaysBeStored() throws NextcloudHttpRequestFailedException {
        final Capabilities capabilities = new Capabilities("{ocs: {}}", "ed38bf28-e429-4231-84be-35d166acfb6d ");
        assertEquals("ed38bf28-e429-4231-84be-35d166acfb6d ", capabilities.getETag());
    }

    @Test(expected = NextcloudHttpRequestFailedException.class)
    public void throwsExceptionOnMaintenanceMode() throws NextcloudHttpRequestFailedException {
        final String response = "" +
                "{" +
                "   'ocs':{" +
                "        'meta':{" +
                "            'status':'ok'," +
                "            'statuscode':503," +
                "            'message':'OK'" +
                "        }" +
                "    }" +
                "}";
        new Capabilities(response, null);
    }
}