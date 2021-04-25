package com.example.tokenauth.errors;

public class MemberEmailDuplicationException extends RuntimeException {
    public MemberEmailDuplicationException(String email) {
        super(email + " 는 이미 존재하는 Email 입니다.");
    }
}
