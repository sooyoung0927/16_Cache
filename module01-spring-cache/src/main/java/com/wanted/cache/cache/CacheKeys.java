package com.wanted.cache.cache;

import java.util.Locale;

public final class CacheKeys {

    /*comment
    *  캐시는 key가 존재하며
    *  key가 같으면 같은 캐시 항목으로 취급한다. */

    private CacheKeys() {
    }

    /*comment
    *  key 생성
    *  예1) popular::food::2000::12000 -> 이렇게 1개의 key를 생성한다
    *  예2) popular::food::2000::10000 -> 이렇게 1개의 key를 생성한다
    *   예1과 예2가 한 부분이라도 다르다면 그건 다른 키로 서로 다른 부분임
    * */
    public static String search(String keyword, String category, Integer minPrice, Integer maxPrice) {
        return normalize(keyword) + "::" + normalize(category) + "::" + value(minPrice) + "::" + value(maxPrice);
    }


    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "*";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static String value(Integer value) {
        return value == null ? "*" : String.valueOf(value);
    }
}
