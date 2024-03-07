/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.sync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Color;

import com.google.gson.JsonParser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class CapabilitiesDeserializerTest {

    private final CapabilitiesDeserializer deserializer = new CapabilitiesDeserializer();

    @Test
    public void testDefaultWithoutApiVersion() {
        //language=json
        final String response = "" +
                "{" +
                "    \"version\":{" +
                "        \"major\":18," +
                "        \"minor\":0," +
                "        \"micro\":4," +
                "        \"string\":\"18.0.4\"," +
                "        \"edition\":\"\"," +
                "        \"extendedSupport\":false" +
                "    }," +
                "    \"capabilities\":{" +
                "        \"theming\":{" +
                "            \"color\":\"#1E4164\"," +
                "            \"color-text\":\"#ffffff\"" +
                "        }" +
                "    }" +
                "}";
        final var capabilities = deserializer.deserialize(JsonParser.parseString(response), null, null);
        assertNull(capabilities.getETag());
        assertNull(capabilities.getApiVersion());
        assertEquals(Color.parseColor("#1E4164"), capabilities.getColor());
        assertEquals(Color.parseColor("#ffffff"), capabilities.getTextColor());
    }

    @Test
    public void testDefaultWithApiVersion() {
        //language=json
        final String response = "" +
                "{" +
                "    \"version\":{" +
                "        \"major\":18," +
                "        \"minor\":0," +
                "        \"micro\":4," +
                "        \"string\":\"18.0.4\"," +
                "        \"edition\":\"\"," +
                "        \"extendedSupport\":false" +
                "    }," +
                "    \"capabilities\":{" +
                "        \"notes\":{" +
                "            \"api_version\": [" +
                "                \"0.2\"," +
                "                \"1.1\"" +
                "            ]" +
                "        }," +
                "        \"theming\":{" +
                "            \"color\":\"#1E4164\"," +
                "            \"color-text\":\"#ffffff\"" +
                "        }" +
                "    }" +
                "}";
        final var capabilities = deserializer.deserialize(JsonParser.parseString(response), null, null);
        assertNull(capabilities.getETag());
        assertEquals("[\"0.2\",\"1.1\"]", capabilities.getApiVersion());
        assertEquals(Color.parseColor("#1E4164"), capabilities.getColor());
        assertEquals(Color.parseColor("#ffffff"), capabilities.getTextColor());
    }

    /**
     * According to the <a href="https://github.com/nextcloud/notes/blob/master/docs/api/README.md#capabilites">REST-API documentation</a>, the <code>api_version</code> property is a "list of strings", so a plain string is not allowed.
     */
    @Test
    public void testDefaultWithInvalidApiVersion() {
        //language=json
        final String response = "" +
                "{" +
                "    \"version\":{" +
                "        \"major\":18," +
                "        \"minor\":0," +
                "        \"micro\":4," +
                "        \"string\":\"18.0.4\"," +
                "        \"edition\":\"\"," +
                "        \"extendedSupport\":false" +
                "    }," +
                "    \"capabilities\":{" +
                "        \"notes\":{" +
                "            \"api_version\": \"1.0\"" +
                "        }," +
                "        \"theming\":{" +
                "            \"color\":\"#1E4164\"," +
                "            \"color-text\":\"#ffffff\"" +
                "        }" +
                "    }" +
                "}";
        final var capabilities = deserializer.deserialize(JsonParser.parseString(response), null, null);
        assertNull(capabilities.getETag());
        assertEquals("\"1.0\"", capabilities.getApiVersion());
        assertEquals(Color.parseColor("#1E4164"), capabilities.getColor());
        assertEquals(Color.parseColor("#ffffff"), capabilities.getTextColor());
    }


    @Test
    public void testRealisticSample() {
        //language=json
        final String response = "" +
                "{" +
                "    \"version\": {" +
                "        \"major\": 20," +
                "        \"minor\": 0," +
                "        \"micro\": 7," +
                "        \"string\": \"20.0.7\"," +
                "        \"edition\": \"\"," +
                "        \"extendedSupport\": false" +
                "    }," +
                "    \"capabilities\": {" +
                "        \"core\": {" +
                "            \"pollinterval\": 60," +
                "            \"webdav-root\": \"remote.php/webdav\"" +
                "        }," +
                "        \"bruteforce\": {" +
                "            \"delay\": 0" +
                "        }," +
                "        \"files\": {" +
                "            \"bigfilechunking\": true," +
                "            \"blacklisted_files\": [" +
                "                \".htaccess\"" +
                "            ]," +
                "            \"directEditing\": {" +
                "                \"url\": \"https://nextcloud.example.com/ocs/v2.php/apps/files/api/v1/directEditing\"," +
                "                \"etag\": \"6226ba873373f5e73a3ef504107523f7\"" +
                "            }," +
                "            \"comments\": true," +
                "            \"undelete\": true," +
                "            \"versioning\": true" +
                "        }," +
                "        \"activity\": {" +
                "            \"apiv2\": [" +
                "                \"filters\"," +
                "                \"filters-api\"," +
                "                \"previews\"," +
                "                \"rich-strings\"" +
                "            ]" +
                "        }," +
                "        \"ocm\": {" +
                "            \"enabled\": true," +
                "            \"apiVersion\": \"1.0-proposal1\"," +
                "            \"endPoint\": \"https://nextcloud.example.com/index.php/ocm\"," +
                "            \"resourceTypes\": [" +
                "                {" +
                "                    \"name\": \"file\"," +
                "                    \"shareTypes\": [" +
                "                        \"user\"," +
                "                        \"group\"" +
                "                    ]," +
                "                    \"protocols\": {" +
                "                        \"webdav\": \"/public.php/webdav/\"" +
                "                    }" +
                "                }" +
                "            ]" +
                "        }," +
                "        \"dav\": {" +
                "            \"chunking\": \"1.0\"" +
                "        }," +
                "        \"deck\": {" +
                "            \"version\": \"1.2.5\"," +
                "            \"canCreateBoards\": true" +
                "        }," +
                "        \"notes\": {" +
                "            \"api_version\": [" +
                "                \"0.2\"," +
                "                \"1.1\"" +
                "            ]," +
                "            \"version\": \"4.0.4\"" +
                "        }," +
                "        \"notifications\": {" +
                "            \"ocs-endpoints\": [" +
                "                \"list\"," +
                "                \"get\"," +
                "                \"delete\"," +
                "                \"delete-all\"," +
                "                \"icons\"," +
                "                \"rich-strings\"," +
                "                \"action-web\"," +
                "                \"user-status\"" +
                "            ]," +
                "            \"push\": [" +
                "                \"devices\"," +
                "                \"object-data\"," +
                "                \"delete\"" +
                "            ]," +
                "            \"admin-notifications\": [" +
                "                \"ocs\"," +
                "                \"cli\"" +
                "            ]" +
                "        }," +
                "        \"password_policy\": {" +
                "            \"minLength\": 8," +
                "            \"enforceNonCommonPassword\": true," +
                "            \"enforceNumericCharacters\": false," +
                "            \"enforceSpecialCharacters\": false," +
                "            \"enforceUpperLowerCase\": false," +
                "            \"api\": {" +
                "                \"generate\": \"https://nextcloud.example.com/ocs/v2.php/apps/password_policy/api/v1/generate\"," +
                "                \"validate\": \"https://nextcloud.example.com/ocs/v2.php/apps/password_policy/api/v1/validate\"" +
                "            }" +
                "        }," +
                "        \"files_sharing\": {" +
                "            \"sharebymail\": {" +
                "                \"enabled\": true," +
                "                \"upload_files_drop\": {" +
                "                    \"enabled\": true" +
                "                }," +
                "                \"password\": {" +
                "                    \"enabled\": true," +
                "                    \"enforced\": false" +
                "                }," +
                "                \"expire_date\": {" +
                "                    \"enabled\": true" +
                "                }" +
                "            }," +
                "            \"api_enabled\": true," +
                "            \"public\": {" +
                "                \"enabled\": true," +
                "                \"password\": {" +
                "                    \"enforced\": false," +
                "                    \"askForOptionalPassword\": false" +
                "                }," +
                "                \"expire_date\": {" +
                "                    \"enabled\": false" +
                "                }," +
                "                \"multiple_links\": true," +
                "                \"expire_date_internal\": {" +
                "                    \"enabled\": false" +
                "                }," +
                "                \"send_mail\": false," +
                "                \"upload\": true," +
                "                \"upload_files_drop\": true" +
                "            }," +
                "            \"resharing\": true," +
                "            \"user\": {" +
                "                \"send_mail\": false," +
                "                \"expire_date\": {" +
                "                    \"enabled\": true" +
                "                }" +
                "            }," +
                "            \"group_sharing\": true," +
                "            \"group\": {" +
                "                \"enabled\": true," +
                "                \"expire_date\": {" +
                "                    \"enabled\": true" +
                "                }" +
                "            }," +
                "            \"default_permissions\": 31," +
                "            \"federation\": {" +
                "                \"outgoing\": true," +
                "                \"incoming\": true," +
                "                \"expire_date\": {" +
                "                    \"enabled\": true" +
                "                }" +
                "            }," +
                "            \"sharee\": {" +
                "                \"query_lookup_default\": false" +
                "            }" +
                "        }," +
                "        \"theming\": {" +
                "            \"name\": \"Sample name\"," +
                "            \"url\": \"https://nextcloud.com\"," +
                "            \"slogan\": \"a safe home for all your data\"," +
                "            \"color\": \"#44616B\"," +
                "            \"color-text\": \"#ffffff\"," +
                "            \"color-element\": \"#44616B\"," +
                "            \"color-element-bright\": \"#44616B\"," +
                "            \"color-element-dark\": \"#44616B\"," +
                "            \"logo\": \"https://nextcloud.example.com/core/img/logo/logo.svg?v=8\"," +
                "            \"background\": \"#44616B\"," +
                "            \"background-plain\": true," +
                "            \"background-default\": true," +
                "            \"logoheader\": \"https://nextcloud.example.com/core/img/logo/logo.svg?v=8\"," +
                "            \"favicon\": \"https://nextcloud.example.com/core/img/logo/logo.svg?v=8\"" +
                "        }," +
                "        \"user_status\": {" +
                "            \"enabled\": true," +
                "            \"supports_emoji\": true" +
                "        }," +
                "        \"weather_status\": {" +
                "            \"enabled\": true" +
                "        }" +
                "    }" +
                "}";
        final var capabilities = deserializer.deserialize(JsonParser.parseString(response), null, null);
        assertNull(capabilities.getETag());
        assertEquals("[\"0.2\",\"1.1\"]", capabilities.getApiVersion());
        assertEquals(Color.parseColor("#44616B"), capabilities.getColor());
        assertEquals(Color.parseColor("#ffffff"), capabilities.getTextColor());
        assertFalse("Wrongly reporting that direct editing is supported", capabilities.isDirectEditingAvailable());
    }


    @Test
    public void testDirectEditing() {
        //language=json
        final String responseOk = "" +
                "{" +
                "    \"version\":{" +
                "        \"major\":18," +
                "        \"minor\":0," +
                "        \"micro\":4," +
                "        \"string\":\"18.0.4\"," +
                "        \"edition\":\"\"," +
                "        \"extendedSupport\":false" +
                "    }," +
                "    \"capabilities\":{" +
                "        \"files\": {" +
                "            \"directEditing\": {" +
                "                \"url\": \"https://nextcloud.example.com/ocs/v2.php/apps/files/api/v1/directEditing\"," +
                "                \"etag\": \"6226ba873373f5e73a3ef504107523f7\"," +
                "                \"supportsFileId\": true" +
                "            }" +
                "        }" +
                "    }" +
                "}";
        final var capabilities = deserializer.deserialize(JsonParser.parseString(responseOk), null, null);
        assertTrue("Direct editing should be considered supported", capabilities.isDirectEditingAvailable());

        //language=json
        final String responseNotOk = "" +
                "{" +
                "    \"version\":{" +
                "        \"major\":18," +
                "        \"minor\":0," +
                "        \"micro\":4," +
                "        \"string\":\"18.0.4\"," +
                "        \"edition\":\"\"," +
                "        \"extendedSupport\":false" +
                "    }," +
                "    \"capabilities\":{" +
                "        \"files\": {" +
                "            \"directEditing\": {" +
                "                \"url\": \"https://nextcloud.example.com/ocs/v2.php/apps/files/api/v1/directEditing\"," +
                "                \"etag\": \"6226ba873373f5e73a3ef504107523f7\"," +
                "                \"supportsFileId\": false" +
                "            }" +
                "        }" +
                "    }" +
                "}";
        final var capabilities1 = deserializer.deserialize(JsonParser.parseString(responseNotOk), null, null);
        assertFalse("Wrongly reporting that direct editing is supported", capabilities1.isDirectEditingAvailable());

    }
}
