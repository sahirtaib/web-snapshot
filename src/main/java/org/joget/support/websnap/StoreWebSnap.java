package org.joget.support.websnap;

import java.util.Map;

import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;

public class StoreWebSnap extends DefaultApplicationPlugin {

    private static final String MESSAGE_PATH = "messages/WebSnap";

    @Override
    public Object execute(Map properties) {
        /**
         * TODO
         */
        return null;
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
        return "Store " + AppPluginUtil.getMessage("websnap.description", getClassName(), MESSAGE_PATH);
    }

    @Override
    public String getLabel() {
        return "Store " + AppPluginUtil.getMessage("websnap.label", getClassName(), MESSAGE_PATH);
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/WebSnap.json", null, true, MESSAGE_PATH);
    }

    @Override
    public String getPluginIcon() {
        return AppPluginUtil.getMessage("websnap.icon", getClassName(), MESSAGE_PATH);
    }
}
