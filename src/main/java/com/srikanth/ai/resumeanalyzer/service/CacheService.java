package com.srikanth.ai.resumeanalyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Service for caching analysis results using Redis
 */
@Slf4j
@Service
public class CacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "resume_analysis:";
    private static final long CACHE_TTL_HOURS = 24;

    public CacheService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Generates cache key from resume and job description
     *
     * @param resumeText Resume text
     * @param jobDescription Job description
     * @return Cache key
     */
    public String generateCacheKey(String resumeText, String jobDescription) {
        String combined = resumeText + "|" + jobDescription;
        int hashCode = Math.abs(combined.hashCode());
        return CACHE_PREFIX + hashCode;
    }

    /**
     * Gets cached analysis result
     *
     * @param cacheKey The cache key
     * @return Cached analysis result or null
     */
    public Map<String, Object> getCachedAnalysis(String cacheKey) {
        try {
            String cachedValue = redisTemplate.opsForValue().get(cacheKey);
            if (cachedValue != null) {
                log.info("Cache hit for key: {}", cacheKey);
                @SuppressWarnings("unchecked")
                Map<String, Object> result = objectMapper.readValue(cachedValue, Map.class);
                return result;
            }
        } catch (Exception e) {
            log.warn("Error retrieving from cache: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Caches analysis result
     *
     * @param cacheKey The cache key
     * @param analysisResult The analysis result to cache
     */
    public void cacheAnalysis(String cacheKey, Map<String, Object> analysisResult) {
        try {
            String jsonValue = objectMapper.writeValueAsString(analysisResult);
            redisTemplate.opsForValue().set(
                cacheKey,
                jsonValue,
                CACHE_TTL_HOURS,
                TimeUnit.HOURS
            );
            log.info("Cached analysis result with key: {}", cacheKey);
        } catch (Exception e) {
            log.warn("Error caching analysis result: {}", e.getMessage());
        }
    }

    /**
     * Invalidates cache for a key
     *
     * @param cacheKey The cache key to invalidate
     */
    public void invalidateCache(String cacheKey) {
        try {
            Boolean deleted = redisTemplate.delete(cacheKey);
            if (Boolean.TRUE.equals(deleted)) {
                log.info("Invalidated cache for key: {}", cacheKey);
            }
        } catch (Exception e) {
            log.warn("Error invalidating cache: {}", e.getMessage());
        }
    }

    /**
     * Clears all analysis cache
     */
    public void clearAllCache() {
        try {
            redisTemplate.delete(redisTemplate.keys(CACHE_PREFIX + "*"));
            log.info("Cleared all analysis cache");
        } catch (Exception e) {
            log.warn("Error clearing cache: {}", e.getMessage());
        }
    }
}

