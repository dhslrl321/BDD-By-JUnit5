package com.example.tokenauth.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long memberId;
    @Getter
    private RoleType roleType;

    public Role(Long memberId, RoleType roleType) {
        this.memberId = memberId;
        this.roleType = roleType;
    }

    public Role(RoleType roleType) {
        this(null, roleType);
    }

}
