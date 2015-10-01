package it.niedermann.owncloud.notes.util;

import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Utils for Validation etc
 * Created by stefan on 25.09.15.
 */
public class NotesClientUtil {

    /**
     * Checks if the given url String starts with http:// or https://
     *
     * @param url String
     * @return true, if the given String is only http
     */
    public static boolean isHttp(String url) {
        return url.length() > 4 && url.startsWith("http") && url.charAt(4) != 's';
    }

    /**
     * Checks if the given URL returns a valid status code and sets the Check next to the URL-Input Field to visible.
     * @param urlStr String URL
     * @return URL is valid
     */
    public static boolean isValidURL(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1000 * 10); // mTimeout is in seconds
            urlc.connect();
            if (urlc.getResponseCode() == 200) {
                Log.v("Note", "ResponseCode: " + urlc.getResponseCode());
                return true;
            } else {
                return false;
            }
        } catch (MalformedURLException e1) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     *
     * @param url String
     * @param username String
     * @param password String
     * @return Username and Password are a valid Login-Combination for the given URL.
     */
    public static boolean isValidLogin(String url, String username, String password) {
        try {
            String targetURL = url + "index.php/apps/notes/api/v0.2/notes";
            Log.v("Note", targetURL);
            HttpURLConnection con = (HttpURLConnection) new URL(targetURL)
                    .openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty(
                    "Authorization",
                    "Basic "
                            + new String(Base64.encode((username + ":"
                            + password).getBytes(), Base64.NO_WRAP)));
            con.setConnectTimeout(10 * 1000); // 10 seconds
            con.connect();
            if (con.getResponseCode() == 200) {
                return true;
            }
        } catch (MalformedURLException e1) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return false;
    }

}