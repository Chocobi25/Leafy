package com.chocobi.leafy.user.presentation;

import com.chocobi.leafy.user.infra.entity.enums.Level;
import com.chocobi.leafy.user.infra.entity.UserEntity;
import com.chocobi.leafy.user.dto.LevelIconUpdateDto;
import com.chocobi.leafy.user.dto.NicknameUpdateDto;
import com.chocobi.leafy.user.dto.UserProfileDto;
import com.chocobi.leafy.user.dto.UserTripDto;
import com.chocobi.leafy.user.infra.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long kakaoId = (Long) authentication.getPrincipal();

        UserProfileDto userProfileDto = userService.getUserProfile(kakaoId);

        return ResponseEntity.ok(userProfileDto);
    }

    @GetMapping("/trips")
    public ResponseEntity<List<UserTripDto>> getUserTrips() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long kakaoId = (Long) authentication.getPrincipal();
        
        List<UserTripDto> userTrips = userService.getUserTrips(kakaoId);
        return ResponseEntity.ok(userTrips);
    }

    @PutMapping("/nickname")
    public ResponseEntity<String> updateNickname(@RequestBody NicknameUpdateDto nicknameUpdateDto, Authentication authentication) {
        Long kakaoId = Long.parseLong(authentication.getName());

        // 기존 사용자 정보 조회
        UserEntity userEntity = userService.findById(kakaoId);  // TODO: 로직 동작 확인

        // 닉네임 수정
        userEntity.updateNickname(nicknameUpdateDto.getNickname());

        userService.editUser(userEntity);

        return ResponseEntity.ok("닉네임이 성공적으로 수정되었습니다.");
    }

    @PutMapping("/level-icon")
    public ResponseEntity<String> updateLevelIcon(@Valid @RequestBody LevelIconUpdateDto levelIconUpdateDto, Authentication authentication) {
        Long kakaoId = Long.parseLong(authentication.getName());
        userService.updateSelectedLevelIcon(kakaoId, levelIconUpdateDto.getSelectedLevelIcon());
        return ResponseEntity.ok("레벨 아이콘이 성공적으로 변경되었습니다.");
    }

    // 테스트용 레벨 변경 엔드포인트 (개발 중에만 사용)
    @PutMapping("/test/level")
    public ResponseEntity<String> updateTestLevel(@RequestBody Map<String, String> request, Authentication authentication) {
        Long kakaoId = Long.parseLong(authentication.getName());
        UserEntity userEntity = userService.findById(kakaoId);  // TODO: 로직 동작 확인

        try {
            Level newLevel = Level.valueOf(request.get("level"));
            userEntity.setLevel(newLevel);
            userService.editUser(userEntity);

            return ResponseEntity.ok("레벨이 " + newLevel + "로 변경되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("잘못된 레벨입니다: " + e.getMessage());
        }
    }

    // 테스트용 탄소 절감량 변경 엔드포인트
    @PutMapping("/test/carbon")
    public ResponseEntity<String> updateTestCarbon(@RequestBody Map<String, Double> request, Authentication authentication) {
        Long kakaoId = Long.parseLong(authentication.getName());
        UserEntity userEntity = userService.findById(kakaoId);  // TODO: 로직 동작 확인

        try {
            Double carbonAmount = request.get("totalCarbonSaved");
            userEntity.setTotalCarbonSaved(carbonAmount);
            userService.editUser(userEntity);

            return ResponseEntity.ok("탄소 절감량이 " + carbonAmount + "kg으로 변경되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("잘못된 값입니다: " + e.getMessage());
        }
    }
}
