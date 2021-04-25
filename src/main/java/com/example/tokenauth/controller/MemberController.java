package com.example.tokenauth.controller;

import com.example.tokenauth.domain.dto.ErrorResponse;
import com.example.tokenauth.domain.dto.MemberModificationData;
import com.example.tokenauth.domain.dto.MemberRequestSignUpData;
import com.example.tokenauth.domain.dto.MemberResponseData;
import com.example.tokenauth.security.MemberAuthentication;
import com.example.tokenauth.service.MemberService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/api/members", produces = "application/json; charset=utf-8")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<MemberResponseData> signUp(@RequestBody
                                                         @Valid MemberRequestSignUpData memberRequestSignUpData) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.signUp(memberRequestSignUpData));
    }

    @GetMapping("/exists/{email}")
    public ResponseEntity<ErrorResponse> exists(@PathVariable String email) {
        memberService.isExistsEmail(email);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated() and hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<MemberResponseData> modify(@PathVariable("id") Long targetId,
                                                     @RequestBody @Valid
                                                             MemberModificationData memberModificationData,
                                                     MemberAuthentication memberAuthentication) {
        Long parsedId = memberAuthentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.OK)
                .body(memberService.modify(targetId, memberModificationData, parsedId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated() and hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<MemberResponseData> getMember(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(memberService.getMember(id));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated() and hasAnyAuthority('ADMIN')")
    public ResponseEntity<Slice<MemberResponseData>> getMembers(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(memberService.getMembers(pageable));
    }
}
