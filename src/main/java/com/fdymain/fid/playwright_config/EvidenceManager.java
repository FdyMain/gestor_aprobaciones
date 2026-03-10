package com.fdymain.fid.playwright_config;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EvidenceManager {
    public static final String ROOT = "evidencias";
    public static String evidenciaFolder;

    public static void createEvidenceFolder() {
        if (evidenciaFolder == null) {
            String fechaHora = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            evidenciaFolder = ROOT + File.separator + "evidencia_" + fechaHora;
            new File(evidenciaFolder + File.separator + "screenshots").mkdirs();
            new File(evidenciaFolder + File.separator + "videos").mkdirs();
            new File(evidenciaFolder + File.separator + "traces").mkdirs();
        }
    }

    public static String getScreenshotPath(String fileName) {
        return evidenciaFolder + File.separator + "screenshots" + File.separator + fileName;
    }

    public static String getVideoPath() {
        return evidenciaFolder + File.separator + "videos";
    }

    public static String getTracePath(String fileName) {
        return evidenciaFolder + File.separator + "traces" + File.separator + fileName;
    }
}
