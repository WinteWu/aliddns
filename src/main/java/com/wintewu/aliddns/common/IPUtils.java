package com.wintewu.aliddns.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * query IP
 * @author wintewu
 */
public class IPUtils {
    private static final String URL = "http://members.3322.org/dyndns/getip";

    private IPUtils(){}

    public static String getIP() {
        BufferedReader in = null;
        try {
            URL url = new URL(URL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), Charset.forName("UTF-8")));
            return in.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
