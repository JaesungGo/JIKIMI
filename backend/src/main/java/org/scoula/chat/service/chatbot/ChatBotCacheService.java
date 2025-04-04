package org.scoula.chat.service.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.chat.dto.ChatBotDTO;
import org.scoula.chat.mapper.ChatBotMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatBotCacheService {

    private final ChatBotMapper chatBotMapper;
    private final RedisTemplate<String, String> redisTemplate;

    private final Map<String, LocalCache> localCache = new ConcurrentHashMap<>(LOCAL_CACHE_SIZE);
    private final Object cacheLock = new Object();
    private static final int LOCAL_CACHE_SIZE = 1000;
    private static final long LOCAL_CACHE_TTL = 3600L;
    private static final long REDIS_CACHE_TTL = 86400L;
    private static final int REDIS_THRESHOLD = 50;
    private static final int LOCAL_THRESHOLD = 5;

    @Data
    @Builder
    private static class LocalCache {
        private String answer;
        private long cacheHit;
        private LocalDateTime lastAccessedAt;
    }

    @PostConstruct
    public void fillLocalCache() {
        try {
            log.debug("로컬 캐시 워밍 시작...");

            // Redis의 인기 질문 기반
            fillFromRedis();

            // DB의 인기 질문 기준 추가
            int currentCacheSize = localCache.size();
            if (currentCacheSize < LOCAL_CACHE_SIZE * 0.5) {
                fillFromDatabase(LOCAL_CACHE_SIZE - currentCacheSize);
            }

        } catch (Exception e) {
            log.error("로컬 캐시 채우는 도 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @Transactional(timeout = 5)
    public String getResponseWithCache(String question) {
        try {
            String answer = null;
            String normalizedQuestion = normalizeQuestion(question);

            //잠시 로깅하며 체크하는 코드 (운영에서 제거)
            log.debug("========== 로컬 캐시 현재 상태 ==========");
            for (Map.Entry<String, LocalCache> entry : localCache.entrySet()) {
                log.debug("키: '{}', 값: '{}'", entry.getKey(), entry.getValue());
            }
            log.debug("=======================================");

            // 로컬 캐시 확인 (락)
            LocalCache localCacheData = localCache.get(normalizedQuestion);
            if (localCacheData != null) {
                synchronized (cacheLock) {
                    updateLocalCache(localCacheData);
                    return localCacheData.getAnswer();
                }
            }


            // 레디스 캐시 확인
            String redisKey = "chatbot:question" + normalizedQuestion;
            String redisData = redisTemplate.opsForValue().get(redisKey);
            if (redisData != null) {
                long redisCacheHit = incrementRedisHitCount(redisKey);
                log.debug("Redis 캐시 히트! 키: '{}', 값: '{}', 히트 수: {}",
                        redisKey, redisData, redisCacheHit);
                updateRedisCache(redisKey);


                // 레디스 임계치를 넘어가면 로컬캐시에도 저장
                if (redisCacheHit > REDIS_THRESHOLD) {
                    addLocalCache(normalizedQuestion, redisData, redisCacheHit);
                }
                return redisData;
            }

            // RDB 확인
            ChatBotDTO chatByQuestion = chatBotMapper.getByQuestion(normalizedQuestion);
            if (chatByQuestion == null) {
                return null;
            }

            // DB 히트 수 증가
            chatBotMapper.incrementCacheHit(chatByQuestion.getId());
            answer = chatByQuestion.getAnswer();

            if (chatByQuestion.getHitCount() + 1 > REDIS_THRESHOLD) {
                // 레디스 캐시에 저장
                redisTemplate.opsForValue().set(redisKey, chatByQuestion.getAnswer());
                redisTemplate.expire(redisKey, REDIS_CACHE_TTL, TimeUnit.SECONDS);
                redisTemplate.opsForHash().put(redisKey + ":stats", "hits", 1);
            }

            if (chatByQuestion.getHitCount() > LOCAL_THRESHOLD) {
                // 로컬 캐시에 저장
                addLocalCache(chatByQuestion.getQuestion(), chatByQuestion.getAnswer(), chatByQuestion.getHitCount());
            }

            return answer;

        } catch (Exception e) {
            log.error("응답 생성 중 오류 발생 (ChatBotCaching): {}", e.getMessage());
            return null;
        }
    }

    /**
     * Redis 히트 카운트 증가
     * @return 증가 후 히트 카운트
     */
    private long incrementRedisHitCount(String redisKey) {
        String statsKey = redisKey + ":stats";

        // hits 필드가 없으면 생성
        if (!Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(statsKey, "hits"))) {
            redisTemplate.opsForHash().put(statsKey, "hits", 1L);
            return 1L;
        }

        // 히트 카운트 증가
        Long newCount = redisTemplate.opsForHash().increment(statsKey, "hits", 1L);

        // stats 키에도 만료 시간 설정 (메인 키와 동일하게)
        Long ttl = redisTemplate.getExpire(redisKey);
        if (ttl != null && ttl > 0) {
            redisTemplate.expire(statsKey, ttl, TimeUnit.SECONDS);
        } else {
            redisTemplate.expire(statsKey, REDIS_CACHE_TTL, TimeUnit.SECONDS);
        }

        return newCount;
    }

    /**
     * Redis에서 캐시 워밍
     */
    private void fillFromRedis() {
        try {
            // Redis 키 패턴으로 통계 정보 가져오기
            Set<String> statKeys = redisTemplate.keys("chatbot:question:*:stats");
            if (statKeys == null || statKeys.isEmpty()) {
                log.debug("Redis에서 캐싱된 질문을 찾을 수 없습니다.");
                return;
            }

            Map<String, Long> questionHits = new HashMap<>();

            // 모든 키의 히트 수 iter
            for (String statKey : statKeys) {
                String redisKey = statKey.replace(":stats", "");
                Object hitsObj = redisTemplate.opsForHash().get(statKey, "hits");
                if (hitsObj != null) {
                    Long hits = (Long) hitsObj;
                    questionHits.put(redisKey, hits);
                }
            }

            // 히트 수 기준 내림차순 정렬
            List<Map.Entry<String, Long>> sortedEntries = questionHits.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(LOCAL_CACHE_SIZE) // 최대 캐시 크기만큼만
                    .collect(Collectors.toList());

            // 히트 수 높은 순서로 로컬 캐시 채우기
            int loadedCount = 0;
            for (Map.Entry<String, Long> entry : sortedEntries) {
                String redisKey = entry.getKey();
                Long hits = entry.getValue();
                String question = redisKey.replace("chatbot:question:", "");

                String answer = redisTemplate.opsForValue().get(redisKey);
                if (answer != null) {
                    addLocalCache(question, answer, hits);
                    loadedCount++;
                }
            }

            log.debug("Redis에서 {}개의 질문을 로컬 캐시로 로드", loadedCount);
        } catch (Exception e) {
            log.error("Redis에서 캐시 워밍 중 오류: {}", e.getMessage(), e);
        }
    }

    /**
     * DB에서 인기 질문 기반 캐시 워밍
     */
    private void fillFromDatabase(int limit) {
        try {
            // cache_hit 가 높은 순으로 자름
            List<ChatBotDTO> popularQuestions = chatBotMapper.getPopularQuestions(limit);

            if (popularQuestions.isEmpty()) {
                return;
            }

            for (ChatBotDTO question : popularQuestions) {
                String normalizedQuestion = normalizeQuestion(question.getQuestion());
                // 이미 로컬 캐시에 있는지 확인 (Redis에서 이미 로드했을 수 있음)
                if (!localCache.containsKey(normalizedQuestion)) {
                    addLocalCache(normalizedQuestion, question.getAnswer(), question.getHitCount());
                }
            }

            log.info("DB에서 {}개 로컬 캐시로 로드 성공", popularQuestions.size());
        } catch (Exception e) {
            log.error("DB에서 캐시 로드 중 오류: {}", e.getMessage(), e);
        }
    }

    /**
     * DB hit_count 동기화 (스케줄러)
     *
     */
    @Scheduled(fixedRate = 3600000)
    public void syncHitCountsToDB() {
        try {

            // 레디스에 저장되어 있는 모든 키들
            Set<String> keys = redisTemplate.keys("chatbot:question:*:stats");
            if (keys == null || keys.isEmpty()) {
                return;
            }

            int updatedCount = 0;
            for (String statsKey : keys) {
                try {
                    // 원래 키 추출 (stats 부분 제거)
                    String questionKey = statsKey.replace("chatbot:question:", "").replace(":stats", "");

                    // 히트 카운트 가져오기
                    Object hitsObj = redisTemplate.opsForHash().get(statsKey, "hits");
                    if (hitsObj == null) continue;

                    Long hits = (Long) hitsObj;
                    // DB 업데이트 (저장된 값과의 차이만 업데이트)
                    boolean updated = chatBotMapper.syncHitCount(questionKey, hits.intValue()) > 0;
                    if (updated) {
                        updatedCount++;
                    }
                } catch (Exception e) {
                    log.error("히트 카운트 동기화 중 오류: {} - {}", statsKey, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("히트 카운트 DB 동기화 중 오류: {}", e.getMessage(), e);
        }
    }


    /**
     * 질문을 정규화 하는 메서드
     * 1.소문자 변환
     * 2.불필요한 이중 공백 제거
     * 3.앞 뒤 공백 제거
     *
     * @param question
     * @return
     */
    public String normalizeQuestion(String question) {
        return question
                .toLowerCase()
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * 로컬 캐시 업데이트 (사용시 동시성 처리 해주세요)
     * 1. 캐시 카운트 +1
     * 2. 마지막 접근 시간 수정
     * @param localCache
     */
    private void updateLocalCache(LocalCache localCache) {
        localCache.setCacheHit(localCache.getCacheHit() + 1);
        localCache.setLastAccessedAt(LocalDateTime.now());
    }


    /**
     * 로컬 캐시 추가 (이전에 캐시 크기 최적화)
     *
     * @param normalizedQuestion
     * @param answer
     * @param cacheHit
     */
    private void addLocalCache(String normalizedQuestion, String answer, long cacheHit) {
        synchronized (cacheLock) {
            optimizeCacheSize();
            LocalCache generatedLocalCache = LocalCache.builder()
                    .answer(answer)
                    .cacheHit(cacheHit + 1)
                    .lastAccessedAt(LocalDateTime.now()).build();
            localCache.put(normalizedQuestion, generatedLocalCache);
        }
    }


    private void updateRedisCache(String redisKey) {
        redisTemplate.opsForHash().increment(redisKey + ":stats", "hits", 1);
    }

//    private Map<Object, Object> getRedisCache(String redisKey){
//        return redisTemplate.opsForHash().entries(redisKey + ":stats");
//    }

//    private Long getRedisCacheHits(String redisKey) {
//        try {
//            Object redisHits = redisTemplate.opsForHash().get(redisKey + ":stats", "hits");
//            if (redisHits == null) {
//                log.error("Redis에서 캐시 히트 수를 찾을 수 없습니다.");
//                throw new RuntimeException("Redis 캐시 히트 수를 찾을 수 없습니다.");
//            }
//            return (Long) redisHits;
//        } catch (Exception e) {
//            log.error("Redis에서 캐시 히트 수 관련 에러", e.getMessage());
//            throw new RuntimeException("Redis 캐시 히트 수 가져오는 런타임 도중 오류 발생", e);
//        }
//    }


    /**
     * 로컬 캐시 관리 전략 (LRU)
     * 1순위: 사용시간 오래된 순서
     * 2순위: 적게 사용된 순서 (CacheHit Count)
     * 사용
     */
    private void optimizeCacheSize() {
        if (localCache.size() >= LOCAL_CACHE_SIZE) {
            String keyForRemove = localCache.entrySet().stream()
                    .min((e1, e2) -> {
                        int timeLRU = e1.getValue().lastAccessedAt.compareTo(e2.getValue().getLastAccessedAt());
                        if (timeLRU != 0) return timeLRU;

                        return Long.compare(e1.getValue().getCacheHit(), e2.getValue().getCacheHit());
                    })
                    .map(Map.Entry::getKey)
                    .orElse(null);
            if (keyForRemove != null) {
                localCache.remove(keyForRemove);
            }
        }
    }

    /**
     * 시간이 지난 로컬 캐시 정리
     * LOCAL_CACHE_TTL = 3600
     */
    @Scheduled(fixedRate = 60000)
    private void cleaupCache() {
        synchronized (cacheLock) {
            try {
                LocalDateTime now = LocalDateTime.now();
                localCache.entrySet().removeIf(e ->
                        Duration.between(e.getValue().getLastAccessedAt(), now).getSeconds() > LOCAL_CACHE_TTL);
            } catch (Exception e) {
                {
                    log.error("로컬 캐시 정리 오류");
                }
            }
        }
    }
}
