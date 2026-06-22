package com.wanted.cache.cache;

public final class CacheNames {
    /*comment
    *  CacheKeys는 캐시 공간에 접근하는 Key 값을 정의한다
    *  CacheNames는 해당 공간이 어떤 종류의 데이터를 담는 공간인지에 대한 표현을 한다 */

    // 살품 ID 하나에 상품 상세가 매핑되는 캐시
    public static final String PRODUCT_DETAIL = "productDetail";

    // 검색어 조합에 따라 검색 결과가 매핑되는 캐시
    public static final String PRODUCT_SEARCH = "productSearch";

    private CacheNames() {
    }
}
