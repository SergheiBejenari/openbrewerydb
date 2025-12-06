package com.openbrewerydb.specs;

import com.openbrewerydb.config.ConfigProvider;
import com.openbrewerydb.filters.CorrelationIdFilter;
import com.openbrewerydb.filters.CurlLoggingFilter;
import com.openbrewerydb.filters.HttpCaptureFilter;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public final class RequestSpecs {
    private static final String CONNECTION_TIMEOUT_KEY = "http.connection.timeout";
    private static final String SOCKET_TIMEOUT_KEY = "http.socket.timeout";

    private RequestSpecs() {
    }

    public static RequestSpecification json() {
        LogConfig logConfig = LogConfig.logConfig();
        if (ConfigProvider.enableLogOnFailure()) {
            logConfig.enableLoggingOfRequestAndResponseIfValidationFails();
        }

        RestAssuredConfig config = RestAssuredConfig.newConfig()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam(CONNECTION_TIMEOUT_KEY, ConfigProvider.connectTimeoutMs())
                        .setParam(SOCKET_TIMEOUT_KEY, ConfigProvider.readTimeoutMs()))
                .logConfig(logConfig);

        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setConfig(config)
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .addFilter(new CorrelationIdFilter())
                .addFilter(new AllureRestAssured())
                .addFilter(new CurlLoggingFilter())
                .addFilter(new HttpCaptureFilter());

        if (ConfigProvider.enableFullHttpLog()) {
            builder.addFilter(new RequestLoggingFilter(LogDetail.ALL))
                    .addFilter(new ResponseLoggingFilter(LogDetail.ALL));
        }

        return builder.build();
    }
}
