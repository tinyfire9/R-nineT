package com.RnineT.Auth;

abstract public class Token {
    protected String url;
    protected String clientID;
    protected String clientSecret;
    protected String code;
    protected String redirectURI;
    protected String accessToken;
    protected String refreshToken;
    protected String tokenType;
    protected int expiresIn;
    protected Long expiresAt;

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public Long getExpiresAt() {  return expiresAt; }

    abstract void fetchToken();
}