package project.food.domain.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.food.domain.comment.entity.Comment;
import project.food.domain.comment.repository.CommentRepository;
import project.food.domain.like.commentlike.repository.CommentLikeRepository;
import project.food.domain.like.postlike.repository.PostLikeRepository;
import project.food.domain.member.dto.*;
import project.food.domain.member.entity.Member;
import project.food.domain.member.repository.MemberRepository;
import project.food.domain.post.entity.Post;
import project.food.domain.post.repository.PostRepository;
import project.food.global.exception.CustomException;
import project.food.global.exception.ErrorCode;
import project.food.global.file.dto.UploadedFileInfo;
import project.food.global.file.service.FileStorageService;
import project.food.global.jwt.JwtTokenProvider;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final FileStorageService fileStorage;

    /**
     * 회원가입
     */
    @Transactional
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
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        String profileImageUrl = null;
        if (requestDto.getProfileImage() != null && !requestDto.getProfileImage().isEmpty()) {
            UploadedFileInfo fileInfo = fileStorage.saveProfileImage(requestDto.getProfileImage());
            profileImageUrl = fileInfo.getFileUrl();
        }

        // Entity 생성 시 URL 전달
        Member member = requestDto.toEntity(encodedPassword, profileImageUrl);
        Member savedMember = memberRepository.save(member);

        log.info("회원가입 완료: id = {}, email = {}", savedMember.getId(), savedMember.getEmail());

        // ResponseDTO 변환 후 반환
        return MemberResponseDto.from(savedMember);
    }

    /**
     * 로그인
     * @param requestDto
     * @return 토큰 및 회원 정보
     */
    @Transactional
    public LoginResponseDto login(LoginRequestDto requestDto) {
        log.info("로그인 시도: email = {}", requestDto.getEmail());

        // 이메일로 회원 조회
        Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            log.warn("비밀번호 불일치: email = {}", requestDto.getEmail());
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(
                member.getId(), member.getEmail(), member.getRole().getKey());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        log.info("로그인 성공: id = {}, email = {}", member.getId(), member.getEmail());

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .member(MemberResponseDto.from(member))
                .build();
    }

    /**
     * 토큰 재발급
     */
    public LoginResponseDto refreshToken(TokenRefreshRequestDto requestDto) {
        log.info("토큰 재발급 시도");

        if (!jwtTokenProvider.validateToken(requestDto.getRefreshToken())) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        Long memberId = jwtTokenProvider.getMemberIdFromToken(requestDto.getRefreshToken());

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getEmail(), member.getRole().getKey());

        String refreshToken = jwtTokenProvider.createRefreshToken(memberId);

        log.info("토큰 재발급 완료 : memberId = {}", memberId);

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .member(MemberResponseDto.from(member))
                .build();
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
     * 전체 회원 조회
     */
    public List<MemberResponseDto> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(MemberResponseDto::from)
                .toList();
    }

    /**
     * 회원 정보 수정
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

        String profileImageUrl = member.getProfileImage();
        if (updateDto.getProfileImage() != null && !updateDto.getProfileImage().isEmpty()) {
            UploadedFileInfo fileInfo = fileStorage.saveProfileImage(updateDto.getProfileImage());
            profileImageUrl = fileInfo.getFileUrl();
        }


        // 회원 정보 수정
        member.updateProfile(
                updateDto.getName(),
                updateDto.getNickname(),
                profileImageUrl
        );


        log.info("회원 정보 수정 완료: id = {}, nickname = {}", id, updateDto.getNickname());

        return MemberResponseDto.from(member);
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void updatePassword(Long id, String oldPassword, String newPassword) {
        log.info("비밀번호 변경: id = {}", id);

        // 회원 조회
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 기존 비밀번호 검증
        if (!passwordEncoder.matches(oldPassword, member.getPassword())) {
            log.warn("기존 비밀번호 불일치: id = {}", id);
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // 새 비밀번호 암호화 및 변경
        String encodedPassword = passwordEncoder.encode(newPassword);
        member.updatePassword(encodedPassword);

        log.info("비밀번호 변경 완료: id = {}", id);
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public void deleteMember(Long id, Long requesterId) {
        log.info("회원 탈퇴: id = {}", id);

        // 회원 조회
        if (!memberRepository.existsById(id)) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        Member requester = memberRepository.findById(requesterId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (!id.equals(requesterId) && !requester.isAdmin()) {
            log.warn("회원 삭제 권한 없음: targetId = {}, requesterId = {}", id, requesterId);
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 회원이 누른 게시글/댓글 좋아요 삭제
        postLikeRepository.deleteByMemberId(id);
        commentLikeRepository.deleteByMemberId(id);

        // 회원의 댓글에 다른 사람이 누른 좋아요 삭제
        commentLikeRepository.deleteByCommentMemberId(id);

        // 회원 게시글의 댓글에 달린 좋아요 + 게시글 좋아요 삭제
        List<Post> posts = postRepository.findByMemberId(id);
        for  (Post post : posts) {
            for (Comment comment : post.getComments()) {
                commentLikeRepository.deleteByCommentId(comment.getId());
            }
            postLikeRepository.deleteByPostId(post.getId());
        }

        // 회원 게시글 삭제 (cascade: post_image, post_tag, comments 자동 삭제)
        postRepository.deleteAll(posts);

        // 다른 게시글에 작성한 회원의 댓글 삭제
        commentRepository.deleteById(id);

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
