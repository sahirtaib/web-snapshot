package org.joget.support.websnap.gotenberg;

import javax.servlet.http.HttpServletRequest;

/**
 * https://gotenberg.dev/docs/convert-with-chromium/screenshot-url#rendering-behavior
 */
public class UrlToImagePayload {

    // Mandatory
    private final String targetUrl;
    private final HttpServletRequest request;

    // Viewport / Layout
    private final Integer width;
    private final Integer height;
    private final Boolean clip;

    // Image Format
    private final String  format;
    private final Integer jpegQuality;
    private final Boolean omitBackground;
    private final Boolean optimizeForSpeed;

    // Rendering
    private final String  emulatedMediaFeatures;

    // Wait / network
    private final String  waitDelay;
    private final String  waitForExpression;
    private final String  waitForSelector;
    private final Boolean skipNetworkIdleEvent;

    private UrlToImagePayload(Builder b) {
        targetUrl             = b.targetUrl;
        request               = b.request;
        width                 = b.width;
        height                = b.height;
        clip                  = b.clip;
        format                = b.format;
        jpegQuality           = b.jpegQuality;
        omitBackground        = b.omitBackground;
        optimizeForSpeed      = b.optimizeForSpeed;
        emulatedMediaFeatures = b.emulatedMediaFeatures;
        waitDelay             = b.waitDelay;
        waitForExpression     = b.waitForExpression;
        waitForSelector       = b.waitForSelector;
        skipNetworkIdleEvent  = b.skipNetworkIdleEvent;
    }

    // Getters
    public String             getTargetUrl()             { return targetUrl; }
    public HttpServletRequest getRequest()               { return request; }
    public Integer            getWidth()                 { return width; }
    public Integer            getHeight()                { return height; }
    public Boolean            getClip()                  { return clip; }
    public String             getFormat()                { return format; }
    public Integer            getJpegQuality()           { return jpegQuality; }
    public Boolean            getOmitBackground()        { return omitBackground; }
    public Boolean            getOptimizeForSpeed()      { return optimizeForSpeed; }
    public String             getEmulatedMediaFeatures() { return emulatedMediaFeatures; }
    public String             getWaitDelay()             { return waitDelay; }
    public String             getWaitForExpression()     { return waitForExpression; }
    public String             getWaitForSelector()       { return waitForSelector; }
    public Boolean            getSkipNetworkIdleEvent()  { return skipNetworkIdleEvent; }

    public static Builder builder(String targetUrl, HttpServletRequest request) {
        return new Builder(targetUrl, request);
    }

    public static class Builder {

        // Mandatory
        private final String targetUrl;
        private final HttpServletRequest request;

        // Viewport / Layout
        private Integer width           = 1024;
        private Integer height          = 768;
        private Boolean clip            = false;

        // Image Format
        private String  format          = "png";  // png, jpeg, webp
        private Integer jpegQuality     = 100;    // 0-100, only for jpeg
        private Boolean omitBackground  = false;
        private Boolean optimizeForSpeed = false;

        // Rendering
        private String  emulatedMediaFeatures = null;  // JSON array string

        // Wait / network
        private String  waitDelay           = null;
        private String  waitForExpression   = null;
        private String  waitForSelector     = null;
        private Boolean skipNetworkIdleEvent = true;

        private Builder(String targetUrl, HttpServletRequest request) {
            if (targetUrl == null) throw new IllegalArgumentException("targetUrl is required");
            if (request == null)   throw new IllegalArgumentException("request is required");
            this.targetUrl = targetUrl;
            this.request = request;
        }

        public Builder width(Integer val)                 { width = (val == null) ? width : val;                                                    return this; }
        public Builder height(Integer val)                { height = (val == null) ? height : val;                                                  return this; }
        public Builder clip(Boolean val)                  { clip = val;                                                                             return this; }
        public Builder format(String val)                 { format = (val == null || val.isEmpty()) ? format : val;                                 return this; }
        public Builder jpegQuality(Integer val)           { jpegQuality = (val == null) ? jpegQuality : val;                                        return this; }
        public Builder omitBackground(Boolean val)        { omitBackground = val;                                                                   return this; }
        public Builder optimizeForSpeed(Boolean val)      { optimizeForSpeed = val;                                                                 return this; }
        public Builder emulatedMediaFeatures(String val)  { emulatedMediaFeatures = (val == null || val.isEmpty()) ? emulatedMediaFeatures : val;   return this; }
        public Builder waitDelay(String val)              { waitDelay = (val == null || val.isEmpty()) ? waitDelay : val;                           return this; }
        public Builder waitForExpression(String val)      { waitForExpression = (val == null || val.isEmpty()) ? waitForExpression : val;           return this; }
        public Builder waitForSelector(String val)        { waitForSelector = (val == null || val.isEmpty()) ? waitForSelector : val;               return this; }
        public Builder skipNetworkIdleEvent(Boolean val)  { skipNetworkIdleEvent = val;                                                             return this; }

        public UrlToImagePayload build()                  { return new UrlToImagePayload(this); }
    }
}