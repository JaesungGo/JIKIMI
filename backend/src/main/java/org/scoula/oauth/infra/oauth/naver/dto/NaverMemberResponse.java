package org.scoula.oauth.infra.oauth.naver.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.oauth.domain.vo.OauthId;
import org.scoula.oauth.domain.vo.OauthMemberVO;
import org.scoula.oauth.domain.vo.OauthServerType;

import java.time.LocalDate;


@JsonNaming(SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NaverMemberResponse {
    private String resultcode;
    private String message;
    private Response response;

    public OauthMemberVO toDomain() {
        if (response == null) {
            throw new IllegalStateException("Response is null");
        }

        Integer gender = null;
        if ("M".equals(response.gender)) {
            gender = 1;
        } else if ("F".equals(response.gender)) {
            gender = 0;
        }


        String birthday = null;
        if (response.birthday != null) {
            String[] parts = response.birthday.split("-");
            if (parts.length == 2) {
                int year = LocalDate.now().getYear(); // 현재 연도를 사용
                birthday = String.format("%d-%s-%s", year, parts[0], parts[1]);
            }
        }

        return OauthMemberVO.builder()
                .oauthId(new OauthId(String.valueOf(response.id), OauthServerType.NAVER))
                .email(response.email)
                .name(response.name)
                .gender(gender)  // 성별 설정
                .birthday(birthday)
                .mobileE164(response.mobile_e164)
                .build();
    }

    @JsonNaming(SnakeCaseStrategy.class)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private String id;
        private String nickname;
        private String name;
        private String email;
        private String gender;
        private String birthday;
        private String profileImage;
        private String birthyear;
        private String mobile;
        private String mobile_e164;
        private String age;
    }
}