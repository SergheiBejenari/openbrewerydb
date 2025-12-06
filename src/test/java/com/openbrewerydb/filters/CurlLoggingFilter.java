package com.openbrewerydb.filters;

import io.qameta.allure.Allure;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;

import java.nio.charset.StandardCharsets;

public class CurlLoggingFilter implements Filter {

    private static String toCurl(io.restassured.specification.FilterableRequestSpecification req) {
        StringBuilder sb = new StringBuilder("curl -i -X ")
                .append(req.getMethod()).append(" '")
                .append(req.getURI()).append("'");

        req.getHeaders().asList().forEach(h ->
                sb.append(" \\\n  -H '").append(escape(h.getName())).append(": ")
                        .append(escape(h.getValue())).append("'"));

        Object bodyObj = req.getBody();
        if (bodyObj != null) {
            String body = (bodyObj instanceof byte[])
                    ? new String((byte[]) bodyObj, StandardCharsets.UTF_8)
                    : String.valueOf(bodyObj);
            sb.append(" \\\n  --data '").append(escape(body)).append("'");
        }
        return sb.toString();
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("'", "'\"'\"'");
    }

    @Override
    public Response filter(io.restassured.specification.FilterableRequestSpecification req,
                           io.restassured.specification.FilterableResponseSpecification res,
                           FilterContext ctx) {

        String curl = toCurl(req);
        Allure.addAttachment("cURL", "text/plain", curl, ".sh");
        return ctx.next(req, res);
    }
}
