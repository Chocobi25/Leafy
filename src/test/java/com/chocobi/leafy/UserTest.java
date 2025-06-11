package com.chocobi.leafy;

import com.chocobi.leafy.user.domain.User;
import com.chocobi.leafy.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest // JPA 관련 컴포넌트만 로드하여 테스트
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test") // test 프로필을 사용하도록 설정
public class UserTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("유저 정보 수정 후 재조회 시 새로운 데이터가 생성되지 않는 지 확인")
    void updateUserTest() {
        // 1. given: 테스트용 유저 생성 및 저장 (최초 로그인 상황 시뮬레이션)
        Long testKakaoId = 12345L;
        User user = User.builder()
                .kakaoId(testKakaoId)
                .nickname("초코비")
                .profileImageUrl("http://example.com/profile.jpg")
                .build();

        userRepository.save(user);

        // 2. when: 유저 정보 수정 (닉네임 변경 시뮬레이션)
        User foundUser = userRepository.findByKakaoId(testKakaoId).orElseThrow();
        foundUser.updateNickname("초코비2");
        userRepository.save(foundUser);

        // 3. then: 결과 확인
        // kakaoId로 다시 조회
        User updatedUser = userRepository.findByKakaoId(testKakaoId).orElseThrow();

        // 닉네임 변경 확인
        assertThat(updatedUser.getNickname()).isEqualTo("초코비2");

        // 전체 유저 수가 1명인지 확인 (새로운 유저가 생성되지 않았는지)
        assertThat(userRepository.findAll().size()).isEqualTo(1);

        // ID가 동일한지 확인(같은 row가 수정된 것인지)
        assertThat(updatedUser.getId()).isEqualTo(foundUser.getId());

        System.out.println("기존 유저 ID: " + foundUser.getId() + ", 닉네임 : " + foundUser.getNickname());
        System.out.println("수정된 유저 ID: " + updatedUser.getId() + ", 닉네임 : " + updatedUser.getNickname());
    }
}
