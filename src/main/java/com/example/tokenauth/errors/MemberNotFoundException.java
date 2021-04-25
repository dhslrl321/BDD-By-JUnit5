package com.example.tokenauth.errors;

public class MemberNotFoundException extends RuntimeException{
    public MemberNotFoundException(Long id) {
        super("member not found: " + id);
    }
}
