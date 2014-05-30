package com._37coins.merchant;

import org.apache.http.HttpResponse;

/**
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
public class Util {

    public static boolean isSucceed(HttpResponse response) {
        return response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300;
    }

    public static String toLowerCase(String str) {
        return !str.contains("%") ? str.toLowerCase() : str;
    }


}
