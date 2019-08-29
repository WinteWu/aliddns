package com.wintewu.aliddns.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * query IP
 *
 * @author WinteWu
 */
public class IPUtils {

    /**
     * 获取IP地址的服务，只要能返回纯地址即可
     */
    private static final List<String> URLS = new ArrayList<>();

    static {
        URLS.add("https://whatismyip.wintewu.com1");
        URLS.add("https://ifconfig.me/ip");
        URLS.add("http://members.3322.org/dyndns/getip");
    }

    private IPUtils() {
    }

    public static String getIP() {
        for (String urlStr : URLS) {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), Charset.forName("UTF-8")))) {
                    return in.readLine();
                }
            } catch (IOException ignored) {
            }
        }
        return null;
    }
}
