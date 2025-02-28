package org.scoula.like.dictionary.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.dictionary.domain.DictionaryDTO;
import org.scoula.like.dictionary.mapper.LikeDictionaryMapper;
import org.scoula.oauth.jwt.service.JwtService;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeDictionaryServiceImpl implements LikeDictionaryService {

    private final LikeDictionaryMapper mapper;

    private final JwtService jwtService;

    @Override
    public int create(int dicNo, String token) {
        token = token.substring(7);
        String userId = jwtService.getUserIdFromToken(token);
        return mapper.create(dicNo, userId);
    }

    @Override
    public List<DictionaryDTO> getList(String token) {
        token = token.substring(7);
        String userId = jwtService.getUserIdFromToken(token);

        log.info("DDDDDDDDDDDDDDDDDDICT");

        return mapper.getList(userId).stream()
                .map(DictionaryDTO::of)
                .toList();
    }

    @Override
    public int delete(int dicNo) {
        return mapper.delete(dicNo);
    }
}
