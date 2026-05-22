package org.joget.support.websnap;

import java.io.File;

import org.joget.commons.util.FileManager;
import org.joget.commons.util.SetupManager;

public class WebSnapFileStore extends FileManager {

    public static String getBaseDirectory() {
        return SetupManager.getBaseDirectory() + File.separator + "websnap" + File.separator;
    }
}