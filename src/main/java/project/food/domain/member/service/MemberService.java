package project.food.domain.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.food.domain.member.dto.MemberRequestDto;
import project.food.domain.member.dto.MemberResponseDto;
import project.food.domain.member.dto.MemberUpdateDto;
import project.food.domain.member.entity.Member;
import project.food.domain.member.repository.MemberRepository;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    // 회원가입
    public MemberResponseDto signUp(MemberRequestDto requestDto) {
        log.info("회원가입 시도: email = {}", requestDto.getEmail());

        // 이메일 중복 확인
        if (memberRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 닉네임 중복 확인
        if (memberRepository.existsByNickname(requestDto.getNickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = requestDto.getPassword();

        // Entity 생성 및 저장
        Member member = requestDto.toEntity(encodedPassword);
        Member savedMember = memberRepository.save(member);

        log.info("회원가입 완료: id = {}, email = {}", savedMember.getId(), savedMember.getEmail());

        // ResponseDTO 변환 후 반환
        return MemberResponseDto.from(savedMember);
    }

    // 회원 정보 조회
    public MemberResponseDto getMember(Long id) {
        log.info("회원 조회: id = {}", id);

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        return MemberResponseDto.from(member);
    }

    // 이메일로 회원 조회
    public MemberResponseDto getMemberByEmail(String email) {
        log.info("이메일로 회원 조회: email = {}", email);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원 이메일입니다."));

        return MemberResponseDto.from(member);
    }

    // 회원 정보 수정
    @Transactional
    public MemberResponseDto updateMember(Long id, MemberUpdateDto updateDto) {
        log.info("회원 정보 수정: id = {}", id);

        // 회원 조회
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 닉네임 변경 시 중복 확인
        if (!member.getNickname().equals(updateDto.getNickname())) {
            if (memberRepository.existsByNickname(updateDto.getNickname())) {
                throw new IllegalArgumentException("중복된 닉네임입니다.");
            }
        }

        // 회원 정보 수정
        member.updateProfile(
                updateDto.getName(),
                updateDto.getNickname(),
                updateDto.getProfileImage()
        );

        log.info("회원 정보 수정 완료: id = {}, nickname = {}", id, updateDto.getNickname());

        return MemberResponseDto.from(member);
    }

    // 비밀번호 변경
    @Transactional
    public void updatePassword(Long id, String oldPassword, String newPassword) {
        log.info("비밀번호 변경: id = {}", id);

        // 회원 조회
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 새 비밀번호 암호화 및 변경
        String encodedPassword = newPassword;
        member.updatePassword(newPassword);

        log.info("비밀번호 변경 완료: id = {}", id);
    }

    // 회원 탈퇴
    @Transactional
    public void deleteMember(Long id) {
        log.info("회원 탈퇴: id = {}", id);

        // 회원 조회
        if (!memberRepository.existsById(id)) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }

        // 회원 삭제
        memberRepository.deleteById(id);

        log.info("회원 탈퇴 완료: id = {}", id);
    }

    // 이메일 중복 확인
    public boolean isEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }

    // 닉네임 중복 확인
    public boolean isNicknameDuplicate(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }
}
