package com.wanted.cache.product;

import com.wanted.cache.cache.CacheNames;
import com.wanted.cache.support.SlowSimulator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
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
}
