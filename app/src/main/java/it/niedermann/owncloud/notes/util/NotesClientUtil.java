package it.niedermann.owncloud.notes.util;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import at.bitfire.cert4android.CustomCertManager;
import it.niedermann.owncloud.notes.R;

/**
 * Utils for Validation etc
 * Created by stefan on 25.09.15.
 */
public class NotesClientUtil {

    public enum LoginStatus {
        OK(0),
        AUTH_FAILED(R.string.error_username_password_invalid),
        CONNECTION_FAILED(R.string.error_io),
        NO_NETWORK(R.string.error_no_network),
        JSON_FAILED(R.string.error_json),
        SERVER_FAILED(R.string.error_server);

        @StringRes
        public final int str;
        LoginStatus(@StringRes int str) {
            this.str = str;
        }
    }

    /**
     * Checks if the given url String starts with http:// or https://
     *
     * @param url String
     * @return true, if the given String is only http
     */
    public static boolean isHttp(String url) {
        return url != null && url.length() > 4 && url.startsWith("http") && url.charAt(4) != 's';
    }

    /**
     * @param url      String
     * @param username String
     * @param password String
     * @return Username and Password are a valid Login-Combination for the given URL.
     */
    public static LoginStatus isValidLogin(CustomCertManager ccm, String url, String username, String password) {
        try {
            String targetURL = url + "index.php/apps/notes/api/v0.2/notes";
            HttpURLConnection con = SupportUtil.getHttpURLConnection(ccm, targetURL);
            con.setRequestMethod("GET");
            con.setRequestProperty(
                    "Authorization",
                    "Basic "
                            + new String(Base64.encode((username + ":"
                            + password).getBytes(), Base64.NO_WRAP)));
            con.setConnectTimeout(10 * 1000); // 10 seconds
            con.connect();
            if (con.getResponseCode() == 200) {
                StringBuilder result = new StringBuilder();
                BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                System.out.println(result.toString());
                new JSONArray(result.toString());
                return LoginStatus.OK;
            } else if (con.getResponseCode() >= 401 && con.getResponseCode() <= 403) {
                return LoginStatus.AUTH_FAILED;
            } else {
                return LoginStatus.SERVER_FAILED;
            }
        } catch (MalformedURLException e) {
            Log.e(NotesClientUtil.class.getSimpleName(), "Exception", e);
            return LoginStatus.CONNECTION_FAILED;
        } catch (IOException e) {
            Log.e(NotesClientUtil.class.getSimpleName(), "Exception", e);
            return LoginStatus.CONNECTION_FAILED;
        } catch (JSONException e) {
            Log.e(NotesClientUtil.class.getSimpleName(), "Exception", e);
            return LoginStatus.JSON_FAILED;
        }
    }

    /**
     * Pings a server and checks if there is a installed ownCloud instance
     *
     * @param url String URL to server
     * @return true if there is a installed instance, false if not
     */
    public static boolean isValidURL(CustomCertManager ccm, String url) {
        StringBuilder result = new StringBuilder();
        try {
            HttpURLConnection con = SupportUtil.getHttpURLConnection(ccm, url + "status.php");
            con.setRequestMethod(NotesClient.METHOD_GET);
            con.setConnectTimeout(10 * 1000); // 10 seconds
            BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            JSONObject response = new JSONObject(result.toString());
            return response.getBoolean("installed");
        } catch (IOException | JSONException | NullPointerException e) {
            return false;
        }
    }

}