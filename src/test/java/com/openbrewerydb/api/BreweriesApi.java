package com.openbrewerydb.api;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static com.openbrewerydb.specs.RequestSpecs.json;

public final class BreweriesApi {
    private static final String SEARCH_PATH = "/search";
    private final RequestSpecification spec;

    public BreweriesApi() {
        this(json());
    }

    public BreweriesApi(RequestSpecification spec) {
        this.spec = spec;
    }

    @Step("GET /search · {params}")
    public Response search(SearchParams params) {
        Map<String, Object> qp = (params == null) ? Map.of() : params.toQueryMap();
        return given()
                .spec(spec)
                .queryParams(qp)
                .when()
                .get(SEARCH_PATH)
                .then()
                .extract().response();
    }
}
