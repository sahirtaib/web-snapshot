package org.joget.support.websnap;

import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joget.apps.app.dao.BuilderDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.BuilderDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.lib.component.ButtonComponent;
import org.joget.apps.userview.model.UserviewBuilderPalette;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.support.websnap.gotenberg.GotenbergService;
import org.joget.support.websnap.gotenberg.UrlToImagePayload;
import org.joget.support.websnap.gotenberg.UrlToPdfPayload;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;

public class PageSnap extends ButtonComponent implements PluginWebSupport {

    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setPluginProperties();

        GotenbergService gotenbergService = Activator.getGotenbergService();
        gotenbergService.configure(
            getPropertyString("gotenbergScheme"),
            getPropertyString("gotenbergDomain"),
            Integer.parseInt(getPropertyString("gotenbergPort")),
            Boolean.parseBoolean(getPropertyString("debugMode")));

        String targetUrl = getTargetUrl();
        
        byte[] snapshot = null;

        if (getPropertyString("fileExtension").equalsIgnoreCase("pdf")) {
            UrlToPdfPayload payload = UrlToPdfPayload.builder(targetUrl, request)
                .windowWidth(Integer.parseInt(getPropertyString("screenWidth")))
                .windowHeight(Integer.parseInt(getPropertyString("screenHeight")))
                .emulatedMediaType(getPropertyString("urlToPdfMediaType"))
                .printBackground(Boolean.parseBoolean(getPropertyString("urlToPdfPrintBackground")))
                .landscape(Boolean.parseBoolean(getPropertyString("urlToPdfLandscape")))
                .paperWidth(getPropertyString("urlToPdfPaperWidth"))
                .paperHeight(getPropertyString("urlToPdfPaperHeight"))
                .marginTop(getPropertyString("urlToPdfMarginTop"))
                .marginBottom(getPropertyString("urlToPdfMarginBottom"))
                .marginLeft(getPropertyString("urlToPdfMarginLeft"))
                .marginRight(getPropertyString("urlToPdfMarginRight"))
                .scale(Double.parseDouble(getPropertyString("urlToPdfScale")))
                .build();
            
            snapshot = gotenbergService.urlToPdf(payload);
        }
        else {
            String qualityStr = getPropertyString("urlToImageJpegQuality");
            Integer jpegQuality = (qualityStr != null && !qualityStr.isEmpty()) ? Integer.parseInt(qualityStr) : null;
            
            UrlToImagePayload payload = UrlToImagePayload.builder(targetUrl, request)
                .width(Integer.parseInt(getPropertyString("screenWidth")))
                .height(Integer.parseInt(getPropertyString("screenHeight")))
                .clip(Boolean.parseBoolean(getPropertyString("urlToImageClip")))
                .format(getPropertyString("fileExtension"))
                .omitBackground(Boolean.parseBoolean(getPropertyString("urlToImageOmitBackground")))
                .optimizeForSpeed(Boolean.parseBoolean(getPropertyString("urlToImageOptimizeForSpeed")))
                .jpegQuality(jpegQuality)
                .build();

            snapshot = gotenbergService.urlToImage(payload);
        }

        String fileNameStr = getPropertyString("fileName");
        String fileName = (fileNameStr != null && !fileNameStr.isEmpty()) ? fileNameStr : AppPluginUtil.getMessage("websnap.fileName.default", getClassName(), Activator.MESSAGE_PATH);
        fileName += "-" + System.currentTimeMillis();

        Map<String, Object> payload = new HashMap<>();
        payload.put("snapshot", snapshot);
        payload.put("fileName", fileName);
        payload.put("fileExtension", getPropertyString("fileExtension"));
        
