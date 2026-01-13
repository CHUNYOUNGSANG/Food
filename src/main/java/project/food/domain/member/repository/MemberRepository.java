package project.food.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.food.domain.member.entity.Member;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 이메일로 회원 조회
     * @param email 조회할 회원의 이메일
     * @return 이메일로 조회된 회원 정보
     */
    Optional<Member> findByEmail(String email);

    /**
     * 닉네임으로 회원 조회
     * @param nickname 조회할 회원의 닉네임
     * @return 닉네임으로 조회된 회원 정보
     */
    Optional<Member> findByNickname(String nickname);

    /**
     * 이메일 중복 확인
     * @param email 중복 확인할 이메일
     * @return 이메일 존재 여부
     */
    boolean existsByEmail(String email);

    /**
     * 닉네임 중복 확인
     * @param nickname 중복 확인할 닉네임
     * @return 닉네임 존재 여부
     */
    boolean existsByNickname(String nickname);
}
