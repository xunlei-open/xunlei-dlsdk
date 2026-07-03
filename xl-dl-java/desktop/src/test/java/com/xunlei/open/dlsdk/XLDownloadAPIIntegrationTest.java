package com.xunlei.open.dlsdk;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public final class XLDownloadAPIIntegrationTest {
    private static final String APP_VERSION = "1.0";
    private static final String DEFAULT_TASK_URL = "https://down.sandai.net/thunder11/XunLeiSetup12.0.12.2510.exe";
    private static final long DEFAULT_TIMEOUT_SECONDS = 900L;

    private XLDownloadAPIIntegrationTest() {
    }

    public static void main(String[] args) throws Exception {
        String apiKey = env("API_KEY", "");
        require(apiKey.length() > 0, "API_KEY environment variable is required");
        String appId = env("APP_ID", "");
        require(appId.length() > 0, "APP_ID environment variable is required");

        String taskUrl = env("XL_DL_TEST_URL", DEFAULT_TASK_URL);
        long timeoutSeconds = Long.parseLong(env("XL_DL_TEST_TIMEOUT_SECONDS", String.valueOf(DEFAULT_TIMEOUT_SECONDS)));

        Path configPath = null;
        Path savePath = null;
        long taskId = 0L;
        try {
            configPath = Files.createTempDirectory("xl-dl-java-cfg-");
            savePath = Files.createTempDirectory("xl-dl-java-downloads-");
            int initResult = XLDownloadAPI.init(appId, APP_VERSION, configPath.toString(), true);
            require(initResult == XLDownloadAPI.ERROR_SUCCESS || initResult == XLDownloadAPI.ERROR_ALREADY_INIT,
                    "init failed: " + initResult);

            try {
                XLDownloadAPI.LoginTokenResult token = XLDownloadAPI.getLoginToken(apiKey);
                if (token.code != 0) {
                    System.err.println("warning: get loginToken failed, code=" + token.code + ", message=" + token.message);
                } else if (token.token == null || token.token.length() == 0) {
                    System.err.println("warning: loginToken is empty, continue without login");
                } else {
                    try {
                        XLDownloadAPI.LoginResult login = XLDownloadAPI.login(token.token);
                        if (login.result != XLDownloadAPI.ERROR_SUCCESS) {
                            System.err.println("warning: login failed: " + login.result + ", continue without login");
                        } else if (login.sessionId == null || login.sessionId.length() == 0) {
                            System.err.println("warning: session id is empty, continue without login");
                        }
                    } catch (Exception error) {
                        System.err.println("warning: login failed: " + error.getMessage() + ", continue without login");
                    }
                }
            } catch (Exception error) {
                System.err.println("warning: get loginToken failed: " + error.getMessage() + ", continue without login");
            }

            XLDownloadAPI.CreateTaskResult create = XLDownloadAPI.createP2spTask(taskUrl, savePath.toString(), saveName(taskUrl));
            int createResult = create.result;
            require(createResult == XLDownloadAPI.ERROR_SUCCESS, "create task failed: " + createResult);
            require(create.taskId > 0, "task id must be greater than zero");
            taskId = create.taskId;

            int startResult = XLDownloadAPI.startTask(taskId);
            require(startResult == XLDownloadAPI.ERROR_SUCCESS, "start task failed: " + startResult);

            long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
            while (System.currentTimeMillis() < deadline) {
                XLDownloadAPI.TaskStateResult stateResult = XLDownloadAPI.getTaskState(taskId);
                XLDownloadAPI.TaskState state = stateResult.state;
                int stateResultCode = stateResult.result;
                require(stateResultCode == XLDownloadAPI.ERROR_SUCCESS, "get task state failed: " + stateResultCode);
                if (state.stateCode == XLDownloadAPI.TASK_STATUS_SUCCEEDED) {
                    return;
                }
                require(state.stateCode != XLDownloadAPI.TASK_STATUS_FAILED, "task failed");
                Thread.sleep(1000L);
            }

            int setUrlAccelerationResult = XLDownloadAPI.setDownloadUrlAcceleration(true);
            require(setUrlAccelerationResult == XLDownloadAPI.ERROR_SUCCESS, "set download url acceleration failed: " + setUrlAccelerationResult);
            Thread.sleep(1000L);
            setUrlAccelerationResult = XLDownloadAPI.setDownloadUrlAcceleration(false);
            require(setUrlAccelerationResult == XLDownloadAPI.ERROR_SUCCESS, "set download url acceleration failed: " + setUrlAccelerationResult);

            throw new RuntimeException("task " + taskId + " did not finish within " + timeoutSeconds + " seconds");
        } finally {
            try {
                if (taskId != 0L) {
                    XLDownloadAPI.deleteTask(taskId, true);
                }
                XLDownloadAPI.uninit();
            } finally {
                deleteRecursively(configPath);
                deleteRecursively(savePath);
            }
        }
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.length() == 0 ? defaultValue : value;
    }

    private static String saveName(String taskUrl) {
        try {
            String name = new File(new URL(taskUrl).getPath()).getName();
            return name.length() == 0 ? "download.tmp" : name;
        } catch (Exception ignored) {
            return "download.tmp";
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new RuntimeException(message);
        }
    }

    private static void deleteRecursively(Path path) throws Exception {
        if (path == null || !Files.exists(path)) {
            return;
        }
        Stream<Path> walk = Files.walk(path);
        try {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(item -> {
                        try {
                            Files.deleteIfExists(item);
                        } catch (Exception ignored) {
                        }
                    });
        } finally {
            walk.close();
        }
    }
}
