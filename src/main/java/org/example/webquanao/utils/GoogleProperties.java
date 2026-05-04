package org.example.webquanao.utils;

import org.example.webquanao.controller.GoogleController;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GoogleProperties {
    private static final Properties props = new Properties();

    static {
        try (InputStream input = GoogleController.class
                .getClassLoader()
                .getResourceAsStream("gg.properties")) {

            if (input == null) {
                throw new RuntimeException("Cannot find gg.properties");
            }

            props.load(input);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String CLIENT_ID =
            props.getProperty("google.client.id");

    private static final String CLIENT_SECRET =
            props.getProperty("google.client.secret");

    private static final String REDIRECT_URI =
            props.getProperty("google.redirect.uri");

    public static String getClientId() {
        return CLIENT_ID;
    }

    public static String getClientSecret() {
        return CLIENT_SECRET;
    }

    public static String getRedirectUri() {
        return REDIRECT_URI;
    }
}
