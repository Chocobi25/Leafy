package com.chocobi.leafy.User;

import com.chocobi.leafy.user.Entity.User;
import com.chocobi.leafy.user.repository.UserRepository;
import com.chocobi.leafy.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test") // test 프로필을 사용하도록 설정
public class UserTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @Test
    @DisplayName("유저 업데이트")
    void updateUserTest() {
        // 1. given: 테스트용 유저 생성 및 저장 (최초 로그인 상황 시뮬레이션)
        Long testKakaoId = 12345L;
        String nickname = "초코비";
        String profileImageUrl = "http://example.com/profile.jpg";
        User user = User.builder()
                .kakaoId(testKakaoId)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .build();

        userService.saveOrGetUser(testKakaoId, nickname, profileImageUrl);

        // 2. when: 유저 정보 수정 (닉네임 변경 시뮬레이션)
        String newNickname = "초코비2";
        User newUser = User.builder()
                .kakaoId(testKakaoId)
                .nickname(newNickname)
                .profileImageUrl(profileImageUrl)
                .build();
        userService.editUser(newUser);

        // 3. then: 결과 확인
        // kakaoId로 다시 조회
        User updatedUser = userService.saveOrGetUser(testKakaoId, newNickname, profileImageUrl);

        // 닉네임 변경 확인
        assertThat(updatedUser.getNickname()).isEqualTo("초코비2");

        // 전체 유저 수가 1명인지 확인 (새로운 유저가 생성되지 않았는지)
        assertThat(userRepository.findAll().size()).isEqualTo(1);

        System.out.println("기존 유저 ID: " + user.getKakaoId() + ", 닉네임 : " + user.getNickname());
        System.out.println("수정된 유저 ID: " + updatedUser.getKakaoId() + ", 닉네임 : " + updatedUser.getNickname());
    }
}
