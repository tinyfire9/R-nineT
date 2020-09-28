package com.RnineT.Auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.simple.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

public class Token {
    private String url;
    private String clientID;
    private String clientSecret;
    private String code;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private int expiresIn;

    public Token(String url, String clientID, String clientSecret, String code){
        this.url = url;
        this.clientID = clientID;
        this.clientSecret = clientSecret;
        this.code = code;
        this.fetchToken();
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

    public void fetchToken(){
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        JSONObject body = new JSONObject();

        headers.setContentType(MediaType.APPLICATION_JSON);
        body.put("grant_type", "authorization_code");
        body.put("code", code);
        body.put("client_id", clientID);
        body.put("client_secret", clientSecret);

        HttpEntity<String> request = new HttpEntity<String>(body.toJSONString(), headers);
        ResponseEntity<String> responseEntity = rest.postForEntity(URI.create(url), request, String.class);

        try {
            JsonNode node = new ObjectMapper().readTree(responseEntity.getBody());

            this.accessToken = node.get("access_token").asText();
            this.refreshToken = node.get("refresh_token").asText();
            this.tokenType = node.get("token_type").asText();
            this.expiresIn = node.get("expires_in").asInt();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}