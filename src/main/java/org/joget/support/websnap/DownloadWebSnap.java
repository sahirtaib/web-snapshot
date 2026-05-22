package org.joget.support.websnap;

import java.beans.Introspector;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListActionDefault;
import org.joget.apps.datalist.model.DataListActionResult;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.support.websnap.gotenberg.GotenbergService;
import org.joget.support.websnap.gotenberg.UrlToPdfPayload;
import org.joget.support.websnap.gotenberg.UrlToImagePayload;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONObject;

public class DownloadWebSnap extends DataListActionDefault implements PluginWebSupport {

    private static final String MESSAGE_PATH = "messages/WebSnap";

    @Override
    public DataListActionResult executeAction(DataList dataList, String[] ids) {
        DataListActionResult result = new DataListActionResult();
        result.setType(DataListActionResult.TYPE_REDIRECT);
        result.setUrl("REFERER");

        if (ids == null || ids.length < 1) {
            return result;
        }
        
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null && !"POST".equalsIgnoreCase(request.getMethod())) {
            return result;
        }
 
        GotenbergService gotenbergService = Activator.getGotenbergService();
        gotenbergService.configure(
            getPropertyString("gotenbergScheme"),
            getPropertyString("gotenbergDomain"),
            Integer.parseInt(getPropertyString("gotenbergPort")),
            Boolean.parseBoolean(getPropertyString("debugMode")));

        Map<String, byte[]> snapshots = new HashMap<>();
        for (String id : ids) {
            if (id == null || id.isEmpty()) {
                continue;
            }
            
            /**
             * request.getServerName()
             * pros: cluster aware
             * cons: requires DNS name resolution
             * 
             * localhost
             * pros: DNS name resolution not required
             * cons: not cluster aware
             */

            /**
             * TODO:
             * when request object is not available, should fallback to appId, viewId, keyId and menuId provided as params
             */
            String targetUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() +
                request.getAttribute("javax.servlet.forward.request_uri").toString() +
                "?_mode=edit&embed=true&id=" + id;
            
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
            
            if (snapshot != null && snapshot.length > 0) {
                snapshots.put(id, snapshot);
            }
        }

        int snapshotCount = snapshots.size();

        if (snapshotCount == 0) {
            return result; 
        }

        HttpServletResponse response = WorkflowUtil.getHttpServletResponse();
        String fileNameStr = getPropertyString("fileName");
        String fileName = (fileNameStr != null && !fileNameStr.isEmpty()) ? fileNameStr : "web-snapshot";
        fileName += "-" + System.currentTimeMillis();

        if (snapshotCount == 1) {
            byte[] snapshot = snapshots.values().iterator().next();
            streamSingleSnapshot(request, response, snapshot, fileName);
        } else {
            streamZippedSnapshots(request, response, snapshots, fileName);
        }
        
        return null;
    }

    protected void streamSingleSnapshot(HttpServletRequest request, HttpServletResponse response, byte[] snapshot, String fileName) {
        String mimeType = getPropertyString("fileExtension").equalsIgnoreCase("pdf")
            ? "application/pdf"
            : "image/" + getPropertyString("fileExtension").toLowerCase();
        
        try {
            writeResponse(request, response, snapshot, fileName + "." + getPropertyString("fileExtension"), mimeType);
        } catch (Exception e) {
            LogUtil.error(getClassName(), e, "Error streaming file");
        }
    }

    protected void streamZippedSnapshots(HttpServletRequest request, HttpServletResponse response, Map<String, byte[]> snapshots, String fileName) {        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(baos)) {
            
            for (Map.Entry<String, byte[]> snapshot : snapshots.entrySet()) {
                String id = snapshot.getKey();
                byte[] bytes = snapshot.getValue();

                zip.putNextEntry(new ZipEntry(id + "." + getPropertyString("fileExtension")));
                zip.write(bytes);
                zip.closeEntry();
            }
            
            zip.finish();
            
            writeResponse(request, response, baos.toByteArray(), fileName + ".zip", "application/zip");
            
        } catch (Exception e) {
            LogUtil.error(getClassName(), e, "Error streaming ZIP file");
        }
    }

    protected void writeResponse(HttpServletRequest request, HttpServletResponse response, byte[] bytes, String filename, String contentType) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        OutputStream out = response.getOutputStream();
        try {
            response.setHeader("Content-Type", contentType);
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            response.setContentLength(bytes.length);
            out.write(bytes);
        } finally {
            out.flush();
            out.close();
        }
    }

    /**
     * JSON API for test connection button
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException 
     */
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //Limit the API for admin usage only
        boolean isAdmin = WorkflowUtil.isCurrentUserInRole(WorkflowUtil.ROLE_ADMIN);
        if (!isAdmin) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        String gotenbergScheme = AppUtil.processHashVariable(request.getParameter("gotenbergScheme"), null, null, null, appDef);
        String gotenbergDomain = AppUtil.processHashVariable(request.getParameter("gotenbergDomain"), null, null, null, appDef);
        String gotenbergPort = AppUtil.processHashVariable(request.getParameter("gotenbergPort"), null, null, null, appDef);

        GotenbergService gotenbergService = Activator.getGotenbergService();
        gotenbergService.configure(
            gotenbergScheme,
            gotenbergDomain,
            Integer.parseInt(gotenbergPort));
        
        String message = "";
        if (gotenbergService.pingServer()) {
            message = AppPluginUtil.getMessage("websnap.gotenberg.connection.ok", getClassName(), MESSAGE_PATH);
        } else {
            message = AppPluginUtil.getMessage("websnap.gotenberg.connection.fail", getClassName(), MESSAGE_PATH);
        }
        
        try {
            JSONObject body = new JSONObject();
            body.accumulate("message", message);
            body.write(response.getWriter());
        } catch (Exception e) {
            LogUtil.error(getClassName(), e, "Error writing JSON response");
        }
    }

    @Override
    public String getLinkLabel() {
        return getPropertyString("label");
    }

    @Override
    public String getHref() {
        return getPropertyString("href");
    }

    @Override
    public String getTarget() {
        return "post";
    }

    @Override
    public String getHrefParam() {
        return getPropertyString("hrefParam");
    }

    @Override
    public String getHrefColumn() {
        return getPropertyString("hrefColumn");
    }

    @Override
    public String getConfirmation() {
        if (isDownloadAllEntries()){
            return "";
        }
        return getPropertyString("confirmation");
    }
    
    public String getCustomFilename() {
        return getPropertyString("fileName");
    }

    public boolean isDownloadAllEntries() {
        String downloadAllEntriesStr = (String) getProperty("downloadAllEntries");
        return Boolean.parseBoolean(downloadAllEntriesStr);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public String getVersion() {
        return AppPluginUtil.getMessage("websnap.version", getClassName(), MESSAGE_PATH);
    }

    @Override
    public String getDescription() {
        return "Download " + AppPluginUtil.getMessage("websnap.description", getClassName(), MESSAGE_PATH);
    }

    @Override
    public String getLabel() {
        return "Download " + AppPluginUtil.getMessage("websnap.label", getClassName(), MESSAGE_PATH);
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/WebSnap.json", null, true, MESSAGE_PATH);
    }

    @Override
    public String getIcon() {
        return AppPluginUtil.getMessage("websnap.icon", getClassName(), MESSAGE_PATH);
    }

    public String getDecapitalizedName() {
        return Introspector.decapitalize(getName());
    }
}
