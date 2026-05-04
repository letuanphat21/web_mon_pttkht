package org.example.webquanao.db;

import org.jdbi.v3.core.Jdbi;

public class DBConnect {
    private static Jdbi jdbi;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = DBProperties.get("db.url");
            String username = DBProperties.get("db.username");
            String password = DBProperties.get("db.password");

            jdbi = Jdbi.create(url, username, password);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Jdbi get() {
        return jdbi;
    }
}
