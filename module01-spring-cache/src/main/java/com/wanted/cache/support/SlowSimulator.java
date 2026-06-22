package com.wanted.cache.support;

import org.springframework.stereotype.Component;

@Component
public class SlowSimulator {

    // 상세 조회용 의도적인 지연 만드는 메서드
    // 120ms의 지연을 만들어냄
    public void detailQueryLatency() {
        sleep(120);
    }

    // 검색 조회용 의도적인 지연 만드는 메서드
    // 450ms 지연을 만듦
    public void searchQueryLatency() {
        sleep(450);
    }

    //  ThreadSleep 메서드 의도적으로 작업을 중단한다
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Sleep Error 발생!", e);
        }
    }
}
