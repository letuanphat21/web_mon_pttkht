package org.example.webquanao.db;

import java.io.InputStream;
import java.util.Properties;

public class DBProperties {
    private static Properties props = new Properties();

    static {
        try {
            InputStream input = DBProperties.class
                    .getClassLoader()
                    .getResourceAsStream("db.properties");

            props.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }

    public static void main(String[] args) {
        System.out.println(props);
    }
}
