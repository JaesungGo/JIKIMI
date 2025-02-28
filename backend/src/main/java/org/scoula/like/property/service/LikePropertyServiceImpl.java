package org.scoula.like.property.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.like.property.mapper.LikePropertyMapper;
import org.scoula.map.domain.MapDetailDTO;
import org.scoula.oauth.jwt.service.JwtService;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikePropertyServiceImpl implements LikePropertyService {

    private final LikePropertyMapper mapper;

    private final JwtService jwtService;

    @Override
    public int create(int proNo, String token) {
        token = token.substring(7);
        String userId = jwtService.getUserIdFromToken(token);
        return mapper.create (proNo, userId);
    }

    @Override
    public List<MapDetailDTO> getList(String token) {
        token = token.substring(7);
        String userId = jwtService.getUserIdFromToken(token);

        return mapper.getList(userId);
    }

    @Override
    public int delete(int proNo) {
        return mapper.delete(proNo);
    }
}
