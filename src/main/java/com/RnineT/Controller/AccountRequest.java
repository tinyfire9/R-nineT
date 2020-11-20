package com.RnineT.Controller;

import com.RnineT.Account.Account;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class AccountRequest {
    private String email;
    private String password;
    private String token;

    @JsonProperty("email")
    public void setEmail (String email){
        this.email = email;
    }

    @JsonProperty("password")
    public void setPassword(String password){
        this.password = password;
    }

    @JsonProperty("token")
    public void setToken(String token){
        this.token = token;
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
