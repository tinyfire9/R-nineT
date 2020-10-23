package com.RnineT.Auth;

import com.RnineT.Transfer.Drives.Box.Box;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Date;

public class BoxToken extends Token{
    public BoxToken(String code){
        this.url = "https://api.box.com/oauth2/token";
        this.clientID = "inothb10fvq4yopnj2bzhh9khnawl4f5";
        this.clientSecret = System.getenv("R_NINET_BOX_CLIENT_SECRET");
        this.code = code;
        this.fetchToken();
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
            this.expiresAt = new Date().getTime() + (this.expiresIn * 1000);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
