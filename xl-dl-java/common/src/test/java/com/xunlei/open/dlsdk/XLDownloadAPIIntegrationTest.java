package com.xunlei.open.dlsdk;

import java.io.File;
import java.net.URL;

/**
 * Shared integration flow for Desktop and Android.
 *
 * <p>Platform entry points only resolve credentials and directories, then call {@link #run}.
 */
public final class XLDownloadAPIIntegrationTest {
    public static final String APP_VERSION = "1.0";
    public static final String DEFAULT_TASK_URL =
            "https://down.sandai.net/thunder11/XunLeiWebSetup25.0.90.1592xl11.exe";
    public static final long DEFAULT_TIMEOUT_SECONDS = 900L;

    private XLDownloadAPIIntegrationTest() {
    }

    public static void main(String[] args) throws Exception {
        String apiKey = env("API_KEY", "");
        require(apiKey.length() > 0, "API_KEY environment variable is required");
        String appId = env("APP_ID", "");
        require(appId.length() > 0, "APP_ID environment variable is required");

        String taskUrl = env("XL_DL_TEST_URL", DEFAULT_TASK_URL);
        long timeoutSeconds = Long.parseLong(
                env("XL_DL_TEST_TIMEOUT_SECONDS", String.valueOf(DEFAULT_TIMEOUT_SECONDS)));

        File configPath = createTempDirectory("xl-dl-java-cfg-");
        File savePath = createTempDirectory("xl-dl-java-downloads-");
        try {
            run(appId, apiKey, configPath.getAbsolutePath(), savePath.getAbsolutePath(),
                    taskUrl, timeoutSeconds);
        } finally {
            deleteRecursively(configPath);
            deleteRecursively(savePath);
        }
    }

    public static void run(
            String appId,
            String apiKey,
            String configPath,
            String savePath,
            String taskUrl,
            long timeoutSeconds) throws Exception {
        require(appId != null && appId.length() > 0, "appId is required");
        require(apiKey != null && apiKey.length() > 0, "apiKey is required");
        require(configPath != null && configPath.length() > 0, "configPath is required");
        require(savePath != null && savePath.length() > 0, "savePath is required");
        if (taskUrl == null || taskUrl.length() == 0) {
            taskUrl = DEFAULT_TASK_URL;
        }
        if (timeoutSeconds <= 0L) {
            timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
        }

        long[] taskIdHolder = new long[]{0L};
        int initResult = XLDownloadAPI.init(appId, APP_VERSION, configPath, true);
        require(initResult == XLDownloadAPI.ERROR_SUCCESS || initResult == XLDownloadAPI.ERROR_ALREADY_INIT,
                "init failed: " + initResult);

        try {
            tryLogin(apiKey);
            downloadTest(savePath, taskUrl, timeoutSeconds, taskIdHolder);
            Thread.sleep(1000L);

            int setAccelerationResult = XLDownloadAPI.setDynamicLinkAcceleration(false);
            require(setAccelerationResult == XLDownloadAPI.ERROR_SUCCESS,
                    "set dynamic link acceleration failed: " + setAccelerationResult);

            downloadTest(savePath, taskUrl, timeoutSeconds, taskIdHolder);
        } finally {
            try {
                if (taskIdHolder[0] != 0L) {
                    XLDownloadAPI.deleteTask(taskIdHolder[0], true);
                }
                XLDownloadAPI.uninit();
            } catch (Exception ignored) {
            }
        }
    }

    private static void tryLogin(String apiKey) {
        try {
            XLDownloadAPI.LoginTokenResult token = XLDownloadAPI.getLoginToken(apiKey);
            if (token.code != 0) {
                System.err.println("warning: get loginToken failed, code=" + token.code
                        + ", message=" + token.message);
            } else if (token.token == null || token.token.length() == 0) {
                System.err.println("warning: loginToken is empty, continue without login");
            } else {
                try {
                    XLDownloadAPI.LoginResult login = XLDownloadAPI.login(token.token);
                    if (login.result != XLDownloadAPI.ERROR_SUCCESS) {
                        System.err.println("warning: login failed: " + login.result
                                + ", continue without login");
                    } else if (login.sessionId == null || login.sessionId.length() == 0) {
                        System.err.println("warning: session id is empty, continue without login");
                    }
                } catch (Exception error) {
                    System.err.println("warning: login failed: " + error.getMessage()
                            + ", continue without login");
                }
            }
        } catch (Exception error) {
            System.err.println("warning: get loginToken failed: " + error.getMessage()
                    + ", continue without login");
        }
    }

    private static void downloadTest(
            String savePath,
            String taskUrl,
            long timeoutSeconds,
            long[] taskIdHolder) throws Exception {
        XLDownloadAPI.CreateTaskResult create =
                XLDownloadAPI.createP2spTask(taskUrl, savePath, saveName(taskUrl));
        int createResult = create.result;
        require(createResult == XLDownloadAPI.ERROR_SUCCESS, "create task failed: " + createResult);
        require(create.taskId > 0, "task id must be greater than zero");
        taskIdHolder[0] = create.taskId;

        int startResult = XLDownloadAPI.startTask(taskIdHolder[0]);
        require(startResult == XLDownloadAPI.ERROR_SUCCESS, "start task failed: " + startResult);

        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            XLDownloadAPI.TaskStateResult stateResult = XLDownloadAPI.getTaskState(taskIdHolder[0]);
            XLDownloadAPI.TaskState state = stateResult.state;
            int stateResultCode = stateResult.result;
            require(stateResultCode == XLDownloadAPI.ERROR_SUCCESS,
                    "get task state failed: " + stateResultCode);
            if (state.stateCode == XLDownloadAPI.TASK_STATUS_SUCCEEDED) {
                return;
            }
            require(state.stateCode != XLDownloadAPI.TASK_STATUS_FAILED, "task failed");
            Thread.sleep(1000L);
        }

        throw new RuntimeException(
                "task " + taskIdHolder[0] + " did not finish within " + timeoutSeconds + " seconds");
    }

    public static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.length() == 0 ? defaultValue : value;
    }

    public static String firstNonEmpty(String first, String second, String defaultValue) {
        if (first != null && first.length() > 0) {
            return first;
        }
        if (second != null && second.length() > 0) {
            return second;
        }
        return defaultValue;
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

    private static File createTempDirectory(String prefix) throws Exception {
        File dir = File.createTempFile(prefix, "");
        if (!dir.delete()) {
            throw new RuntimeException("failed to delete temp marker " + dir.getAbsolutePath());
        }
        if (!dir.mkdirs()) {
            throw new RuntimeException("failed to create temp directory " + dir.getAbsolutePath());
        }
        return dir;
    }

    public static void deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }
}
