package com.wanted.cache.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
/*comment
*  @EnableCashing 어노테이션이 있어야
*  @Cacheable, @CachePut, @CacheEvict, @Caching 등 캐시 관련 어노테이션이 동작할 수 있다
*  Spring은 해당 어노테이션이 붙은 메서드를 프록시로 감싸고 메서드 호출 전,후에 캐시가 있는지를 확인한다. */
@EnableCaching
public class LocalCacheConfig {

    // 미리 만들어둔 CacheName
    private static final List<String> CACHE_NAMES = List.of(
            CacheNames.PRODUCT_DETAIL,
            CacheNames.PRODUCT_SEARCH
    );

    @Bean
    CacheManager cacheManager(){
        // CacheManager는 스트링에서 제공하는 추상화 객체로 우리의 구현체로 덮을거임
        // 실제 구현체는 CaffeineCacheManager로 구현
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();

        caffeineCacheManager.setCacheNames(CACHE_NAMES);

        // 캐시 관련 설정
        caffeineCacheManager.setCaffeine(
                Caffeine.newBuilder()
                        // 캐시 폭발 방지를 위해 최대 항목 수 지정
                        .maximumSize(1_000)
                        // 오래된 데이터(캐시)가 무한히 남지 않게 만료시간을 둔다
                        .expireAfterAccess(Duration.ofMinutes(5))
                        // 캐시 관련 지표를 볼 수 있게 통계를 기록한다
                        // 캐시히트 비율 확인 -> 수치화 프로메테우스와 연동
                        .recordStats()
                        // 크기 제한, 만료 등으로 제거도ㅓㅣㄴ 캐기 항목을 관찰
                        // 커스텀 메서드 사용 가능
                        .removalListener((key, value, cause) -> System.out.printf("cache removed: key=%s , cause=%s%n", key , cause))
        );
        return caffeineCacheManager;
    }


}
