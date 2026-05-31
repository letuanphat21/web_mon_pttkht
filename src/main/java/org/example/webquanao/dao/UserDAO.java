package org.example.webquanao.dao;

import org.example.webquanao.db.DBConnect;
import org.example.webquanao.entity.User;
import org.jdbi.v3.core.Jdbi;

import java.util.Date;
import java.util.List;

public class UserDAO {
    private Jdbi jdbi = DBConnect.get();

    public List<User> findAll() {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT *
                        FROM users
                        ORDER BY id DESC
                """)
                        .mapToBean(User.class)
                        .list()
        );
    }

    public User findById(int id) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT * FROM users WHERE id = :id")
                        .bind("id", id)
                        .mapToBean(User.class)
                        .findOne()
                        .orElse(null)
        );
    }

    public User findByEmail(String email) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT * FROM users WHERE email = :email")
                        .bind("email", email)
                        .mapToBean(User.class)
                        .findOne()
                        .orElse(null)
        );
    }
//    public User findByUsername(String username) {
//        return jdbi.withHandle(handle ->
//                handle.createQuery("SELECT * FROM users WHERE username = :username")
//                        .bind("username", username)
//                        .mapToBean(User.class)
//                        .findOne()
//                        .orElse(null)
//        );
//    }

    public User findByGoogleId(String googleId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT * FROM users WHERE google_id = :gid")
                        .bind("gid", googleId)
                        .mapToBean(User.class)
                        .findOne()
                        .orElse(null)
        );
    }

    public int insertUser(User user) {
        return jdbi.withHandle(handle ->
                handle.createUpdate("""
                INSERT INTO users(email, password, full_name, avatar,code_active)
                VALUES(:email,  :password, :fullName, :avatar,:codeActive)
            """)
                        .bindBean(user)
                        .executeAndReturnGeneratedKeys("id")
                        .mapTo(int.class)
                        .one()
        );
    }

    public int insertManagedUser(User user) {
        return jdbi.withHandle(handle ->
                handle.createUpdate("""
                        INSERT INTO users(email, password, full_name, phone, address, active, verified)
                        VALUES(:email, :password, :fullName, :phone, :address, :active, :verified)
                """)
                        .bindBean(user)
                        .executeAndReturnGeneratedKeys("id")
                        .mapTo(int.class)
                        .one()
        );
    }

    public void updateManagedUser(User user) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                        UPDATE users
                        SET email = :email,
                            full_name = :fullName,
                            phone = :phone,
                            address = :address,
                            active = :active,
                            verified = :verified
                        WHERE id = :id
                """)
                        .bindBean(user)
                        .execute()
        );
    }

    public void updateManagedUserWithPassword(User user) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                        UPDATE users
                        SET email = :email,
                            password = :password,
                            full_name = :fullName,
                            phone = :phone,
                            address = :address,
                            active = :active,
                            verified = :verified
                        WHERE id = :id
                """)
                        .bindBean(user)
                        .execute()
        );
    }

    public void updateActive(int id, boolean active) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                        UPDATE users
                        SET active = :active
                        WHERE id = :id
                """)
                        .bind("active", active)
                        .bind("id", id)
                        .execute()
        );
    }

    public void updatePasswordAndUsername(User user) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
            UPDATE users
            SET password = :password,
                username = :username
            WHERE id = :id
        """)
                        .bindBean(user)
                        .execute()
        );
    }

    public int insertGoogleUser(User user) {
        return jdbi.withHandle(handle ->
                handle.createUpdate("""
                INSERT INTO users(email,password, google_id, full_name,active,verified)
                VALUES(:email, :password,:googleId, :fullName, :active,:verified)
            """)
                        .bindBean(user)
                        .executeAndReturnGeneratedKeys("id")
                        .mapTo(int.class)
                        .one()
        );
    }

    public void updateGoogleId(int userId, String googleId) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                UPDATE users 
                SET google_id = :gid 
                WHERE id = :id
            """)
                        .bind("gid", googleId)
                        .bind("id", userId)
                        .execute()
        );
    }

    public void updateCodeActive(User user) {
        jdbi.useHandle(handle -> handle.createUpdate("""
        UPDATE users
        SET verified = :active
        WHERE email = :email
    """)
                .bind("email",user.getEmail())
                .bind("active", user.isVerified())
                .execute());
    }
    public void updateFailedAttemptsAndLock(int userId, int attempts, Date lockUntil) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
            UPDATE users
            SET failed_attempts = :attempts,
                lock_until = :lockUntil
            WHERE id = :id
        """)
                        .bind("attempts", attempts)
                        .bind("lockUntil", lockUntil)
                        .bind("id", userId)
                        .execute()
        );
    }
    public void updateFailedAttempts(int userId, int attempts) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
            UPDATE users
            SET failed_attempts = :attempts
            WHERE id = :id
        """)
                        .bind("attempts", attempts)
                        .bind("id", userId)
                        .execute()
        );
    }

    public void updatePassword(int userId, String hashedPassword) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                        UPDATE users
                        SET password = :password,
                            failed_attempts = 0,
                            lock_until = NULL
                        WHERE id = :id
                """)
                        .bind("password", hashedPassword)
                        .bind("id", userId)
                        .execute()
        );
    }

    public static void main(String[] args) {
//        UserDAO userDAO = new UserDAO();
//        User user = userDAO.findByUsername("admin");
//        System.out.println(user);
    }
}
