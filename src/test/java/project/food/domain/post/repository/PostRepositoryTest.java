package project.food.domain.post.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import project.food.domain.member.entity.Member;
import project.food.domain.post.entity.Post;
import project.food.domain.restaurant.entity.Restaurant;
import project.food.global.config.JpaConfig;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PostRepository 테스트
 *
 * 목적: PostRepository의 모든 쿼리 메서드가 올바르게 동작하는지 검증
 *
 * @DataJpaTest 어노테이션:
 * - JPA 관련 컴포넌트만 로드 (빠른 테스트
 * - 각 테스트 메서드 실행 후 자동 롤백 (데이터 독립성 보장)
 * - TestEntityManager 제공 (테스트 데이터 준비용)
 *
 * - 테스트 구조:
 * - @Nested를 사용하여 기능별로 그룹화
 * - Given-When-Then 패턴 사용
 * - 성공 케이스와 실패 케이스 모두 테스트
 */
@DataJpaTest
@DisplayName("PostRepository 테스트")
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(JpaConfig.class)
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Member testMember1;
    private Member testMember2;
    private Restaurant testRestaurant;

    /**
     * 각 테스트 실행 전에 공통으로 필요한 데이터 준비
     *
     * @BeforeEach: 모든 @Test 메서드 실행 전에 자동 실행
     */
    @BeforeEach
    void setUp() {
        // Given: 테스트용 회원 2명 생성
        testMember1 = createMember("test1@example.com", "테스터1", "닉네임1");
        testMember2 = createMember("test2@example.com", "테스터2", "닉네임2");

        // 테스트용 음식점 생성
        testRestaurant = Restaurant.builder()
                .sourceId("test-source-1")
                .name("테스트 레스토랑")
                .address("서울시 강남구")
                .category("한식")
                .latitude(37.1234)
                .longitude(127.1234)
                .build();

        // 영속성 컨텍스트에 저장 및 DB 반영
        entityManager.persist(testMember1);
        entityManager.persist(testMember2);
        entityManager.persist(testRestaurant);
        entityManager.flush();
        entityManager.clear();      // 영속성 컨텍스트 초기화 (캐시 제거)
    }

    /**
     * 1. 기본 CRUD 테스트
     */
    @Nested
    @DisplayName("기본 CRUD 테스트")
    class CrudTest {

        @Test
        @DisplayName("게시글 저장 - 성공")
        void save_Success() {
            // Given: 새로운 게시글 생성
            Post post = createPost("테스트 게시글", testMember1);

            // When: 게시글 저장
            Post savedPost = postRepository.save(post);

            // Then: 저장 후 ID가 생성되어야 함
            assertThat(savedPost.getId()).isNotNull();
            assertThat(savedPost.getTitle()).isEqualTo("테스트 게시글");
            assertThat(savedPost.getMember().getId()).isEqualTo(testMember1.getId());
        }

        @Test
        @DisplayName("게시글 ID로 조회 - 성공")
        void findById_Success() {
            // Given: 게시글 저장
            Post post = createPost("조회 테스트", testMember1);
            Post savedPost = entityManager.persist(post);
            entityManager.flush();
            Long postId = savedPost.getId();

            // When: ID로 게시글 조회
            Optional<Post> foundPost = postRepository.findById(postId);

            // Then: 게시글이 조회되어야 함
            assertThat(foundPost).isPresent();
            assertThat(foundPost.get().getId()).isEqualTo(postId);
            assertThat(foundPost.get().getTitle()).isEqualTo("조회 테스트");
        }

        @Test
        @DisplayName("게시글 ID로 조회 - 존재하지 않는 ID")
        void findById_NotFound() {
            // Given: 존재하지 않는 ID
            Long invalidId = 99999L;

            // When: 존재하지 않는 ID로 조회
            Optional<Post> foundPost = postRepository.findById(invalidId);

            // Then: 빈 Optional 반환
            assertThat(foundPost).isEmpty();
        }

        @Test
        @DisplayName("게시글 수정 - 성공")
        void update_Success() {
            // Given: 게시글 저장
            Post post = createPost("원본 제목", testMember1);
            Post savedPost = entityManager.persist(post);
            entityManager.flush();
            entityManager.clear();

            // When: 게시글 수정
            Post foundPost = postRepository.findById(savedPost.getId()).orElseThrow();
            foundPost.updatePost(
                    "수정된 제목",
                    "수정된 내용",
                    5.0
            );
            entityManager.flush();
            entityManager.clear();

            // Then: 수정된 내용이 DB에 반영되어야 함
            Post updatedPost = postRepository.findById(savedPost.getId()).orElseThrow();
            assertThat(updatedPost.getTitle()).isEqualTo("수정된 제목");
            assertThat(updatedPost.getContent()).isEqualTo("수정된 내용");
            assertThat(updatedPost.getRating()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("게시글 삭제 - 성공")
        void delete_Success() {
            // Given: 게시글 저장
            Post post = createPost("삭제할 게시글", testMember1);
            Post savedPost = entityManager.persist(post);
            entityManager.flush();
            Long postId = savedPost.getId();

            // When: 게시글 삭제
            postRepository.delete(savedPost);
            entityManager.flush();

            // Then: 삭제 후 조회되지 않아야 함
            Optional<Post> deletedPost = postRepository.findById(postId);
            assertThat(deletedPost).isEmpty();
        }

        @Test
        @DisplayName("전체 게시글 개수 조회 - 성공")
        void count_Success() {
            // Given: 게시글 3개 저장
            entityManager.persist(createPost("게시글 1", testMember1));
            entityManager.persist(createPost("게시글 2", testMember1));
            entityManager.persist(createPost("게시글 3", testMember2));
            entityManager.flush();

            // When: 전체 게시글 개수 조회
            long count = postRepository.count();

            // Then: 3개가 조회되어야 함
            assertThat(count).isEqualTo(3);
        }
    }

    /**
     * 2. 회원별 게시글 조회 테스트
     */
    @Nested
    @DisplayName("회원별 게시글 조회 테스트")
    class FindByMemberIdTest {

        @Test
        @DisplayName("특정 회원이 작성한 게시글 목록 조회 - 성공")
        void findByMemberId_Success() {
            // Given: testMember1이 2개, testMember2가 1개 게시글 작성
            entityManager.persist(createPost("회원1의 게시글1", testMember1));
            entityManager.persist(createPost("회원1의 게시글2", testMember1));
            entityManager.persist(createPost("회원2의 게시글", testMember2));
            entityManager.flush();

            // When: testMember1의 게시글 조회
            List<Post> member1Posts = postRepository.findByMemberId(testMember1.getId());

            // Then: testMember1의 게시글 2개만 조회되어야 함
            assertThat(member1Posts).hasSize(2);
            assertThat(member1Posts)
                    .extracting(Post::getTitle)
                    .containsExactlyInAnyOrder("회원1의 게시글1", "회원1의 게시글2");
            assertThat(member1Posts)
                    .allMatch(post -> post.getMember().getId().equals(testMember1.getId()));
        }

        @Test
        @DisplayName("특정 회원이 작성한 게시글 목록 조회 - 게시글 없음")
        void findByMemberId_Empty() {
            // Given: testMember1이 게시글을 작성하지 않은 상태

            // When: testMember1의 게시글 조회
            List<Post> posts = postRepository.findByMemberId(testMember1.getId());

            // Then: 빈 리스트 반환
            assertThat(posts).isEmpty();
        }

        @Test
        @DisplayName("여러 회원의 게시글 구분 조회 - 성공")
        void findByMemberId_MultipleMembers() {
            // Given: 두 회원이 각각 게시글 작성
            entityManager.persist(createPost("회원1-게시글A", testMember1));
            entityManager.persist(createPost("회원2-게시글A", testMember2));
            entityManager.persist(createPost("회원1-게시글B", testMember1));
            entityManager.persist(createPost("회원2-게시글B", testMember2));
            entityManager.flush();

            // When: 각 회원의 게시글 조회
            List<Post> member1Posts = postRepository.findByMemberId(testMember1.getId());
            List<Post> member2Posts = postRepository.findByMemberId(testMember2.getId());

            // Then: 각자 2개씩 조회되어야 함
            assertThat(member1Posts).hasSize(2);
            assertThat(member2Posts).hasSize(2);
            assertThat(member1Posts).extracting(Post::getTitle)
                    .containsExactlyInAnyOrder("회원1-게시글A", "회원1-게시글B");
            assertThat(member2Posts).extracting(Post::getTitle)
                    .containsExactlyInAnyOrder("회원2-게시글A", "회원2-게시글B");
        }
    }

    /**
     * 3. 음식점별 게시글 조회 테스트
     */
    @Nested
    @DisplayName("음식점별 게시글 조회 테스트")
    class FindByRestaurantIdTest {

        @Test
        @DisplayName("특정 음식점의 게시글 조회 - 성공")
        void findByRestaurantId_Success() {
            // Given: 같은 음식점에 대한 게시글 2개 생성
            entityManager.persist(createPost("맛집 리뷰1", testMember1));
            entityManager.persist(createPost("맛집 리뷰2", testMember2));
            entityManager.flush();

            // When: 해당 음식점의 게시글 조회
            org.springframework.data.domain.Pageable pageable =
                    org.springframework.data.domain.PageRequest.of(0, 10);
            var posts = postRepository.findByRestaurant_IdOrderByCreatedAtDesc(
                    testRestaurant.getId(), pageable);

            // Then: 2개 조회
            assertThat(posts.getContent()).hasSize(2);
        }

        @Test
        @DisplayName("음식점이 없는 게시글 조회")
        void findByRestaurantIsNull() {
            // Given: 음식점 없는 게시글 생성
            Post postWithoutRestaurant = Post.builder()
                    .member(testMember1)
                    .title("음식점 없는 게시글")
                    .content("테스트 내용")
                    .rating(4.0)
                    .viewCount(0)
                    .build();
            entityManager.persist(postWithoutRestaurant);
            entityManager.persist(createPost("음식점 있는 게시글", testMember1));
            entityManager.flush();

            // When: 음식점이 null인 게시글 조회
            org.springframework.data.domain.Pageable pageable =
                    org.springframework.data.domain.PageRequest.of(0, 10);
            var posts = postRepository.findByRestaurantIsNull(pageable);

            // Then: 1개만 조회
            assertThat(posts.getContent()).hasSize(1);
            assertThat(posts.getContent().get(0).getTitle()).isEqualTo("음식점 없는 게시글");
        }
    }

    /**
     * 4. 제목 검색 테스트
     */
    @Nested
    @DisplayName("제목 키워드 검색 테스트")
    class FindByTitleContainingTest {

        @Test
        @DisplayName("제목에 키워드 포함된 게시글 검색 - 성공")
        void findByTitleContaining_Success() {
            // Given: 다양한 제목의 게시글 생성
            entityManager.persist(createPost("강남 최고 맛집", testMember1));
            entityManager.persist(createPost("홍대 분위기 좋은 맛집", testMember2));
            entityManager.persist(createPost("강남 숨은 맛집", testMember1));
            entityManager.persist(createPost("강남역 근처 카페", testMember2));
            entityManager.flush();

            // When: "강남"이 포함된 게시글 검색
            List<Post> posts = postRepository.findByTitleContaining("강남");

            // Then: "강남"이 포함된 게시글 3개 조회
            assertThat(posts).hasSize(3);
            assertThat(posts)
                    .extracting(Post::getTitle)
                    .allMatch(title -> title.contains("강남"));
        }

        @Test
        @DisplayName("제목 키워드 검색 - 빈 문자열")
        void findByTitleContaining_EmptyString() {
            // Given: 게시글 2개 생성
            entityManager.persist(createPost("게시글 1", testMember1));
            entityManager.persist(createPost("게시글 2", testMember2));
            entityManager.flush();

            // When: 빈 문자열로 검색
            List<Post> posts = postRepository.findByTitleContaining("");

            // Then: 모든 게시글 조회 (빈 문자열은 모두 문자열에 포함)
            assertThat(posts).hasSize(2);
        }

        @Test
        @DisplayName("제목 키워드 검색 - 정확한 키워드 매칭")
        void findByTitleContaining_ExactKeyword() {
            // Given: 영어 제목 게시글
            entityManager.persist(createPost("Best Restaurant in Seoul", testMember1));
            entityManager.persist(createPost("restaurant guide", testMember2));
            entityManager.persist(createPost("cafe review", testMember1));
            entityManager.flush();

            // When: "Restaurant" 키워드로 검색
            List<Post> searchResults = postRepository.findByTitleContaining("Restaurant");

            // Then: "Restaurant"이 포함된 게시글 조회
            assertThat(searchResults).isNotEmpty();
            assertThat(searchResults)
                    .extracting(Post::getTitle)
                    .allMatch(title -> title.contains("Restaurant"));
        }

        @Test
        @DisplayName("제목 키워드 검색 - 부분 일치")
        void findByTitleContaining_PartialMatch() {
            entityManager.persist(createPost("맛집 추천", testMember1));
            entityManager.persist(createPost("맛집탐방", testMember2));
            entityManager.persist(createPost("숨은 맛집", testMember1));
            entityManager.flush();

            // When: "맛집" 검색
            List<Post> posts = postRepository.findByTitleContaining("맛집");

            // Then: "맛집"이 포함된 모든 게시글 조회
            assertThat(posts).hasSize(3);
        }
    }

    /**
     * 5. 최신순 정렬 조회 테스트
     */
    @Nested
    @DisplayName("최신순 정렬 조회 테스트")
    class FindAllByOrderByCreatedAtDescTest {

        @Test
        @DisplayName("최신순으로 게시글 정렬 조회 - 성공")
        void findAllByOrderByCreatedAtDesc_Success() throws InterruptedException {
            // Given: 시간 차이를 두고 게시글 생성
            Post oldPost = createPost("오래된 게시글", testMember1);
            entityManager.persist(oldPost);
            entityManager.flush();

            Thread.sleep(100);      // 시간 차이 생성 (100ms 대기)

            Post recentPost = createPost("최신 게시글", testMember1);
            entityManager.persist(recentPost);
            entityManager.flush();

            Thread.sleep(100);

            Post newestPost = createPost("가장 최신 게시글", testMember2);
            entityManager.persist(newestPost);
            entityManager.flush();

            // When: 최신순으로 조회
            List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();

            // Then: 가장 최신 게시글이 먼저 나와야 함
            assertThat(posts).hasSize(3);
            assertThat(posts.get(0).getTitle()).isEqualTo("가장 최신 게시글");
            assertThat(posts.get(1).getTitle()).isEqualTo("최신 게시글");
            assertThat(posts.get(2).getTitle()).isEqualTo("오래된 게시글");
        }

        @Test
        @DisplayName("최신순 정렬 조회 - 게시글 없음")
        void findAllByOrderByCreatedAtDesc_Empty() {
            // Given: 게시글이 없는 상태

            // When: 최신순 조회
            List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();

            // Then: 빈 리스트 반환
            assertThat(posts).isEmpty();
        }

        @Test
        @DisplayName("최신순 정렬 조회 - 같은 시간에 생성된 게시글")
        void findAllByOrderByCreatedAtDesc_SameTime() {
            // Given: 거의 동시에 여러 게시글 생성
            Post post1 = createPost("게시글 1", testMember1);
            Post post2 = createPost("게시글 2", testMember1);
            Post post3 = createPost("게시글 3", testMember2);

            entityManager.persist(post1);
            entityManager.persist(post2);
            entityManager.persist(post3);
            entityManager.flush();

            // When: 최신순 조회
            List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();

            // Then: 3개 모두 조회 (순서는 DB가 결정)
            assertThat(posts).hasSize(3);
        }
    }

    /**
     * 6. 복합 조건 테스트
     */
    @Nested
    @DisplayName("복합 조건 테스트")
    class ComplexQueryTest {

        @Test
        @DisplayName("키워드 검색 후 최신순 정렬")
        void searchAndSort() throws InterruptedException {
            // Given: "맛집" 키워드가 포함된 게시글 시간차 생성
            Post oldPost = createPost("강남 맛집", testMember1);
            entityManager.persist(oldPost);
            entityManager.flush();

            Thread.sleep(100);

            Post newPost = createPost("홍대 맛집", testMember2);
            entityManager.persist(newPost);
            entityManager.flush();

            // When: "맛집" 검색 후 최신순 정렬
            List<Post> searchResults = postRepository.findByTitleContaining("맛집");
            List<Post> sortedResults = searchResults.stream()
                    .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                    .toList();

            // Then: 최신 게시글이 먼저
            assertThat(sortedResults).hasSize(2);
            assertThat(sortedResults.get(0).getTitle()).isEqualTo("홍대 맛집");
            assertThat(sortedResults.get(1).getTitle()).isEqualTo("강남 맛집");
        }

        @Test
        @DisplayName("특정 회원의 특정 음식점 게시글 조회")
        void findByMemberAndRestaurant() {
            // Given: 다른 음식점 생성
            Restaurant anotherRestaurant = Restaurant.builder()
                    .sourceId("test-source-2")
                    .name("다른 레스토랑")
                    .address("서울시 홍대")
                    .category("중식")
                    .latitude(37.5678)
                    .longitude(127.5678)
                    .build();
            entityManager.persist(anotherRestaurant);
            entityManager.flush();

            // 회원1 - testRestaurant 게시글
            Post post1 = createPost("회원1-레스토랑1", testMember1);
            entityManager.persist(post1);

            // 회원1 - anotherRestaurant 게시글
            Post post2 = Post.builder()
                    .member(testMember1)
                    .title("회원1-레스토랑2")
                    .content("테스트 내용")
                    .restaurant(anotherRestaurant)
                    .rating(4.0)
                    .viewCount(0)
                    .build();
            entityManager.persist(post2);

            // 회원2 - testRestaurant 게시글
            Post post3 = createPost("회원2-레스토랑1", testMember2);
            entityManager.persist(post3);
            entityManager.flush();

            // When: 회원1의 게시글 중 testRestaurant 것만 필터링
            List<Post> member1Posts = postRepository.findByMemberId(testMember1.getId());
            List<Post> member1Restaurant1Posts = member1Posts.stream()
                    .filter(post -> testRestaurant.getId().equals(
                            post.getRestaurant() != null ? post.getRestaurant().getId() : null))
                    .toList();

            // Then: 회원1의 testRestaurant 게시글 1개만 조회
            assertThat(member1Restaurant1Posts).hasSize(1);
            assertThat(member1Restaurant1Posts.get(0).getTitle()).isEqualTo("회원1-레스토랑1");
        }
    }

    /**
     * 테스트용 회원 생성 헬퍼 메서드
     */
    private Member createMember(String email, String name, String nickname) {
        return Member.builder()
                .email(email)
                .password("qwer1234")
                .name(name)
                .nickname(nickname)
                .build();
    }

    /**
     * 테스트용 게시글 생성 헬퍼 메서드
     */
    private Post createPost(String title, Member member) {
        return Post.builder()
                .member(member)
                .title(title)
                .content("테스트 내용")
                .restaurant(testRestaurant)
                .rating(4.5)
                .viewCount(0)
                .build();
    }
}
