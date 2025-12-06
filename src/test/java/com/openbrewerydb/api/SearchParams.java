package com.openbrewerydb.api;

import lombok.Builder;
import lombok.Value;

import java.util.LinkedHashMap;
import java.util.Map;

@Value
@Builder
public class SearchParams {
    String query;
    Integer perPage;
    Integer page;

    public static SearchParams of(String query) {
        return SearchParams.builder().query(query).build();
    }

    public static SearchParams of(String query, Integer perPage, Integer page) {
        return SearchParams.builder().query(query).perPage(perPage).page(page).build();
    }

    public Map<String, Object> toQueryMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        if (query != null) map.put("query", query);
        if (perPage != null) map.put("per_page", perPage);
        if (page != null) map.put("page", page);
        return map;
    }
}
