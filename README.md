# Automated Testing Framework

A lightweight, modular framework for automated API testing, built with **Java** and **Gradle**, integrated with **Allure** for comprehensive test reporting.  
Designed to test the **Open Brewery DB API**.

---

## Prerequisites

- **Java**: JDK 11 or higher
- **Allure**: Command-line tool for report generation
    - **macOS/Linux**: `brew install allure` or download from [Allure Releases](https://github.com/allure-framework/allure2/releases)
    - **Windows**: Download from [Allure Releases](https://github.com/allure-framework/allure2/releases), unzip, and add `bin` to `PATH`
- **Gradle**: Included as Gradle Wrapper (no installation needed)

---

## 🚀 Getting Started

### Build and Run Tests

To compile and execute tests:

**macOS/Linux**
```bash
  ./gradlew clean test

```

**Windows**
```bash
  gradlew.bat clean test
```

- `clean`: Removes previous build artifacts
- `test`: Runs tests and generates raw Allure results in `build/allure-results`

---

### Generate and View Allure Reports

To create and view test reports:

```bash
# Generate HTML report
allure generate build/allure-results -o build/allure-report

# Open report in browser
allure open build/allure-report
```

---

## Configuration

The `config.properties` file customizes framework behavior.  
Below are the configuration parameters and their purposes:

- **base_uri**  
  Value: `https://api.openbrewerydb.org`  
  Purpose: Specifies the base URL for API requests, defining the host for all API calls.

- **base_path**  
  Value: `/v1/breweries`  
  Purpose: Defines the base path for API endpoints, appended to `base_uri` for all requests.

- **enable_log_on_failure**  
  Value: `true`  
  Purpose: Enables detailed logging when a test fails, providing additional context for debugging.

- **enable_full_http_log**  
  Value: `false`  
  Purpose: Controls verbose HTTP request/response logging. Set to `true` for detailed logs during debugging; keep `false` to minimize output clutter.

- **http_connect_timeout_ms**  
  Value: `3000`  
  Purpose: Sets the maximum time (in milliseconds) to establish an HTTP connection, ensuring tests fail fast if the server is unresponsive.

- **http_read_timeout_ms**  
  Value: `3000`  
  Purpose: Sets the maximum time (in milliseconds) to wait for an HTTP response, preventing tests from hanging on slow responses.

### Example `config.properties`

```properties
base_uri=https://api.openbrewerydb.org
base_path=/v1/breweries
enable_log_on_failure=true
enable_full_http_log=false
http_connect_timeout_ms=3000
http_read_timeout_ms=3000
```

---

## Project Structure

- **Tests**: Located in `src/test/java/com/openbrewerydb/tests/search/`
- **Reports**: Allure reports are generated in `build/allure-report`

---

## Troubleshooting

- Ensure **JDK 11+** and **Allure** are installed correctly
- Verify `config.properties` settings if tests fail unexpectedly
- Set `enable_full_http_log=true` for detailed HTTP logs during debugging

---

