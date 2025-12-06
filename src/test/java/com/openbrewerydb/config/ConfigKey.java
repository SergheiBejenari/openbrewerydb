package com.openbrewerydb.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConfigKey {

    BASE_URI("base_uri", "https://api.openbrewerydb.org"),
    BASE_PATH("base_path", "/v1/breweries"),

    ENABLE_LOG_ON_FAILURE("enable_log_on_failure", "true"),
    ENABLE_FULL_HTTP_LOG("enable_full_http_log", "false"),

    HTTP_CONNECT_TIMEOUT_MS("http_connect_timeout_ms", "3000"),
    HTTP_READ_TIMEOUT_MS("http_read_timeout_ms", "3000");

    private final String key;
    private final String defaultValue;
}
