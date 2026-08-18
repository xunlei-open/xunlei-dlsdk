package com.xunlei.open.dlsdk;

import android.content.Context;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * Android instrumentation entry for the shared {@link XLDownloadAPIIntegrationTest} flow.
 *
 * <p>Credentials come from {@code testInstrumentationRunnerArguments} (host env injected by
 * Gradle). {@code System.getenv} on-device usually cannot see the host CI/local environment.
 */
@RunWith(AndroidJUnit4.class)
public final class AndroidXLDownloadAPIIntegrationTest {
    @Test
    public void downloadFlow() throws Exception {
        Bundle args = InstrumentationRegistry.getArguments();
        String apiKey = value(args, "API_KEY", "");
        String appId = value(args, "APP_ID", "");
        String taskUrl = value(args, "XL_DL_TEST_URL", XLDownloadAPIIntegrationTest.DEFAULT_TASK_URL);
        long timeoutSeconds = Long.parseLong(value(
                args,
                "XL_DL_TEST_TIMEOUT_SECONDS",
                String.valueOf(XLDownloadAPIIntegrationTest.DEFAULT_TIMEOUT_SECONDS)));

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File configPath = new File(context.getFilesDir(), "xl-dl-java-cfg");
        File savePath = new File(context.getCacheDir(), "xl-dl-java-downloads");
        if (!configPath.mkdirs() && !configPath.isDirectory()) {
            throw new RuntimeException("failed to create config dir");
        }
        if (!savePath.mkdirs() && !savePath.isDirectory()) {
            throw new RuntimeException("failed to create save dir");
        }

        try {
            XLDownloadAPIIntegrationTest.run(
                    appId,
                    apiKey,
                    configPath.getAbsolutePath(),
                    savePath.getAbsolutePath(),
                    taskUrl,
                    timeoutSeconds);
            throw new RuntimeException("downloadFlow failed test case");
        } finally {
            XLDownloadAPIIntegrationTest.deleteRecursively(configPath);
            XLDownloadAPIIntegrationTest.deleteRecursively(savePath);
        }
    }

    private static String value(Bundle args, String name, String defaultValue) {
        String fromArgs = args == null ? null : args.getString(name);
        return fromArgs == null || fromArgs.length() == 0 ? defaultValue : fromArgs;
    }
}
