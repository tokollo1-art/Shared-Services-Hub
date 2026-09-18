package com.ssh.dto.response;

/**
 * Auth Response DTO
 *
 * Purpose: Carries authentication results back to the frontend after login/register.
 *
 * WHY SSH needs this DTO:
 *   - Returns the JWT token to the client
 *   - Returns safe user info (never the password)
 *   - Includes token metadata (type, expiration) for the frontend
 *   - Used by AuthController for both login and register responses
 *
 * TODO: Add refreshToken field if refresh token flow is added
 * TODO: Add "issuedAt" timestamp for debugging
 *
 * TDD: Write AuthResponseTest first
 *       - shouldExposeTokenAndUser()
 *       - shouldDefaultToBearerTokenType()
 *       - shouldNotExposePassword()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
public class AuthResponse {

    private String token;

    private String tokenType = "Bearer";

    private long expiresIn;

    private UserResponse user;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public AuthResponse() {}

    public AuthResponse(String token, long expiresIn, UserResponse user) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    // ============================================
    // TO STRING (EXCLUDES TOKEN AND USER)
    // ============================================

    @Override
    public String toString() {
        return "AuthResponse{" +
            "tokenType='" + tokenType + '\'' +
            ", expiresIn=" + expiresIn +
            '}';
    }
}
