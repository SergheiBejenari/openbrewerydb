package com.openbrewerydb.filters;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class CorrelationIdFilter implements Filter {
    public static final String CID_KEY = "cid";
    public static final String CID_HEADER = "X-Correlation-Id";

    @Override
    public Response filter(FilterableRequestSpecification req,
                           FilterableResponseSpecification res,
                           FilterContext ctx) {

        String cid = MDC.get(CID_KEY);
        if (cid == null || cid.isBlank()) {
            cid = UUID.randomUUID().toString();
            MDC.put(CID_KEY, cid);
        }

        Map<String, String> headers = req.getHeaders().asList().stream()
                .collect(Collectors.toMap(h -> h.getName().toLowerCase(), Header::getValue, (a, b) -> a));

        if (!headers.containsKey(CID_HEADER.toLowerCase())) {
            req.header(CID_HEADER, cid);
        }
        return ctx.next(req, res);
    }
}
