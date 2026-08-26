package org.joget.support.websnap;

import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListActionDefault;
import org.joget.apps.datalist.model.DataListActionResult;
import org.joget.support.websnap.gotenberg.GotenbergService;
import org.joget.support.websnap.gotenberg.UrlToPdfPayload;
import org.joget.support.websnap.gotenberg.UrlToImagePayload;
import org.joget.workflow.util.WorkflowUtil;

public class FormSnap extends DataListActionDefault {
    
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
                request.getAttribute("jakarta.servlet.forward.request_uri").toString() +
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
        String fileName = (fileNameStr != null && !fileNameStr.isEmpty()) ? fileNameStr : AppPluginUtil.getMessage("websnap.fileName.default", getClassName(), Activator.MESSAGE_PATH);
        fileName += "-" + System.currentTimeMillis();

        if (snapshotCount == 1) {
            byte[] snapshot = snapshots.values().iterator().next();

            Map<String, Object> payload = new HashMap<>();
            payload.put("snapshot", snapshot);
            payload.put("fileName", fileName);
            payload.put("fileExtension", getPropertyString("fileExtension"));
            
            Response send = new Response(response, payload);
            send.singleSnapshot();
        } else {
            Map<String, Object> payload = new HashMap<>();
            payload.put("snapshots", snapshots);
            payload.put("fileName", fileName);
            payload.put("fileExtension", getPropertyString("fileExtension"));
            
            Response send = new Response(response, payload);
            send.zippedSnapshots();
        }
        
        return null;
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
        return AppPluginUtil.getMessage("websnap.version", getClassName(), Activator.MESSAGE_PATH);
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
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), Activator.PROPERTIES_PATH + "/" + Activator.SETTINGS_JSON, null, true, Activator.MESSAGE_PATH);
    }

    @Override
    public String getIcon() {
        return AppPluginUtil.getMessage("websnap.icon.camera", getClassName(), Activator.MESSAGE_PATH);
    }
}
