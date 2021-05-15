package com.example.tokenauth.domain.entity;

import com.example.tokenauth.domain.dto.MemberModificationData;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter @Builder @Setter
@AllArgsConstructor @NoArgsConstructor
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;
    private String nickname;

    public void changePassword(String password,
                                PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(password);
    }

    public void changeNicknameFrom(MemberModificationData memberModificationData) {
        this.nickname = memberModificationData.getNickname();
    }

    public boolean authenticate(String password,
                                PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(password, this.password);
    }
}
