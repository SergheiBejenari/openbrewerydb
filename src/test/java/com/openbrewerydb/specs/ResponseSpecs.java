package com.openbrewerydb.specs;

import com.openbrewerydb.config.ConfigProvider;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.ResponseSpecification;

import static org.hamcrest.Matchers.*;

public final class ResponseSpecs {
    private ResponseSpecs() {
    }

    public static ResponseSpecification okJson() {
        return statusJson(200);
    }

    public static ResponseSpecification statusJson(int httpStatus) {
        return new ResponseSpecBuilder()
                .expectStatusCode(httpStatus)
                .expectContentType(ContentType.JSON)
                .expectResponseTime(lessThan((long) ConfigProvider.readTimeoutMs()))
                .build();
    }

    public static ResponseSpecification emptyArray() {
        return new ResponseSpecBuilder()
                .expectBody("$", empty())
                .build();
    }

    public static ResponseSpecification validationMessage422(String expectedMessagePart) {
        return new ResponseSpecBuilder()
                .expectStatusCode(422)
                .expectContentType(ContentType.JSON)
                .expectBody("message", containsString(expectedMessagePart))
                .expectBody("errors.query[0]", containsString(expectedMessagePart))
                .build();
    }

}
