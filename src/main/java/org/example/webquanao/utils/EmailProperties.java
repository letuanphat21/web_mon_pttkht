package org.example.webquanao.utils;

import java.io.InputStream;
import java.util.Properties;

public class EmailProperties {
    private static Properties props = new Properties();

    static {
        try {
            InputStream input = EmailProperties.class
                    .getClassLoader()
                    .getResourceAsStream("email.properties");

            props.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}
