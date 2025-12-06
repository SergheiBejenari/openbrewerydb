package com.openbrewerydb.tests;

import com.openbrewerydb.config.ConfigProvider;
import io.restassured.RestAssured;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import static io.restassured.RestAssured.basePath;
import static io.restassured.RestAssured.baseURI;

public abstract class BaseApiTest {

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        baseURI = ConfigProvider.baseUri();
        basePath = ConfigProvider.basePath();
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        RestAssured.reset();
    }
}
