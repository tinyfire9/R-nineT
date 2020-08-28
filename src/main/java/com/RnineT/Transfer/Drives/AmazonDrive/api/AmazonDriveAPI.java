package com.RnineT.Transfer.Drives.AmazonDrive.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

public class AmazonDriveAPI {
    private Token token;

    public AmazonDriveAPI(String authorizationCode){
        this.token = getToken(authorizationCode);
    }

    private Token getToken(String authorizationCode){
        String tokenURI = "https://api.amazon.com/auth/o2/token";
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        JSONObject body = new JSONObject();

        headers.setContentType(MediaType.APPLICATION_JSON);

        body.put("grant_type", "authorization_code");
        body.put("code", authorizationCode);
        body.put("client_id", "amzn1.application-oa2-client.a9d7ba10e4e94869a109449b17676473");
        body.put("client_secret", "efe3cced1ea3271cc9ef6a6c3d39b1bdeaeb1fcf9380c0d02044e41598904849");
        body.put("redirect_uri", "http://localhost:8080/amz-auth-token");

        HttpEntity<String> request = new HttpEntity<String>(body.toJSONString(), headers);
        ResponseEntity<String> responseEntity = rest.postForEntity(URI.create(tokenURI), request, String.class);

        try {
            JsonNode node = new ObjectMapper().readTree(responseEntity.getBody());
            return new Token(
                    node.get("access_token").asText(),
                    node.get("refresh_token").asText(),
                    node.get("token_type").asText(),
                    node.get("expires_in").asInt()
            );
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

}
