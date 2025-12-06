package com.openbrewerydb.filters;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

public final class HttpCaptureFilter implements Filter {

    private static final ThreadLocal<String> LAST_REQUEST = new ThreadLocal<>();
    private static final ThreadLocal<String> LAST_RESPONSE = new ThreadLocal<>();

    public static void clear() {
        LAST_REQUEST.remove();
        LAST_RESPONSE.remove();
    }

    public static String pollLastRequest() {
        String v = LAST_REQUEST.get();
        LAST_REQUEST.remove();
        return v;
    }

    public static String pollLastResponse() {
        String v = LAST_RESPONSE.get();
        LAST_RESPONSE.remove();
        return v;
    }

    private static String buildRequestDump(FilterableRequestSpecification req) {
        StringJoiner j = new StringJoiner("\n");

        String method = req.getMethod();
        String uri = req.getURI();
        j.add(method + " " + uri);

        if (req.getHeaders() != null && req.getHeaders().size() > 0) {
            req.getHeaders().forEach(h -> j.add(h.getName() + ": " + h.getValue()));
        }

        if (req.getCookies() != null && !req.getCookies().asList().isEmpty()) {
            j.add("");
            req.getCookies().asList().forEach(c -> j.add("Cookie: " + c.getName() + "=" + c.getValue()));
        }

        Object body = req.getBody();
        if (body != null) {
            j.add("");
            j.add(stringifyBody(body));
        }

        return j.toString();
    }

    // ==================== helpers ====================

    private static String buildResponseDump(Response resp) {
        StringJoiner j = new StringJoiner("\n");

        String statusLine = resp.getStatusLine();
        j.add(statusLine != null && !statusLine.isBlank()
                ? statusLine
                : ("HTTP/1.1 " + resp.getStatusCode()));

        if (resp.getHeaders() != null && resp.getHeaders().size() > 0) {
            resp.getHeaders().forEach(h -> j.add(h.getName() + ": " + h.getValue()));
        }

        String body = nullSafe(resp.asString());
        if (!body.isBlank()) {
            j.add("");
            j.add(body);
        }

        return j.toString();
    }

    private static String stringifyBody(Object body) {
        if (body instanceof byte[]) {
            return new String((byte[]) body, StandardCharsets.UTF_8);
        }
        return String.valueOf(body);
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }

    @Override
    public Response filter(FilterableRequestSpecification req,
                           FilterableResponseSpecification res,
                           FilterContext ctx) {

        String requestDump = buildRequestDump(req);
        LAST_REQUEST.set(requestDump);

        Response response = ctx.next(req, res);

        String responseDump = buildResponseDump(response);
        LAST_RESPONSE.set(responseDump);

        return response;
    }
}
