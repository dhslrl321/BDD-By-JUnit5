package com.example.tokenauth.service;

import com.example.tokenauth.domain.dto.MemberModificationData;
import com.example.tokenauth.domain.dto.MemberRequestSignUpData;
import com.example.tokenauth.domain.dto.MemberResponseData;
import com.example.tokenauth.domain.entity.Member;
import com.example.tokenauth.domain.entity.Role;
import com.example.tokenauth.domain.entity.RoleType;
import com.example.tokenauth.domain.repository.MemberRepository;
import com.example.tokenauth.domain.repository.RoleRepository;
import com.example.tokenauth.errors.MemberEmailDuplicationException;
import com.example.tokenauth.errors.MemberNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public MemberService(MemberRepository memberRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.memberRepository = memberRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    /**
     * 유저를 등록한다.
     *
     * @param signUpData : 회원가입 요청 데이터
     * @return 회원가입된 유저
     * @throw 이메일 중복 예외
     */
    public MemberResponseData signUp(MemberRequestSignUpData signUpData) {

        String email = signUpData.getEmail();

        if(memberRepository.existsByEmail(email)) {
            throw new MemberEmailDuplicationException(email);
        }

        Member savedMember = memberRepository.save(modelMapper.map(signUpData, Member.class));
        savedMember.changePassword(signUpData.getPassword(), passwordEncoder);

        roleRepository.save(new Role(savedMember.getId(), RoleType.USER));

        return modelMapper.map(savedMember, MemberResponseData.class);
    }

    /**
     * 특정 회원을 조회한다.
     *
     * @param id 회원 번호
     * @return 조회된 회원
     * @throw 존재하지 않는 회원 예외
     */
    public MemberResponseData getMember(Long id) {
        Member member = findMember(id);
        return modelMapper.map(member, MemberResponseData.class);
    }

    /**
     * 모든 회원을 조회한다.
     *
     * @param pageable : 페이징 정보
     * @return 모든 회원
     */
    public Page<MemberResponseData> getMembers(Pageable pageable) {
        return memberRepository.findAll(pageable).map(member -> MemberResponseData.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .build());
    }

    /**
     * 회원의 이메일을 받아서 존재하는 회원인지를 확인한다.
     *
     * @param email 존재 여부를 조회할 이메일
     * @return 존재하지 않는다면 false
     * @throw 이메일 중복 에러
     */
    public boolean isExistsEmail(String email) {
        if(memberRepository.existsByEmail(email)) {
            throw new MemberEmailDuplicationException(email);
        }
        return false;
    }

    /**
     * 회원 정보를 수정한다.
     *
     * @param parsedId 토큰으로부터 복호된 요청 사용자의 정보
     * @param memberModificationData 정보 변경에 필요한 데이터
     * @param targetId 정보를 수정할 대상 ID
     * @return 수정된 사용자의 닉네임과 이메일 데이터
     * @throw AccessDeniedException
     */
    public MemberResponseData modify(Long parsedId,
                                     MemberModificationData memberModificationData,
                                     Long targetId) {
        if(!targetId.equals(parsedId)) {
            throw new AccessDeniedException("Access Denied");
        }

        Member member = findMember(parsedId);
        member.changeNicknameFrom(memberModificationData);

        Member modifiedMember = memberRepository.save(member);

        return modelMapper.map(modifiedMember, MemberResponseData.class);
    }

    private Member findMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }
}
