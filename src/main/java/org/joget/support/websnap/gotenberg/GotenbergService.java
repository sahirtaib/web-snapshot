package org.joget.support.websnap.gotenberg;

import org.joget.commons.util.LogUtil;
import org.joget.support.websnap.Activator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Before calling to Gotenberg endpoints:
 * - Gotenberg server and Joget server must be reachable to each other.
 * - GotenbergService requires manual configuration.
 * 
 * Please refer to DownloadWebSnap for usage example.
 * 
 * https://github.com/gotenberg/gotenberg
 */
public class GotenbergService {
    protected RestTemplate restTemplate;

    public GotenbergService() {
        /**
         * TODO:
         * make timeouts configurable in builder UI
         */
        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(
            new SimpleClientHttpRequestFactory() {{
                setConnectTimeout(5000);  // milliseconds
                setReadTimeout(10000);
            }}
        );
        
        restTemplate = new RestTemplate(factory);
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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = buildUrlToPdfPayload(payload);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        logRequest("urlToPdf", requestEntity);

        ResponseEntity<byte[]> response = restTemplate.postForEntity(getBaseUrl() + urlToPdfEndpoint, requestEntity, byte[].class);

        logResponse("urlToPdf", response);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException(getClassName() + ": urlToPdf endpoint failed with status " + response.getStatusCode());
        }

        if (response.getBody() != null) {
            results = response.getBody();
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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = buildUrlToImagePayload(payload);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        logRequest("urlToImage", requestEntity);

        ResponseEntity<byte[]> response = restTemplate.postForEntity(getBaseUrl() + screenshotEndpoint, requestEntity, byte[].class);

        logResponse("urlToImage", response);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException(getClassName() + ": urlToImage endpoint failed with status " + response.getStatusCode());
        }

        if (response.getBody() != null) {
            results = response.getBody();
        }
        
        return results;
    }

    /**
     * Build the multipart form data payload for urlToPdf endpoint.
     */
    private MultiValueMap<String, Object> buildUrlToPdfPayload(UrlToPdfPayload payload) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("url", payload.getTargetUrl());

        String referer = payload.getRequest().getHeader("Referer");
        String JSESSIONID = payload.getRequest().getSession(false) != null
                ? payload.getRequest().getSession(false).getId() : "";

        JSONObject extraHttpHeaders = new JSONObject();
        extraHttpHeaders.put("Cookie", "JSESSIONID=" + JSESSIONID);
        extraHttpHeaders.put("Referer", referer != null ? referer : "");
        body.add("extraHttpHeaders", extraHttpHeaders.toString());

        body.add("emulatedMediaType", payload.getEmulatedMediaType());
        body.add("printBackground", String.valueOf(payload.getPrintBackground()));
        body.add("skipNetworkIdleEvent", String.valueOf(payload.getSkipNetworkIdleEvent()));

        JSONArray extraChromiumArgs = new JSONArray();
        extraChromiumArgs.put("--window-size=" + payload.getWindowWidth() + "," + payload.getWindowHeight());
        extraChromiumArgs.put("--force-device-scale-factor=" + payload.getScale());

        body.add("extraChromiumArgs", extraChromiumArgs.toString());
        body.add("scale", String.valueOf(payload.getScale()));
        body.add("landscape", String.valueOf(payload.getLandscape()));

        body.add("paperWidth", String.valueOf(payload.getPaperWidth()));
        body.add("paperHeight", String.valueOf(payload.getPaperHeight()));

        body.add("marginTop", String.valueOf(payload.getMarginTop()));
        body.add("marginBottom", String.valueOf(payload.getMarginBottom()));
        body.add("marginLeft", String.valueOf(payload.getMarginLeft()));
        body.add("marginRight", String.valueOf(payload.getMarginRight()));

        return body;
    }

    /**
     * Build the multipart form data payload for screenshot endpoint.
     */
    private MultiValueMap<String, Object> buildUrlToImagePayload(UrlToImagePayload payload) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("url", payload.getTargetUrl());

        String referer = payload.getRequest().getHeader("Referer");
        String JSESSIONID = payload.getRequest().getSession(false) != null
                ? payload.getRequest().getSession(false).getId() : "";

        JSONObject extraHttpHeaders = new JSONObject();
        extraHttpHeaders.put("Cookie", "JSESSIONID=" + JSESSIONID);
        extraHttpHeaders.put("Referer", referer != null ? referer : "");
        body.add("extraHttpHeaders", extraHttpHeaders.toString());

        body.add("width", String.valueOf(payload.getWidth()));
        body.add("height", String.valueOf(payload.getHeight()));
        body.add("clip", String.valueOf(payload.getClip()));

        body.add("format", payload.getFormat());
        body.add("quality", String.valueOf(payload.getJpegQuality()));
        body.add("omitBackground", String.valueOf(payload.getOmitBackground()));
        body.add("optimizeForSpeed", String.valueOf(payload.getOptimizeForSpeed()));

        if (payload.getEmulatedMediaFeatures() != null) {
            body.add("emulatedMediaFeatures", payload.getEmulatedMediaFeatures());
        }

        body.add("width", String.valueOf(payload.getWidth()));
        body.add("height", String.valueOf(payload.getHeight()));

        body.add("waitDelay", payload.getWaitDelay());
        body.add("waitForExpression", payload.getWaitForExpression());
        body.add("waitForSelector", payload.getWaitForSelector());
        body.add("skipNetworkIdleEvent", String.valueOf(payload.getSkipNetworkIdleEvent()));

        return body;
    }

    public boolean pingServer() {
        try {
            ResponseEntity<Void> response = restTemplate.execute(
                getBaseUrl() + healthEndpoint, 
                HttpMethod.HEAD, 
                null, 
                clientResponse -> new ResponseEntity<>(clientResponse.getStatusCode())
            );

            /**
             * TODO:
             * add debug toggle in WebSnap.json?
             */
            if (Activator.getGotenbergService().getDebugMode()) {
                LogUtil.info(getClassName(), "[pingServer] response Status: " + response.getStatusCode());
            }
            
            return response != null && response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            LogUtil.error(getClassName(), e, e.getMessage());
            return false;
        }
    }

    private void logRequest(String origin, HttpEntity<?> request) {
        if (Activator.getGotenbergService().getDebugMode()) {
            LogUtil.info(getClassName(), "[" + origin + "] request headers: " + request.getHeaders().toString());
            LogUtil.info(getClassName(), "[" + origin + "] request body: " + request.getBody().toString());
        }
    }

    private <T> void logResponse(String origin, ResponseEntity<T> response) {
        if (Activator.getGotenbergService().getDebugMode()) {
            T body = response.getBody();
            String bodySize = "0";
            if (body instanceof byte[]) {
                bodySize = String.valueOf(((byte[]) body).length);
            }
            LogUtil.info(getClassName(), "[" + origin + "] response Status: " + response.getStatusCode() 
                + ", Content-Type: " + response.getHeaders().getContentType() 
                + ", Body size: " + bodySize + " bytes");
        }
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
