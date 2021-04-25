package com.example.tokenauth.domain.repository;

import com.example.tokenauth.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findAllByMemberId(Long memberId);
}
