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
import project.food.global.exception.CustomException;
import project.food.global.exception.ErrorCode;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 회원가입
     */
    public MemberResponseDto signUp(MemberRequestDto requestDto) {
        log.info("회원가입 시도: email = {}", requestDto.getEmail());

        // 이메일 중복 확인
        if (memberRepository.existsByEmail(requestDto.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 닉네임 중복 확인
        if (memberRepository.existsByNickname(requestDto.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
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

    /**
     * 회원 정보 조회
     */
    public MemberResponseDto getMember(Long id) {
        log.info("회원 조회: id = {}", id);

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberResponseDto.from(member);
    }

    /**
     * 이메일로 회원 조회
     */
    public MemberResponseDto getMemberByEmail(String email) {
        log.info("이메일로 회원 조회: email = {}", email);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberResponseDto.from(member);
    }

    /**
     * 회원 정보 수정
     * @return
     */
    @Transactional
    public MemberResponseDto updateMember(Long id, MemberUpdateDto updateDto) {
        log.info("회원 정보 수정: id = {}", id);

        // 회원 조회
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 닉네임 변경 시 중복 확인
        if (!member.getNickname().equals(updateDto.getNickname())) {
            if (memberRepository.existsByNickname(updateDto.getNickname())) {
                throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
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
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 새 비밀번호 암호화 및 변경
        String encodedPassword = newPassword;
        member.updatePassword(newPassword);

        log.info("비밀번호 변경 완료: id = {}", id);
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public void deleteMember(Long id) {
        log.info("회원 탈퇴: id = {}", id);

        // 회원 조회
        if (!memberRepository.existsById(id)) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // 회원 삭제
        memberRepository.deleteById(id);

        log.info("회원 탈퇴 완료: id = {}", id);
    }

    /**
     * 이메일 중복 확인
     */
    public boolean isEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }

    /**
     * 닉네임 중복 확인
     */
    public boolean isNicknameDuplicate(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }
}
