package org.scoula.oauth.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.scoula.oauth.domain.dto.OauthMemberDTO;
import org.scoula.oauth.domain.vo.OauthServerType;
import org.scoula.oauth.jwt.service.JwtService;
import org.scoula.oauth.service.OauthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/*
 * kakao -> OauthServerType.KAKAO로 변환하여 가져옴
 * OauthService에서 URI를 생성하고 사용자를 Redirect 시킴
 */

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
@RestController
public class OauthController {

    private final OauthService oauthService;
    private final JwtService jwtService;



    @SneakyThrows
    @GetMapping("/{oauthServerType}")
    ResponseEntity<Void> redirectAuthCodeRequestUrl(
            @PathVariable OauthServerType oauthServerType,
            HttpServletResponse response
    ) {
        String redirectUrl = oauthService.getAuthCodeRequestUrl(oauthServerType);
        response.sendRedirect(redirectUrl);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/{oauthServerType}")
    ResponseEntity<String> login(
            @PathVariable OauthServerType oauthServerType,
            @RequestParam("code") String code,
            HttpServletResponse response
    ) {
        try {
            oauthService.login(oauthServerType, code, response);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyToken(@RequestHeader("Authorization") String token) {
        System.out.println("OauthController.verifyToken");

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(400).body(Map.of("isAuthenticated", false, "message", "Token is missing or malformed."));
        }

        token = token.substring(7); // "Bearer " 제거

        boolean isValid = jwtService.validateToken(token);
        if (isValid) {
            String userId = jwtService.getUserIdFromToken(token);
            System.out.println("userId = " + userId);
            return ResponseEntity.ok(Map.of("isAuthenticated", true, "userId", userId));
        } else {
            return ResponseEntity.status(401).body(Map.of("isAuthenticated", false, "message", "Invalid or expired token."));
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        System.out.println("AuthController.logout");
        Cookie cookie = new Cookie("jwtToken", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0); // 쿠키 만료 시간 설정
        response.addCookie(cookie);
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/info")
    public ResponseEntity<OauthMemberDTO> Info(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(oauthService.getInfo(token));
    }

    @PutMapping("/info")
    public ResponseEntity<Integer> updateInfo(@RequestHeader("Authorization") String token, @RequestBody OauthMemberDTO member) {
        log.info("-------------update start-------------");
        return ResponseEntity.ok(oauthService.updateInfo(token, member));
    }
}
