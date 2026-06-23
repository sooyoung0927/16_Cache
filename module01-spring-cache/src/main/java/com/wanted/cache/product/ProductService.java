package com.wanted.cache.product;

import com.wanted.cache.cache.CacheNames;
import com.wanted.cache.support.SlowSimulator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
// 해당 어노테이션은 이 서비스에서 반복 사용하는 기본 캐시 이름을 클래스 수준에 지정해서
// 메서드마다 중복될 수 있는 문자열을 줄이는 용도로 사용한다
@CacheConfig(cacheNames = CacheNames.PRODUCT_DETAIL)
public class ProductService {

    private final ProductRepository productRepository;
    private final SlowSimulator slowSimulator;

    public Product getProductBefore(Long id) {

        // 의도적인 지연 발생시킴
        slowSimulator.detailQueryLatency();

        return findProduct(id);
    }

    // id로 조회용 헬퍼 메서드
    private Product findProduct(Long id){
        return productRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException("ID가 없습니다"));
    }

    public ProductResponse searchBefore(String keyword, String category, Integer minPrice, Integer maxPrice) {

        slowSimulator.searchQueryLatency();

        return searchProducts(keyword, category, minPrice, maxPrice);
    }

    private ProductResponse searchProducts(String keyword, String category, Integer minPrice, Integer maxPrice) {
        List<Product> products = productRepository.search(blankToNull(keyword), blankToNull(category), minPrice, maxPrice);
        return new ProductResponse(keyword, category, minPrice, maxPrice, products.size(), products);
    }

    // 검색 조건이 비어서 올 때(= 빈 문자열) NULL로 변환해서 반환해주는 헬퍼메서드
    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

//    ==========================================

    // 상세조회에 캐시 추가
    /*comment
    *  - key : 메서드 마라미터인 id 변수를 캐시 접근 key로 사용하겠다는 의미
    *  - condition : 메서드 실행 전에 편가되며, id가 0이하면 캐시를 사용하지 않는다
    *  - unless : 메서드 실행 후에 편가되며, 결과값이 null이면 캐시를 사용하지 않는다 = 메서드 실행 후에 결과가 널이면 캐시에 저장하지 않겠다 */
    @Cacheable(key="#id",condition = "#id>0", unless = "#result == null")
    public Product getProductAfter(Long id) {

        // 의도적인 지연 발생시킴
        slowSimulator.detailQueryLatency();

        return findProduct(id);
    }

    // 검색으로 상품 조회하기
    @Cacheable(cacheNames = CacheNames.PRODUCT_SEARCH, // 클래스 레벨에 있는 거랑 달라서 작성해줘야함
                key = "T(com.wanted.cache.cache.CacheKeys).search(#keyword, #category, #minPrice, #maxPrice)",
                condition = "#keyword != null && #keyword.length() >=2 ", // 검색어 2글자 이하로 키 만들면 키가 너무 많아지니까
                unless = "#result.totalCount() == 0"
    )
    public ProductResponse searchAfter(String keyword, String category, Integer minPrice, Integer maxPrice) {

        slowSimulator.searchQueryLatency();

        return searchProducts(keyword, category, minPrice, maxPrice);
    }

    // CachePut은 캐시 히트 여부와 관계없이 메서드 본문을 실행한다.
    @CachePut(key="#id")
    public Product refreshProduct(Long id) {
        slowSimulator.detailQueryLatency();

        return findProduct(id);
    }

    // id 값에 해당하는 캐시 데이터를 무효화 (제거)한다
    @CacheEvict(key="#id")
    public void evictProduct(Long id) {

    }


    /*comment
    *  @Caching은 여러 캐시 작업을 한 번에 묶을 수 있다
    *  = put + evict + cacheable
    *  --
    *  재고 변경 등에 의한 캐시 재설정은 put 보다는 evict 해서 기존 캐시를 날리고
    *  새로 만드는 방법이 훨씬 많이 쓰인다
    *  --
    *  evict allEntries = true 는 PRODUCT_SEARCH 캐시 전체를 비우는 명령어이다.
    *  단순하고 안전하지만, PRODUCT_SEARCH 캐시가 많을수록 재생성 비용이 커질 수 있다.
    *  트레이드 오프가 발생한다
    *  */
    @Transactional // dml 이니까
    @Caching(
            put = @CachePut(key="#id"),
            evict = @CacheEvict(cacheNames = CacheNames.PRODUCT_SEARCH,allEntries = true)
    )
    public Product changeStock(Long id, int stock) {

        Product product = findProduct(id);
        product.changeStock(stock);

        return product;
    }
}
