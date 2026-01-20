package project.food.domain.post.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import project.food.domain.member.entity.Member;
import project.food.domain.post.entity.Post;
import project.food.global.config.JpaConfig;


import java.math.BigDecimal;
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
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Member testMember1;
    private Member testMember2;

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

        // 영속성 컨텍스트에 저장 및 DB 반영
        entityManager.persist(testMember1);
        entityManager.persist(testMember2);
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
        @DisplayName("게시글 ID로 조회 - 존재하지 않는 ID")
        void findById_Success() {
            // Given: 게시글 저장
            Post post = createPost("조회 테스트", testMember1);
            Post savedPost = entityManager.persist(post);
            entityManager.flush();
            Long postId = savedPost.getId();

            // When: ID로 게시글 조회
            Optional<Post> foundPost = postRepository.findById(postId);

            // Then: 게시글이 조화되어야 함
            assertThat(foundPost).isPresent();
            assertThat(foundPost.get().getId()).isEqualTo(postId);
            assertThat(foundPost.get().getTitle()).isEqualTo("조회 테스트");
        }

        @Test
        @DisplayName("게시글 ID로 조회 - 존재하지 않는 ID")
        void findBtId_NotFound() {
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
                    "수정된 레스토랑",
                    "수정된 주소",
                    "중식",
                    BigDecimal.valueOf(5.0),
                    "https://example.com/new.jpg"
            );
            entityManager.flush();
            entityManager.clear();

            // Then: 수정된 내용이 DB에 반영되어야 함
            Post updatedPost = postRepository.findById(savedPost.getId()).orElseThrow();
            assertThat(updatedPost.getTitle()).isEqualTo("수정된 제목");
            assertThat(updatedPost.getContent()).isEqualTo("수정된 내용");
            assertThat(updatedPost.getRestaurantName()).isEqualTo("수정된 레스토랑");
            assertThat(updatedPost.getFoodCategory()).isEqualTo("중식");
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
     * 3. 음식 카테고리별 조회 테스트
     */
    @Nested
    @DisplayName("음식 카테고리별 조회 테스트")
    class FindByFoodCategoryTest {

        @Test
        @DisplayName("특정 카테고리의 게시글 조회 - 성공")
        void findByFoodCategory_Success() {
            // Given: 다양한 카테고리의 게시글 생성
            entityManager.persist(createPostWithCategory("한식 맛집1", "한식", testMember1));
            entityManager.persist(createPostWithCategory("한식 맛집2", "한식", testMember2));
            entityManager.persist(createPostWithCategory("중식 맛집", "중식", testMember1));
            entityManager.persist(createPostWithCategory("일식 맛집", "일식", testMember2));
            entityManager.flush();

            // When: "한식" 카테고리 게시글 조회
            List<Post> koreanPosts = postRepository.findByFoodCategory("한식");

            // Then: 한식 게시글 2개만 조회
            assertThat(koreanPosts).hasSize(2);
            assertThat(koreanPosts)
                    .extracting(Post::getFoodCategory)
                    .containsOnly("한식");
            assertThat(koreanPosts)
                    .extracting(Post::getTitle)
                    .containsExactlyInAnyOrder("한식 맛집1", "한식 맛집2");
        }

        @Test
        @DisplayName("특정 카테고리의 게시글 조회 - 결과 없음")
        void findByFoodCategory_Empty() {
            // Given: "한식" 카테고리만 있는 상태
            entityManager.persist(createPostWithCategory("한식 맛집", "한식", testMember1));
            entityManager.flush();

            // When: "양식" 카테고리 조회
            List<Post> westernPosts = postRepository.findByFoodCategory("양식");

            // Then: 빈 리스트 반환

            assertThat(westernPosts).isEmpty();
        }

        @Test
        @DisplayName("카테고리가 null인 게시글 조회")
        void findByFoodCategory_Null() {
            // Given: 카테고리가 null인 게시글
            Post postWithoutCategory = createPost("카테고리 없음", testMember1);
            // foodCategory는 null (기본값)
            entityManager.persist(postWithoutCategory);
            entityManager.flush();

            // When: null로 조회
            List<Post> postsWithNullCategory = postRepository.findByFoodCategory(null);

            // Then: 카테고리 null인 게시글 조회
            assertThat(postsWithNullCategory).hasSize(1);
            assertThat(postsWithNullCategory.get(0).getFoodCategory()).isNull();

        }

        @Test
        @DisplayName("대소문자 구분 확인, 공백")
        void findByFoodCategory_CaseSensitive() {
            // Given: "한식" 카테고리 게시글
            entityManager.persist(createPostWithCategory("한식 맛집", "한식", testMember1));
            entityManager.flush();

            // When: 대소문자, 공백 다르게 조회 (실제로는 한글이라 큰 의미 없지만 테스트 목적)
            List<Post> exactMatch = postRepository.findByFoodCategory("한식");
            List<Post> differentCase = postRepository.findByFoodCategory("한식 ");

            // Then: 정확히 일치하는 경우만 조회
            assertThat(exactMatch).hasSize(1);
            assertThat(differentCase).hasSize(1);
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
        @DisplayName("제목 키워드 검색 - 대소문자 구분 없음")
        void findByTitleContaining_CaseInsensitive() {
            // Given: 영어 제목 게시글
            entityManager.persist(createPost("Best Restaurant in Seoul", testMember1));
            entityManager.persist(createPost("restaurant guide", testMember2));
            entityManager.flush();

            // When: 소문자로 검색
            List<Post> lowerCaseSearch = postRepository.findByTitleContaining("restaurant");

            // When: 대문자로 검색
            List<Post> upperCaseSearch = postRepository.findByTitleContaining("Restaurant");

            // Then: 대소문자 구분 없이 조회 (MySQL은 기본적으로 대소문자 구분 없음)
            assertThat(lowerCaseSearch).hasSize(2);
            assertThat(upperCaseSearch).hasSize(2);
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

            Post newstPost = createPost("가장 최신 게시글", testMember2);
            entityManager.persist(newstPost);
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

        /**
         * 6. 복잡 조건 테스트
         */
        @Nested
        @DisplayName("복합 조건 테스트")
        class ComPlexQueryTest {

            @Test
            @DisplayName("특정 회원의 특정 카테고리 게시글 조회")
            void findByMemberIdAndCategory() {
                // Given: 다양한 조합의 게시글 생성
                entityManager.persist(createPostWithCategory("회원1-한식", "한식", testMember1));
                entityManager.persist(createPostWithCategory("회원1-중식", "중식", testMember1));
                entityManager.persist(createPostWithCategory("회원2-한식", "중식", testMember2));
                entityManager.flush();

                // When: 회원1의 게시글 중 한식만 필터링
                List<Post> member1Posts = postRepository.findByMemberId(testMember1.getId());
                List<Post> member1KoreanPosts = member1Posts.stream()
                        .filter(post -> "한식".equals(post.getFoodCategory()))
                        .toList();

                // Then: 회원1의 한식 게시글 1개만 조회
                assertThat(member1KoreanPosts).hasSize(1);
                assertThat(member1KoreanPosts.get(0).getTitle()).isEqualTo("회원1-한식");
            }

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
                .restaurantName("테스트 레스토랑")
                .restaurantAddress("서울시 강남구")
                .rating(BigDecimal.valueOf(4.5))
                .viewCount(0)
                .build();
    }

    /**
     * 테스트용 게시글 생성 헬퍼 메서드 (카테고리 포함)
     */
    private Post createPostWithCategory(String title, String category, Member member) {
        return Post.builder()
                .member(member)
                .title(title)
                .content("테스트 내용")
                .restaurantName("테스트 레스토랑")
                .restaurantAddress("서울시 강남구")
                .foodCategory(category)
                .rating(BigDecimal.valueOf(4.5))
                .viewCount(0)
                .build();

    }
}