        Response send = new Response(response, payload);
        send.singleSnapshot();
    }

    protected String getTargetUrl() {
        /**
         * TODO:
         * instead of relying on the request, make joget's scheme, host and port configurable in builder UI.
         * very useful to route gotenberg request to a specific joget instance.
         */
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();

        String menuId = request.getParameter("customMenuId");
        if (menuId == null || menuId.trim().isEmpty()) {
            menuId = request.getParameter("menuId");
        }

        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() +
                request.getContextPath() +
                "/web/userview/" + request.getParameter("appId") +
                "/" + request.getParameter("userviewId") +
                "/" + request.getParameter("key") +
                "/" + menuId +
                "?embed=true&" + request.getQueryString();
    }

    /**
     * wrap button in none printable div only in runtime
     * 
     * expected visual difference between ui builder vs ui runtime:
     * ui builder always apply css from ui settings to 1st html element defined in getBuilderJavaScriptTemplate()
     * this behavior breaks visual display in ui builder
     */
    @Override
    public String render(String id, String cssClass, String style, String attr, boolean isBuilder) {
        if (isBuilder) {
            attr += " data-cbuilder-textContent";
        }
        return "<div class=\"d-flex gap-1 d-print-none\"><a href=\""+ getServiceUri() +"\" "+attr+" id=\""+id+"\" class=\""+cssClass+"\">" + renderChildren() + style + "</a></div>";
    }

    @Override
    public String getPropertyOptions() {
        String pageComponentStr = AppUtil.readPluginResource(getClassName(), Activator.PROPERTIES_PATH + "/pageSnap/" + Activator.SETTINGS_JSON, null, true, Activator.MESSAGE_PATH);
        String settingsStr = AppUtil.readPluginResource(getClassName(), Activator.PROPERTIES_PATH + "/" + Activator.SETTINGS_JSON, null, true, Activator.MESSAGE_PATH);
        
        JSONArray pageComponent = new JSONArray(pageComponentStr);
        JSONArray settings = new JSONArray(settingsStr);
        
        JSONArray combined = new JSONArray();
        for (int i = 0; i < pageComponent.length(); i++) {
            combined.put(pageComponent.get(i));
        }
        for (int i = 0; i < settings.length(); i++) {
            combined.put(settings.get(i));
        }
        
        return combined.toString();
    }

    protected String getServiceUri() {
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        String userviewId = getUserview().getPropertyString("id");
        String uri = request.getContextPath() +"/web/json/app/"+ appDef.getAppId() +"/"+ appDef.getVersion().toString() +"/plugin/"+ getClassName() + "/service" +
                     "?appId=" + appDef.getAppId() + "&userviewId=" + userviewId;
        
        UserviewMenu menu = getUserview().getCurrent();
        if (menu != null) {
            String key = "&key=" + (menu.getKey() != null ? menu.getKey() : "_");
            uri += key + "&menuId=" + menu.getPropertyString("id") +
                         "&customMenuId=" + menu.getPropertyString("customId");
        }

        if (request.getQueryString() != null) {
             uri += "&" + request.getQueryString();
        }

        return uri;
    }

    protected void setPluginProperties() {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        BuilderDefinitionDao dao = (BuilderDefinitionDao) AppUtil.getApplicationContext().getBean("builderDefinitionDao");
        BuilderDefinition menuDef = dao.loadById("up-"+request.getParameter("menuId"), AppUtil.getCurrentAppDefinition());
        String path = "$..elements[?(@.className == '"+ getClassName() +"')].properties";
        List<Map<String, Object>> result = JsonPath.read(menuDef.getJson(), path);
        if (result.isEmpty()) {
            throw new RuntimeException("Plugin properties not found.");
        }
        setProperties(result.get(0));
    }

    @Override
    public String getCategory() {
        return UserviewBuilderPalette.CATEGORY_GENERAL;
    }

    @Override
    public String getIcon() {
        return AppPluginUtil.getMessage("websnap.icon.camera", getClassName(), Activator.MESSAGE_PATH);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public String getLabel() {
        return AppPluginUtil.getMessage("websnap." + getName() + ".label", getClassName(), Activator.MESSAGE_PATH);
    }

    @Override
    public String getDescription() {
        return AppPluginUtil.getMessage("websnap." + getName() + ".description", getClassName(), Activator.MESSAGE_PATH);
    }

    @Override
    public String getVersion() {
        return AppPluginUtil.getMessage("websnap.version", getClassName(), Activator.MESSAGE_PATH);
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }
}
