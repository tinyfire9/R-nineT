package com.RnineT.Auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

public class DropboxToken extends Token {
    public DropboxToken(String code){
        this.url = "https://api.dropboxapi.com/oauth2/token";
        this.clientID = "gyk9ex16zbrt706";
        this.clientSecret = System.getenv("R_NINET_DROPBOX_CLIENT_SECRET");
        this.redirectURI = "https://localhost:3000?auth-drive=dropbox";
        this.code = code;
        this.fetchToken();
    }

    public void fetchToken(){
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("client_id", clientID);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectURI);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity(body, headers);
        ResponseEntity<String> responseEntity = rest.postForEntity(URI.create(url), request, String.class);

        try {
            JsonNode node = new ObjectMapper().readTree(responseEntity.getBody());

            this.accessToken = node.get("access_token").asText();
            this.tokenType = node.get("token_type").asText();
            this.expiresAt = -1L;
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
