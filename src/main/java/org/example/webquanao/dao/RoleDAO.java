package org.example.webquanao.dao;

import org.example.webquanao.db.DBConnect;
import org.example.webquanao.entity.Role;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

public class RoleDAO {
    private Jdbi jdbi = DBConnect.get();

    public List<Role> findAll() {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT *
                        FROM roles
                        ORDER BY id
                """)
                        .mapToBean(Role.class)
                        .list()
        );
    }

    public Role findByName(String name) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT * FROM roles WHERE name = :name")
                        .bind("name", name)
                        .mapToBean(Role.class)
                        .findOne()
                        .orElse(null)
        );
    }

    public List<Role> getRolesByUser(int userId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT r.*
                FROM roles r
                JOIN user_roles ur ON r.id = ur.role_id
                WHERE ur.user_id = :uid
            """)
                        .bind("uid", userId)
                        .mapToBean(Role.class)
                        .list()
        );
    }

    public static void main(String[] args) {
        RoleDAO dao = new RoleDAO();
        System.out.println(dao.findByName("admin"));
    }
}
