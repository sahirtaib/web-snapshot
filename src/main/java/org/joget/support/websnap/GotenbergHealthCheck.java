package org.joget.support.websnap;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.support.websnap.gotenberg.GotenbergService;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONObject;

public class GotenbergHealthCheck extends ExtDefaultPlugin implements PluginWebSupport {

    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
        
        healthCheck(response);
    }

    public void healthCheck(HttpServletResponse response) {
        String message = "";
        if (Activator.getGotenbergService().isHealthy()) {
            message = AppPluginUtil.getMessage("websnap.connection.ok", getClassName(), Activator.MESSAGE_PATH);
        } else {
            message = AppPluginUtil.getMessage("websnap.connection.fail", getClassName(), Activator.MESSAGE_PATH);
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
    public String getName() {
        return getClass().getSimpleName();
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
