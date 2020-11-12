package com.RnineT.Controller;

import com.RnineT.Account.Account;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class AccountRequest {
    private String email;
    private String password;
    private String token;

    @JsonProperty("email")
    public void mapEmail (Map<String, String> req){
        this.email = req.get("email");
    }

    @JsonProperty("password")
    public void mapPassword(Map<String, String> req){
        this.password = req.get("password");
    }

    @JsonProperty("token")
    public void mapToken(Map<String, String> req){
        try{
            this.token = req.get("token");
        } catch (Exception e){

        }
    }

    public String getEmail(){
        return this.email;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }
}
