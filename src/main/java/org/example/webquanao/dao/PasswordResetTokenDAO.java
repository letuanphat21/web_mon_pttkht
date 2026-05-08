package org.example.webquanao.dao;

import org.example.webquanao.db.DBConnect;
import org.example.webquanao.entity.PasswordResetToken;
import org.jdbi.v3.core.Jdbi;

import java.sql.Timestamp;

public class PasswordResetTokenDAO {
    private final Jdbi jdbi = DBConnect.get();

    public int insert(int userId, String tokenHash, Timestamp expiresAt, String requestIp, String userAgent) {
        return jdbi.withHandle(handle ->
                handle.createUpdate("""
                        INSERT INTO password_reset_tokens(user_id, token_hash, expires_at, request_ip, user_agent)
                        VALUES(:userId, :tokenHash, :expiresAt, :requestIp, :userAgent)
                """)
                        .bind("userId", userId)
                        .bind("tokenHash", tokenHash)
                        .bind("expiresAt", expiresAt)
                        .bind("requestIp", requestIp)
                        .bind("userAgent", userAgent)
                        .executeAndReturnGeneratedKeys("id")
                        .mapTo(int.class)
                        .one()
        );
    }

    public PasswordResetToken findValidByHash(String tokenHash) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT id, user_id, token_hash, expires_at, used_at, created_at, request_ip, user_agent
                        FROM password_reset_tokens
                        WHERE token_hash = :tokenHash
                          AND used_at IS NULL
                          AND expires_at > NOW()
                        LIMIT 1
                """)
                        .bind("tokenHash", tokenHash)
                        .map((rs, ctx) -> {
                            PasswordResetToken token = new PasswordResetToken();
                            token.setId(rs.getInt("id"));
                            token.setUserId(rs.getInt("user_id"));
                            token.setTokenHash(rs.getString("token_hash"));
                            token.setExpiresAt(rs.getTimestamp("expires_at"));
                            token.setUsedAt(rs.getTimestamp("used_at"));
                            token.setCreatedAt(rs.getTimestamp("created_at"));
                            token.setRequestIp(rs.getString("request_ip"));
                            token.setUserAgent(rs.getString("user_agent"));
                            return token;
                        })
                        .findOne()
                        .orElse(null)
        );
    }

    public PasswordResetToken findValidByUserIdAndHash(int userId, String tokenHash) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT id, user_id, token_hash, expires_at, used_at, created_at, request_ip, user_agent
                        FROM password_reset_tokens
                        WHERE user_id = :userId
                          AND token_hash = :tokenHash
                          AND used_at IS NULL
                          AND expires_at > NOW()
                        ORDER BY created_at DESC
                        LIMIT 1
                """)
                        .bind("userId", userId)
                        .bind("tokenHash", tokenHash)
                        .map((rs, ctx) -> {
                            PasswordResetToken token = new PasswordResetToken();
                            token.setId(rs.getInt("id"));
                            token.setUserId(rs.getInt("user_id"));
                            token.setTokenHash(rs.getString("token_hash"));
                            token.setExpiresAt(rs.getTimestamp("expires_at"));
                            token.setUsedAt(rs.getTimestamp("used_at"));
                            token.setCreatedAt(rs.getTimestamp("created_at"));
                            token.setRequestIp(rs.getString("request_ip"));
                            token.setUserAgent(rs.getString("user_agent"));
                            return token;
                        })
                        .findOne()
                        .orElse(null)
        );
    }

    public void markUsed(int id) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                        UPDATE password_reset_tokens
                        SET used_at = NOW()
                        WHERE id = :id
                """)
                        .bind("id", id)
                        .execute()
        );
    }

    public void markAllUnusedByUserAsUsed(int userId) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                        UPDATE password_reset_tokens
                        SET used_at = NOW()
                        WHERE user_id = :userId
                          AND used_at IS NULL
                """)
                        .bind("userId", userId)
                        .execute()
        );
    }

    public int countRecentRequests(int userId, String requestIp, Timestamp fromTime) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT COUNT(*)
                        FROM password_reset_tokens
                        WHERE created_at >= :fromTime
                          AND (user_id = :userId OR request_ip = :requestIp)
                """)
                        .bind("fromTime", fromTime)
                        .bind("userId", userId)
                        .bind("requestIp", requestIp)
                        .mapTo(int.class)
                        .one()
        );
    }

    public void deleteByUserId(int userId) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                        DELETE FROM password_reset_tokens
                        WHERE user_id = :userId
                """)
                        .bind("userId", userId)
                        .execute()
        );
    }
}
