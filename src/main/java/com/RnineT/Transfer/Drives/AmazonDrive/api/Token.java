package com.RnineT.Transfer.Drives.AmazonDrive.api;

public class Token {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private int expiresIn;

    public Token(String accessToken, String refreshToken, String tokenType, int expiresIn){
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

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
}
