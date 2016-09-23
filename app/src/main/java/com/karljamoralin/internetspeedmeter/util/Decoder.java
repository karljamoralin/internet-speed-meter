package com.karljamoralin.internetspeedmeter.util;

/**
 * Created by jamorali on 9/24/2016.
 */

public class Decoder {

    public static String decode (String base64EncodedPublicKey) {

        // Get byte sequence to play with.
        byte[] bytes = base64EncodedPublicKey.getBytes();

        // Swap upper and lower case letters.
        for (int i = 0; i < bytes.length; i++) {
            if(bytes[i] >= 'A' && bytes[i] <= 'Z')
                bytes[i] = (byte)( 'a' + (bytes[i] - 'A'));
            else if(bytes[i] >= 'a' && bytes[i] <= 'z')
                bytes[i] = (byte)( 'A' + (bytes[i] - 'a'));
        }

        return new String(bytes);

    }
}
