package org.joget.support.websnap.gotenberg;

import javax.servlet.http.HttpServletRequest;

/**
 * https://gotenberg.dev/docs/convert-with-chromium/convert-url-to-pdf#rendering-behavior
 */
public class UrlToPdfPayload {

    // Mandatory
    private final String targetUrl;
    private final HttpServletRequest request;

    // Page layout
    private final Boolean landscape;
    private final String  paperWidth;
    private final String  paperHeight;
    private final String  marginTop;
    private final String  marginBottom;
    private final String  marginLeft;
    private final String  marginRight;
    private final Double  scale;
    private final Boolean singlePage;
    private final Boolean preferCssPageSize;

    // Rendering
    private final String  emulatedMediaType;
    private final Boolean printBackground;
    private final Boolean omitBackground;

    // Chrome window size
    private final Integer windowWidth;
    private final Integer windowHeight;

    private final String nativePageRanges;

    // Wait / network
    private final String  waitDelay;
    private final String  waitForExpression;
    private final String  waitForSelector;
    private final Boolean skipNetworkIdleEvent;

    private UrlToPdfPayload(Builder b) {
        targetUrl            = b.targetUrl;
        request              = b.request;
        landscape            = b.landscape;
        paperWidth           = b.paperWidth;
        paperHeight          = b.paperHeight;
        marginTop            = b.marginTop;
        marginBottom         = b.marginBottom;
        marginLeft           = b.marginLeft;
        marginRight          = b.marginRight;
        scale                = b.scale;
        singlePage           = b.singlePage;
        preferCssPageSize    = b.preferCssPageSize;
        emulatedMediaType    = b.emulatedMediaType;
        printBackground      = b.printBackground;
        omitBackground       = b.omitBackground;
        windowWidth          = b.windowWidth;
        windowHeight         = b.windowHeight;
        nativePageRanges     = b.nativePageRanges;
        waitDelay            = b.waitDelay;
        waitForExpression    = b.waitForExpression;
        waitForSelector      = b.waitForSelector;
        skipNetworkIdleEvent = b.skipNetworkIdleEvent;
    }

    // Getters
    public String             getTargetUrl()            { return targetUrl; }
    public HttpServletRequest getRequest()              { return request; }
    public Boolean            getLandscape()            { return landscape; }
    public String             getPaperWidth()           { return paperWidth; }
    public String             getPaperHeight()          { return paperHeight; }
    public String             getMarginTop()            { return marginTop; }
    public String             getMarginBottom()         { return marginBottom; }
    public String             getMarginLeft()           { return marginLeft; }
    public String             getMarginRight()          { return marginRight; }
    public Double             getScale()                { return scale; }
    public Boolean            getSinglePage()           { return singlePage; }
    public Boolean            getPreferCssPageSize()    { return preferCssPageSize; }
    public String             getEmulatedMediaType()    { return emulatedMediaType; }
    public Boolean            getPrintBackground()      { return printBackground; }
    public Boolean            getOmitBackground()       { return omitBackground; }
    public Integer            getWindowWidth()          { return windowWidth; }
    public Integer            getWindowHeight()         { return windowHeight; }
    public String             getNativePageRanges()     { return nativePageRanges; }
    public String             getWaitDelay()            { return waitDelay; }
    public String             getWaitForExpression()    { return waitForExpression; }
    public String             getWaitForSelector()      { return waitForSelector; }
    public Boolean            getSkipNetworkIdleEvent() { return skipNetworkIdleEvent; }

    public static Builder builder(String targetUrl, HttpServletRequest request) {
        return new Builder(targetUrl, request);
    }

    public static class Builder {

        // Mandatory
        private final String targetUrl;
        private final HttpServletRequest request;

        // Page layout
        private Boolean landscape         = false;
        private String  paperWidth        = "8.27in";  // A4
        private String  paperHeight       = "11.7in";  // A4
        private String  marginTop         = "0";
        private String  marginBottom      = "0";
        private String  marginLeft        = "0";
        private String  marginRight       = "0";
        private Double  scale             = 1.0;
        private Boolean singlePage        = false;
        private Boolean preferCssPageSize = false;

        // Rendering
        private String  emulatedMediaType = "print";  // or "screen"
        private Boolean printBackground   = true;
        private Boolean omitBackground    = false;

        // Chrome window size
        private Integer windowWidth       = 5120;
        private Integer windowHeight      = 1440;

        // Page ranges to print, e.g. "1-5, 8, 11-13". Defaults to all pages.
        private String  nativePageRanges     = null;

        // Wait / network
        private String  waitDelay            = null;
        private String  waitForExpression    = null;
        private String  waitForSelector      = null;
        private Boolean skipNetworkIdleEvent = true;

        private Builder(String targetUrl, HttpServletRequest request) {
            if (targetUrl == null) throw new IllegalArgumentException("targetUrl is required");
            if (request == null)   throw new IllegalArgumentException("request is required");
            this.targetUrl = targetUrl;
            this.request = request;
        }

        public Builder landscape(Boolean val)            { landscape = val;                                                                 return this; }
        public Builder paperWidth(String val)            { paperWidth = (val == null || val.isEmpty()) ? paperWidth : val;                  return this; }
        public Builder paperHeight(String val)           { paperHeight = (val == null || val.isEmpty()) ? paperHeight : val;                return this; }
        public Builder marginTop(String val)             { marginTop = (val == null || val.isEmpty()) ? marginTop : val;                    return this; }
        public Builder marginBottom(String val)          { marginBottom = (val == null || val.isEmpty()) ? marginBottom : val;              return this; }
        public Builder marginLeft(String val)            { marginLeft = (val == null || val.isEmpty()) ? marginLeft : val;                  return this; }
        public Builder marginRight(String val)           { marginRight = (val == null || val.isEmpty()) ? marginRight : val;                return this; }
        public Builder scale(Double val)                 { scale = (val == null) ? scale : val;                                             return this; }
        public Builder singlePage(Boolean val)           { singlePage = val;                                                                return this; }
        public Builder preferCssPageSize(Boolean val)    { preferCssPageSize = val;                                                         return this; }
        public Builder emulatedMediaType(String val)     { emulatedMediaType = (val == null || val.isEmpty()) ? emulatedMediaType : val;    return this; }
        public Builder printBackground(Boolean val)      { printBackground = val;                                                           return this; }
        public Builder omitBackground(Boolean val)       { omitBackground = val;                                                            return this; }
        public Builder windowWidth(Integer val)          { windowWidth = (val == null) ? windowWidth : val;                                 return this; }
        public Builder windowHeight(Integer val)         { windowHeight = (val == null) ? windowHeight : val;                               return this; }
        public Builder nativePageRanges(String val)      { nativePageRanges = (val == null || val.isEmpty()) ? nativePageRanges : val;      return this; }
        public Builder waitDelay(String val)             { waitDelay = (val == null || val.isEmpty()) ? waitDelay : val;                    return this; }
        public Builder waitForExpression(String val)     { waitForExpression = (val == null || val.isEmpty()) ? waitForExpression : val;    return this; }
        public Builder waitForSelector(String val)       { waitForSelector = (val == null || val.isEmpty()) ? waitForSelector : val;        return this; }
        public Builder skipNetworkIdleEvent(Boolean val) { skipNetworkIdleEvent = val;                                                      return this; }

        public UrlToPdfPayload build()                   { return new UrlToPdfPayload(this); }
    }
}