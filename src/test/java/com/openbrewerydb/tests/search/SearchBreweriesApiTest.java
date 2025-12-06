package com.openbrewerydb.tests.search;

import com.openbrewerydb.api.BreweriesApi;
import com.openbrewerydb.api.SearchParams;
import io.qameta.allure.*;
import io.qameta.allure.testng.Tag;
import io.restassured.response.Response;
import com.openbrewerydb.models.Brewery;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.openbrewerydb.specs.RequestSpecs;
import com.openbrewerydb.specs.ResponseSpecs;
import com.openbrewerydb.tests.BaseApiTest;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

@Epic("OpenBreweryDB")
@Feature("Search Breweries API")
@Link(name = "API Docs", url = "https://www.openbrewerydb.org/documentation")
public class SearchBreweriesApiTest extends BaseApiTest {

    private BreweriesApi api;

    // ----------------------------
    // Helpers
    // ----------------------------
    private static Predicate<Brewery> containsAllTokens(String rawQuery) {
        final List<String> tokens = Arrays.stream(rawQuery.toLowerCase().trim().split("[\\s_]+"))
                .filter(t -> t.length() >= 3)
                .collect(Collectors.toList());

        if (tokens.isEmpty()) return item -> false;

        return b -> {
            String haystack = Stream.of(
                            b.getId(),
                            b.getName(),
                            b.getBreweryType(),
                            b.getCity(),
                            b.getStateProvince(),
                            b.getState(),
                            b.getCountry(),
                            b.getPostalCode(),
                            b.getStreet(),
                            b.getAddress1(),
                            b.getAddress2(),
                            b.getAddress3(),
                            b.getPhone(),
                            b.getWebsiteUrl(),
                            b.getLongitude() == null ? null : b.getLongitude().toString(),
                            b.getLatitude() == null ? null : b.getLatitude().toString()
                    )
                    .filter(Objects::nonNull)
                    .map(String::toLowerCase)
                    .collect(Collectors.joining(" "));

            return tokens.stream().allMatch(haystack::contains);
        };
    }

    @BeforeClass
    public void initClient() {
        this.api = new BreweriesApi(RequestSpecs.json());
    }

    @DataProvider(name = "dpTypicalQueries", parallel = true)
    public Object[][] dpTypicalQueries() {
        return new Object[][]{
                {"STONE BREWING"},
                {"san_diego "},
                {"brewing stone"},
                {"micro"},
                {"ale"},
                {"919"}
        };
    }

    @DataProvider(name = "dpInvalidOrMissingQuery", parallel = true)
    public Object[][] dpInvalidOrMissingQuery() {
        return new Object[][]{
                {"missing", null, "required"},
                {"empty", "", "required"},
                {"spaces", "   ", "required"},
                {"oneChar", "a", "at least 3"},
                {"twoChars", "as", "at least 3"}
        };
    }

    @DataProvider(name = "dpNormalizationPairs", parallel = true)
    public Object[][] dpNormalizationPairs() {
        return new Object[][]{
                {"stone brewing", "stone_brewing"},
                {"California", "CaLIForNia"}
        };
    }

    // ----------------------------
    // Tests
    // ----------------------------

    @DataProvider(name = "dpPerPageValues", parallel = true)
    public Object[][] dpPerPageValues() {
        return new Object[][]{{1}, {55}, {200}};
    }

    @Story("SB-001 · Typical queries return relevant results")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Non-empty results; at least one item matches all normalized query tokens.")
    @Tag("search")
    @Tag("relevance")
    @Test(dataProvider = "dpTypicalQueries", groups = {"search", "relevance", "positive", "regression"})
    public void shouldReturnRelevantResultsForTypicalQueries(String query) {
        Response resp = api.search(SearchParams.of(query))
                .then()
                .spec(ResponseSpecs.okJson())
                .extract().response();

        List<Brewery> items = resp.jsonPath().getList("", Brewery.class);
        assertThat(items)
                .as("Expected non-empty results for query='%s'", query)
                .isNotEmpty();

        boolean anyItemContainsAllTokens = items.stream().anyMatch(containsAllTokens(query));
        assertThat(anyItemContainsAllTokens)
                .as("At least one item must contain all normalized tokens for query='%s'", query)
                .isTrue();
    }

