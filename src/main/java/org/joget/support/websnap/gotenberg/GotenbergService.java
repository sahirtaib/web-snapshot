package org.joget.support.websnap.gotenberg;

import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.joget.commons.util.LogUtil;
import org.joget.support.websnap.Activator;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Before calling to Gotenberg endpoints:
 * - Gotenberg server and Joget server must be reachable to each other.
 * - GotenbergService requires manual configuration.
 * 
 * Please refer to FormSnap/PageSnap for usage example.
 * 
 * https://github.com/gotenberg/gotenberg
 */
public class GotenbergService {
    protected HttpClient httpClient;

    public GotenbergService() {
        /**
         * TODO:
         * make timeouts configurable in builder UI
         */
        httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(5000))
            .build();
    }

    /**
     * Gotenberg server configurations
     */
    protected String scheme;
    protected String domain;
    protected int port;

    /**
     * Manual configuration is required before calling to Gotenberg endpoints.
     * 
     * @param scheme
     * @param domain
     * @param port
     * @param debugMode
     * @return GotenbergService
     */
    public GotenbergService configure(String scheme, String domain, int port, boolean debugMode) {
        GotenbergService gotenbergService = Activator.getGotenbergService();
        gotenbergService.setScheme(scheme);
        gotenbergService.setDomain(domain);
        gotenbergService.setPort(port);
        gotenbergService.setDebugMode(debugMode);
        return gotenbergService;
    }

    public GotenbergService configure(String scheme, String domain, int port) {
        return configure(scheme, domain, port, false);
    }

    /**
     * Endpoints
     */
    protected String healthEndpoint = "/health";
    protected String urlToPdfEndpoint = "/forms/chromium/convert/url";
    protected String screenshotEndpoint = "/forms/chromium/screenshot/url";

    protected boolean debugMode = false;

    /**
     * Convert any Joget URL to PDF by providing Joget's request to Gotenberg's Chromium engine.
     *
     * @param payload       the request payload.
     * @return              PDF as byte array.
     */
    public byte[] urlToPdf(UrlToPdfPayload payload) {
        byte[] results = null;

        try {
            Map<String, String> fields = buildUrlToPdfFields(payload);
            String boundary = getMultipartBoundary();
            byte[] requestBody = buildMultipartBody(fields, boundary);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + urlToPdfEndpoint))
                .timeout(Duration.ofMillis(10000))
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .build();

            logRequest("urlToPdf", request, requestBody);

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            logResponse("urlToPdf", response);

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException(getClassName() + ": urlToPdf endpoint failed with status " + response.statusCode());
            }

            results = response.body();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(getClassName() + ": urlToPdf request failed", e);
        }
        
        return results;
    }

    /**
     * Convert any Joget URL to an image (screenshot) via Gotenberg's Chromium engine.
     *
     * @param payload       the request payload.
     * @return              Image as byte array.
     */
    public byte[] urlToImage(UrlToImagePayload payload) {
        byte[] results = null;

        try {
            Map<String, String> fields = buildUrlToImageFields(payload);
            String boundary = getMultipartBoundary();
            byte[] requestBody = buildMultipartBody(fields, boundary);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + screenshotEndpoint))
                .timeout(Duration.ofMillis(10000))
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .build();

            logRequest("urlToImage", request, requestBody);

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            logResponse("urlToImage", response);

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException(getClassName() + ": urlToImage endpoint failed with status " + response.statusCode());
            }

            results = response.body();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(getClassName() + ": urlToImage request failed", e);
        }
        
        return results;
    }

    /**
     * Build form fields Map for urlToPdf endpoint.
     */
    private Map<String, String> buildUrlToPdfFields(UrlToPdfPayload payload) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("url", payload.getTargetUrl());

        String referer = payload.getRequest().getHeader("Referer");
        String JSESSIONID = payload.getRequest().getSession(false) != null
                ? payload.getRequest().getSession(false).getId() : "";

        JSONObject extraHttpHeaders = new JSONObject();
        extraHttpHeaders.put("Cookie", "JSESSIONID=" + JSESSIONID);
        extraHttpHeaders.put("Referer", referer != null ? referer : "");
        fields.put("extraHttpHeaders", extraHttpHeaders.toString());

        fields.put("emulatedMediaType", payload.getEmulatedMediaType());
        fields.put("printBackground", String.valueOf(payload.getPrintBackground()));
        fields.put("skipNetworkIdleEvent", String.valueOf(payload.getSkipNetworkIdleEvent()));

        JSONArray extraChromiumArgs = new JSONArray();
        extraChromiumArgs.put("--window-size=" + payload.getWindowWidth() + "," + payload.getWindowHeight());
        extraChromiumArgs.put("--force-device-scale-factor=" + String.format(Locale.US, "%.1f", payload.getScale()));

        fields.put("extraChromiumArgs", extraChromiumArgs.toString());
        fields.put("scale", String.format(Locale.US, "%.1f", payload.getScale()));
        fields.put("landscape", String.valueOf(payload.getLandscape()));

        fields.put("paperWidth", String.valueOf(payload.getPaperWidth()));
        fields.put("paperHeight", String.valueOf(payload.getPaperHeight()));

        fields.put("marginTop", String.valueOf(payload.getMarginTop()));
        fields.put("marginBottom", String.valueOf(payload.getMarginBottom()));
        fields.put("marginLeft", String.valueOf(payload.getMarginLeft()));
        fields.put("marginRight", String.valueOf(payload.getMarginRight()));

        return fields;
    }

    /**
     * Build form fields Map for screenshot endpoint.
     */
    private Map<String, String> buildUrlToImageFields(UrlToImagePayload payload) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("url", payload.getTargetUrl());

        String referer = payload.getRequest().getHeader("Referer");
        String JSESSIONID = payload.getRequest().getSession(false) != null
                ? payload.getRequest().getSession(false).getId() : "";

        JSONObject extraHttpHeaders = new JSONObject();
        extraHttpHeaders.put("Cookie", "JSESSIONID=" + JSESSIONID);
        extraHttpHeaders.put("Referer", referer != null ? referer : "");
        fields.put("extraHttpHeaders", extraHttpHeaders.toString());

        fields.put("width", String.valueOf(payload.getWidth()));
        fields.put("height", String.valueOf(payload.getHeight()));
        fields.put("clip", String.valueOf(payload.getClip()));

        fields.put("format", payload.getFormat());
        fields.put("quality", String.valueOf(payload.getJpegQuality()));
        fields.put("omitBackground", String.valueOf(payload.getOmitBackground()));
        fields.put("optimizeForSpeed", String.valueOf(payload.getOptimizeForSpeed()));

        if (payload.getEmulatedMediaFeatures() != null) {
            fields.put("emulatedMediaFeatures", payload.getEmulatedMediaFeatures());
        }

        fields.put("waitDelay", payload.getWaitDelay());
        fields.put("waitForExpression", payload.getWaitForExpression());
        fields.put("waitForSelector", payload.getWaitForSelector());
        fields.put("skipNetworkIdleEvent", String.valueOf(payload.getSkipNetworkIdleEvent()));

        return fields;
    }

    /**
     * Generate a unique multipart boundary string.
     */
    private String getMultipartBoundary() {
        return "----" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Build multipart/form-data body from field map.
     * Constructs RFC 7578 compliant multipart form data.
     */
    private byte[] buildMultipartBody(Map<String, String> fields, String boundary) {
        StringBuilder sb = new StringBuilder();
        
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"\r\n");
            sb.append("\r\n");
            sb.append(entry.getValue()).append("\r\n");
        }
        
        sb.append("--").append(boundary).append("--\r\n");
        
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public boolean isHealthy() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + healthEndpoint))
                .timeout(Duration.ofMillis(5000))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();
            
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            
            if (Activator.getGotenbergService().getDebugMode()) {
                LogUtil.info(getClassName(), "Health: " + response.statusCode());
            }
            
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception e) {
            LogUtil.error(getClassName(), e, e.getMessage());
            return false;
        }
    }

    private void logRequest(String origin, HttpRequest request, byte[] requestBody) {
        if (Activator.getGotenbergService().getDebugMode()) {
            try {
                LogUtil.info(getClassName(), "[" + origin + "] request URI: " + request.uri().toString());
            } catch (Exception e) {
                LogUtil.info(getClassName(), "[" + origin + "] request URI: unable to retrieve");
            }
            
            if (requestBody != null && requestBody.length > 0) {
                try {
                    JSONObject parsedParams = parseMultipartBody(requestBody, extractBoundaryFromContentType(request));
                    LogUtil.info(getClassName(), "[" + origin + "] request parameters: " + parsedParams.toString());
                } catch (Exception e) {
                    LogUtil.error(getClassName(), e, "Error parsing request body");
                }
            } else {
                LogUtil.info(getClassName(), "[" + origin + "] request body: empty");
            }
        }
    }

    private String extractBoundaryFromContentType(HttpRequest request) {
        Optional<String> contentType = request.headers().firstValue("Content-Type");
        if (contentType.isPresent() && contentType.get().contains("boundary=")) {
            String ct = contentType.get();
            int boundaryIndex = ct.indexOf("boundary=");
            String boundary = ct.substring(boundaryIndex + 9);
            return boundary.replaceAll("^\"|\"$", "");
        }
        return null;
    }

    private void logResponse(String origin, HttpResponse<byte[]> response) {
        if (Activator.getGotenbergService().getDebugMode()) {
            String bodySize = "0";
            if (response.body() != null) {
                bodySize = String.valueOf(response.body().length);
            }
            String contentType = response.headers().firstValue("Content-Type").orElse("unknown");
            LogUtil.info(getClassName(), "[" + origin + "] response Status: " + response.statusCode() 
                + ", Content-Type: " + contentType
                + ", Body size: " + bodySize + " bytes");
        }
    }

    /**
     * Parse multipart form data body and extract fields as JSON.
     */
    private JSONObject parseMultipartBody(byte[] bodyBytes, String boundary) {
        JSONObject params = new JSONObject();
        
        if (bodyBytes == null || bodyBytes.length == 0) {
            return params;
        }
        
        if (boundary == null) {
            params.put("error", "Could not extract boundary from Content-Type");
            return params;
        }
        
        String body = new String(bodyBytes, StandardCharsets.UTF_8);
        String boundaryDelimiter = "--" + boundary;
        String[] parts = body.split(Pattern.quote(boundaryDelimiter));
        
        for (String part : parts) {
            if (part.trim().isEmpty() || part.trim().equals("--")) {
                continue;
            }
            
            // Extract field name and value
            int headerEndIndex = part.indexOf("\r\n\r\n");
            if (headerEndIndex == -1) {
                headerEndIndex = part.indexOf("\n\n");
            }
            
            if (headerEndIndex != -1) {
                String header = part.substring(0, headerEndIndex);
                String value = part.substring(headerEndIndex).replaceAll("^\r?\n", "").replaceAll("\r?\n$", "").trim();
                
                // Extract field name from Content-Disposition header
                String namePattern = "name=\"([^\"]*)\"";
                Pattern pattern = Pattern.compile(namePattern);
                Matcher matcher = pattern.matcher(header);
                
                if (matcher.find()) {
                    String fieldName = matcher.group(1);
                    
                    // Try to parse nested JSON objects/arrays
                    if ((value.startsWith("{") && value.endsWith("}")) || 
                        (value.startsWith("[") && value.endsWith("]"))) {
                        try {
                            if (value.startsWith("{")) {
                                params.put(fieldName, new JSONObject(value));
                            } else {
                                params.put(fieldName, new JSONArray(value));
                            }
                        } catch (Exception e) {
                            params.put(fieldName, value);
                        }
                    } else {
                        params.put(fieldName, value);
                    }
                }
            }
        }
        
        return params;
    }

    public String getBaseUrl() {
        return getScheme() + "://" + getDomain() + ":" + getPort();
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String value) {
        scheme = value;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String value) {
        domain = value;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int value) {
        port = value;
    }

    public boolean getDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean value) {
        debugMode = value;
    }

    public String getClassName() {
        return getClass().getName();
    }
}
