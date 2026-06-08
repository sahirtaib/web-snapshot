package org.joget.support.websnap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.joget.commons.util.LogUtil;

public class Response {
    private final HttpServletResponse response;
    private final Map<String, Object> payload;

    public Response(HttpServletResponse response, Map<String, Object> payload) {
        this.response = response;
        this.payload = payload;
    }

    public void singleSnapshot() {
        byte[] snapshot = (byte[]) payload.get("snapshot");
        String fileName = (String) payload.get("fileName");
        String mimeType = getMimeType();
        try {
            writeResponse(snapshot, fileName + "." + payload.get("fileExtension"), mimeType);
        } catch (Exception e) {
            LogUtil.error(getClassName(), e, "Error streaming file");
        }
    }

    public void zippedSnapshots() {
        @SuppressWarnings("unchecked")
        Map<String, byte[]> snapshots = (Map<String, byte[]>) payload.get("snapshots");
        String fileName = (String) payload.get("fileName");
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(baos)) {
            
            for (Map.Entry<String, byte[]> snapshot : snapshots.entrySet()) {
                zip.putNextEntry(new ZipEntry(snapshot.getKey() + "." + payload.get("fileExtension")));
                zip.write(snapshot.getValue());
                zip.closeEntry();
            }
            zip.finish();
            writeResponse(baos.toByteArray(), fileName + ".zip", "application/zip");
            
        } catch (Exception e) {
            LogUtil.error(getClassName(), e, "Error streaming ZIP file");
        }
    }

    private void writeResponse(byte[] bytes, String filename, String contentType) throws IOException {
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

    private String getMimeType() {
        String ext = (String) payload.get("fileExtension");
        return ext.equalsIgnoreCase("pdf") ? "application/pdf" : "image/" + ext.toLowerCase();
    }

    public String getClassName() {
        return getClass().getName();
    }
}