    @Story("SB-002 · No matches → empty array")
    @Severity(SeverityLevel.NORMAL)
    @Description("Non-existing term returns an empty array.")
    @Tag("search")
    @Tag("empty")
    @Test(groups = {"search", "empty", "positive", "regression"})
    public void shouldReturnEmptyArrayWhenNoMatches() {
        final String rareQuery = "zzqxqzzzxqzzxnonexist";

        Response resp = api.search(SearchParams.of(rareQuery))
                .then()
                .spec(ResponseSpecs.okJson())
                .spec(ResponseSpecs.emptyArray())
                .extract().response();

        List<?> body = resp.jsonPath().getList("$");
        assertThat(body).isEmpty();
    }

    @Story("SB-003 · Query parameter validation")
    @Severity(SeverityLevel.MINOR)
    @Description("Missing/empty/too-short query → 422 + validation JSON with expected message part.")
    @Tag("search")
    @Tag("validation")
    @Tag("negative")
    @Test(dataProvider = "dpInvalidOrMissingQuery",
            groups = {"search", "validation", "negative", "regression"})
    public void shouldReturn422ValidationForInvalidOrMissingQuery(String caseTitle,
                                                                  String query,
                                                                  String expectedPart) {
        Response resp = step("GET /search · " + (query == null ? "no query param" : "query='" + query + "'"),
                () -> api.search(query == null ? null : SearchParams.of(query)));

        resp.then().spec(ResponseSpecs.validationMessage422(expectedPart));

        Allure.addAttachment("case", caseTitle + (query == null ? " (no query param)" : " query='" + query + "'"));
        Allure.addAttachment("status", String.valueOf(resp.statusCode()));
        Allure.addAttachment("body", resp.asPrettyString());
    }

    @Story("SB-004 · Input normalization (spaces/underscores/case)")
    @Severity(SeverityLevel.NORMAL)
    @Description("Equivalent top-N ID sets for spaces vs underscores and case variations.")
    @Tag("search")
    @Tag("normalization")
    @Test(dataProvider = "dpNormalizationPairs", groups = {"search", "normalization", "positive", "regression"})
    public void shouldTreatSpacesUnderscoresAndCaseAsEquivalent(String q1, String q2) {
        List<String> idsQ1 = step("GET /search?query=" + q1, () ->
                api.search(SearchParams.of(q1))
                        .then().spec(ResponseSpecs.okJson())
                        .body("$", not(empty()))
                        .extract().jsonPath().getList("id", String.class));

        List<String> idsQ2 = step("GET /search?query=" + q2, () ->
                api.search(SearchParams.of(q2))
                        .then().spec(ResponseSpecs.okJson())
                        .body("$", not(empty()))
                        .extract().jsonPath().getList("id", String.class));

        final int topN = Math.min(50, Math.min(idsQ1.size(), idsQ2.size()));
        Set<String> topSet1 = idsQ1.stream().limit(topN).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> topSet2 = idsQ2.stream().limit(topN).collect(Collectors.toCollection(LinkedHashSet::new));

        Allure.addAttachment("topQ1_ids", topSet1.toString());
        Allure.addAttachment("topQ2_ids", topSet2.toString());

        assertThat(topSet1)
                .as("Top-%s sets should be equal for normalized queries ('%s' vs '%s')", topN, q1, q2)
                .isEqualTo(topSet2);
    }

    @Story("SB-005 · Pagination: per_page limit is respected")
    @Severity(SeverityLevel.NORMAL)
    @Description("Page size does not exceed per_page; IDs are unique within the page.")
    @Tag("search")
    @Tag("pagination")
    @Tag("per_page")
    @Test(dataProvider = "dpPerPageValues", groups = {"search", "per_page", "positive", "regression"})
    public void shouldRespectPerPageLimit(int perPage) {
        final String query = "brewing";

        List<String> ids = step("GET /search?query=" + query + "&per_page=", () ->
                api.search(SearchParams.of(query, perPage, 1))
                        .then().spec(ResponseSpecs.okJson())
                        .extract().jsonPath().getList("id", String.class));

        assertThat(ids)
                .as("Expect non-empty page for query='%s'", query)
                .isNotEmpty()
                .doesNotContainNull();

        assertThat(ids.size())
                .as("Page size must be ≤ per_page (%s)", perPage)
                .isLessThanOrEqualTo(perPage);

        assertThat(ids.stream().distinct().count())
                .as("IDs within a single page must be unique")
                .isEqualTo(ids.size());

        Allure.addAttachment("per_page", String.valueOf(perPage));
        Allure.addAttachment("returned_count", String.valueOf(ids.size()));
    }
}
