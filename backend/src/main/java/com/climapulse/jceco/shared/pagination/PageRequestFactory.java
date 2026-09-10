package com.climapulse.jceco.shared.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class PageRequestFactory {

    private static final int DEFAULT_PAGE_SIZE = 20;

    public PageRequest defaultPage(int page) {
        return PageRequest.of(page, DEFAULT_PAGE_SIZE);
    }
}
