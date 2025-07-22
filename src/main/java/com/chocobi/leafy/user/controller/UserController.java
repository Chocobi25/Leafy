package com.chocobi.leafy.user.controller;

import com.chocobi.leafy.user.dto.UserProfileDto;
import com.chocobi.leafy.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
