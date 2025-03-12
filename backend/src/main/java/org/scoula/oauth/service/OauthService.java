package org.scoula.oauth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.oauth.domain.dto.OauthMemberDTO;
import org.scoula.oauth.domain.authcode.AuthCodeRequestUrlProviderComposite;
import org.scoula.oauth.domain.client.OauthMemberClientComposite;
import org.scoula.oauth.domain.vo.OauthMemberVO;
import org.scoula.oauth.domain.vo.OauthServerType;
import org.scoula.oauth.domain.vo.Role;
import org.scoula.oauth.jwt.service.JwtService;
import org.scoula.oauth.mapper.OauthMemberMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Service
@RequiredArgsConstructor
/*
 * OAuth 인증 및 회원 관리를 처리하는 서비스 클래스;
 */
public class OauthService {

    private final AuthCodeRequestUrlProviderComposite authCodeRequestUrlProviderComposite;

    private final OauthMemberClientComposite oauthMemberClientComposite;

    private final OauthMemberMapper oauthMemberMapper;

    private final JwtService jwtService;


    public String getAuthCodeRequestUrl(OauthServerType oauthServerType) {
        return authCodeRequestUrlProviderComposite.provide(oauthServerType);
    }


    @Transactional
    public void login(OauthServerType oauthServerType, String authCode, HttpServletResponse response) {
        OauthMemberVO oauthMemberVO = oauthMemberClientComposite.fetch(oauthServerType, authCode);

        String userId = oauthServerType.name() + "_" + oauthMemberVO.getOauthId().getOauthServerId().replace(" ", "");
        oauthMemberVO.setUserId(userId);
        oauthMemberVO.setRole(Role.USER);

        OauthMemberVO saved = oauthMemberMapper.findByOauthId(oauthMemberVO.getUserId());

        if (saved == null) {
            oauthMemberMapper.save(oauthMemberVO);
        }

        String jwtToken = jwtService.generateToken(oauthMemberVO.getUserId(), oauthMemberVO.getRole());

        Cookie jwtCookie = new Cookie("jwtToken", jwtToken);
        jwtCookie.setHttpOnly(false);
        jwtCookie.setSecure(false); // 개발 중에만 false -> https만 지원함
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(1 * 60 * 60);

        // 응답에 쿠키 추가
        response.addCookie(jwtCookie);
    }

    public OauthMemberDTO getInfo(String token) {

        token = token.substring(7);
        String userId = jwtService.getUserIdFromToken(token);

        return OauthMemberDTO.of(oauthMemberMapper.findByOauthId(userId));
    }

    public int updateInfo(String token, OauthMemberDTO member) {
        token = token.substring(7);
        String userId = jwtService.getUserIdFromToken(token);
        log.info("-------------update service-------------");

        return oauthMemberMapper.updateOauthInfo(userId, member);
    }
}
