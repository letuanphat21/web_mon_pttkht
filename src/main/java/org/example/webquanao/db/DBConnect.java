package org.example.webquanao.db;

import org.jdbi.v3.core.Jdbi;

public class DBConnect {
    private static Jdbi jdbi;

    private static void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = DBProperties.get("db.url");
            String username = DBProperties.get("db.username");
            String password = DBProperties.get("db.password");

            if (url == null) throw new RuntimeException("Cấu hình DB URL không tìm thấy");

            jdbi = Jdbi.create(url, username, password);
            System.out.println("Kết nối Database thành công");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi kết nối DB: " + e.getMessage());
        }
    }

    public static Jdbi get() {
        if (jdbi == null) {
            connect();
        }
        return jdbi;
    }
}