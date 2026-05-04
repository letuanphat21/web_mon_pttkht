package org.example.webquanao.dao;

import org.example.webquanao.db.DBConnect;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

public class UserRoleDAO {
    private Jdbi jdbi = DBConnect.get();

    public void addRoleToUser(int userId, int roleId) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                INSERT INTO user_roles(user_id, role_id)
                VALUES(:uid, :rid)
            """)
                        .bind("uid", userId)
                        .bind("rid", roleId)
                        .execute()
        );
    }
    // 2. lấy tất cả role theo userId
    public List<String> getRolesByUserId(int userId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT r.name
                        FROM user_roles ur
                        JOIN roles r ON ur.role_id = r.id
                        WHERE ur.user_id = :uid
                """)
                        .bind("uid", userId)
                        .mapTo(String.class)
                        .list()
        );
    }
}
