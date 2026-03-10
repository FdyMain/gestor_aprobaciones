package com.fdymain.fid.playwright_config;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Tracing;

import java.nio.file.Paths;

public class PlaywrightUtils {

    public static void takeScreenshot(Page page, String fullPath) {
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(fullPath)));
    }

    public static void startTracing(BrowserContext context) {
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));
    }

    public static void stopTracing(BrowserContext context, String fullPath) {
        context.tracing().stop(new Tracing.StopOptions().setPath(Paths.get(fullPath)));
    }
}
