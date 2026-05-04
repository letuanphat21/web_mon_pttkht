package org.example.webquanao.dao;

import org.example.webquanao.db.DBConnect;
import org.example.webquanao.entity.Category;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

public class CategoryDAO {

    private Jdbi jdbi = DBConnect.get();

    // lấy tất cả (cả active + inactive)
    public List<Category> findAll() {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT * FROM categories")
                        .mapToBean(Category.class)
                        .list()
        );
    }

    // chỉ lấy category đang active
    public List<Category> findAllActive() {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT * FROM categories
                        WHERE active = 1
                """)
                        .mapToBean(Category.class)
                        .list()
        );
    }

    // tìm theo id
    public Category findById(int id) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT * FROM categories WHERE id = :id")
                        .bind("id", id)
                        .mapToBean(Category.class)
                        .findOne()
                        .orElse(null)
        );
    }
    public Category findByName(String name) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT * FROM categories WHERE name = :name")
                        .bind("name", name)
                        .mapToBean(Category.class)
                        .findOne()
                        .orElse(null)
        );
    }

    // insert
    public int insert(Category c) {
        return jdbi.withHandle(handle ->
                handle.createUpdate("""
                        INSERT INTO categories(name, active)
                        VALUES(:name, :active)
                """)
                        .bindBean(c)
                        .executeAndReturnGeneratedKeys("id")
                        .mapTo(int.class)
                        .one()
        );
    }

    // update
    public void update(Category c) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                        UPDATE categories
                        SET name = :name,
                            active = :active
                        WHERE id = :id
                """)
                        .bindBean(c)
                        .execute()
        );
    }

    //  soft delete (KHÔNG xóa thật nữa)
    public void deactivate(int id) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                        UPDATE categories
                        SET active = 0
                        WHERE id = :id
                """)
                        .bind("id", id)
                        .execute()
        );
    }

    // kích hoạt lại
    public void activate(int id) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                        UPDATE categories
                        SET active = 1
                        WHERE id = :id
                """)
                        .bind("id", id)
                        .execute()
        );
    }
}