package com.openbrewerydb.listeners;

import com.openbrewerydb.filters.HttpCaptureFilter;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class HttpArtifactsTestNgListener implements ITestListener {

    private static final Logger log = LoggerFactory.getLogger(HttpArtifactsTestNgListener.class);
    private static final int MAX_ATTACH_LEN = 1_000_000; // ~1MB safeguard

    private static void attachHttpArtifacts(String phase, ITestResult result) {
        String testName = result.getName();
        String params = Arrays.toString(result.getParameters());

        String req = safe(HttpCaptureFilter.pollLastRequest());
        String resp = safe(HttpCaptureFilter.pollLastResponse());

        logAtPhase(phase, "Test {} {}. Params={}, attaching last HTTP request/response.",
                testName, phase, params);

        attachSmart("HTTP REQUEST (last)", req);
        attachSmart("HTTP RESPONSE (last)", resp);

        if (result.getThrowable() != null && "FAIL".equals(phase)) {
            String stack = stackToString(result.getThrowable());
            attachText("Throwable", stack);
        }
    }

    private static void logAtPhase(String phase, String msg, Object... args) {
        switch (phase) {
            case "FAIL":
                log.error(msg, args);
                break;
            case "SKIP":
                log.warn(msg, args);
                break;
            default:
                log.info(msg, args);
        }
    }

    private static String safe(String s) {
        if (s == null || s.isBlank()) return "<no data captured>";
        if (s.length() > MAX_ATTACH_LEN) {
            return s.substring(0, MAX_ATTACH_LEN) + "\n\n-- truncated --";
        }
        return s;
    }

    private static void attachSmart(String name, String content) {
        String c = (content == null) ? "<no data captured>" : content;
        String ct = looksLikeJson(c) ? "application/json" : "text/plain";
        String ext = looksLikeJson(c) ? ".json" : ".txt";
        Allure.addAttachment(name, ct, new ByteArrayInputStream(c.getBytes(StandardCharsets.UTF_8)), ext);
    }

    private static void attachText(String name, String content) {
        String c = (content == null) ? "<no data captured>" : content;
        Allure.addAttachment(name, "text/plain",
                new ByteArrayInputStream(c.getBytes(StandardCharsets.UTF_8)), ".txt");
    }

    private static boolean looksLikeJson(String s) {
        String t = s.trim();
        return (t.startsWith("{") && t.endsWith("}")) || (t.startsWith("[") && t.endsWith("]"));
    }

    private static String stackToString(Throwable t) {
        StringBuilder sb = new StringBuilder(1024);
        while (t != null) {
            sb.append(t).append('\n');
            for (StackTraceElement el : t.getStackTrace()) {
                sb.append("    at ").append(el).append('\n');
            }
            t = t.getCause();
            if (t != null) sb.append("Caused by: ");
        }
        return sb.toString();
    }

    @Override
    public void onTestStart(ITestResult result) {
        HttpCaptureFilter.clear();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        attachHttpArtifacts("FAIL", result);
        HttpCaptureFilter.clear();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        attachHttpArtifacts("SKIP", result);
        HttpCaptureFilter.clear();
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        HttpCaptureFilter.clear();
    }
}
