package com.openbrewerydb.config;

import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

@UtilityClass
public class ConfigProvider {

    private static final Logger log = LoggerFactory.getLogger(ConfigProvider.class);
    private static final String PROPS_FILE = "configuration.properties";
    private static final Properties FILE_PROPS = loadProps();

    public static String baseUri() {
        String uri = getString(ConfigKey.BASE_URI);
        if (uri == null) return "";
        while (uri.endsWith("/")) uri = uri.substring(0, uri.length() - 1);
        return uri;
    }

    public static String basePath() {
        String path = getString(ConfigKey.BASE_PATH);
        if (path == null || path.isBlank()) return "/";
        if (!path.startsWith("/")) path = "/" + path;
        while (path.length() > 1 && path.endsWith("/")) path = path.substring(0, path.length() - 1);
        return path;
    }

    public static boolean enableLogOnFailure() {
        return getBoolean(ConfigKey.ENABLE_LOG_ON_FAILURE);
    }

    public static boolean enableFullHttpLog() {
        return getBoolean(ConfigKey.ENABLE_FULL_HTTP_LOG);
    }

    public static int connectTimeoutMs() {
        return getInt(ConfigKey.HTTP_CONNECT_TIMEOUT_MS);
    }

    public static int readTimeoutMs() {
        return getInt(ConfigKey.HTTP_READ_TIMEOUT_MS);
    }


    public static String getString(ConfigKey key) {
        String k = key.getKey();

        String fromSys = System.getProperty(k);
        if (isNonBlank(fromSys)) return fromSys.trim();

        String fromFile = FILE_PROPS.getProperty(k);
        if (isNonBlank(fromFile)) return fromFile.trim();

        return safeTrim(key.getDefaultValue());
    }

    public static boolean getBoolean(ConfigKey key) {
        String raw = getString(key);
        if (!isNonBlank(raw)) return parseBoolDefault(key);

        String v = raw.trim().toLowerCase(Locale.ROOT);
        if ("true".equals(v)) return true;
        if ("false".equals(v)) return false;

        return parseBoolDefault(key);
    }

    public static int getInt(ConfigKey key) {
        String raw = getString(key);
        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception ignored) {
            return parseIntDefault(key, 0);
        }
    }

    private static Properties loadProps() {
        Properties p = new Properties();
        try (InputStream in = ConfigProvider.class.getClassLoader().getResourceAsStream(PROPS_FILE)) {
            if (in == null) {
                log.debug("{} not found; using -D/defaults.", PROPS_FILE);
            } else {
                p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
                log.debug("{} loaded ({} entries).", PROPS_FILE, p.size());
            }
        } catch (Exception e) {
            log.warn("Failed to load {}: {}", PROPS_FILE, e.getMessage());
        }
        return p;
    }

    private static boolean parseBoolDefault(ConfigKey key) {
        String def = key.getDefaultValue();
        return def != null && def.trim().equalsIgnoreCase("true");
    }

    private static int parseIntDefault(ConfigKey key, int fallback) {
        try {
            return Integer.parseInt(safeTrim(key.getDefaultValue()));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static boolean isNonBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
}
