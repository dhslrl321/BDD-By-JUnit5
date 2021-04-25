package com.example.tokenauth.errors;

public class LoginFailException extends RuntimeException{
    public LoginFailException(String email) {
        super(email);
    }
}
