package com.xunlei.open.dlsdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XLDownloadAPI {
    private static final String LOGIN_TOKEN_URL = "https://open.xunlei.com/api/v1/sdk/login_token";

    static {
        NativeLoader.load();
    }

    public static final int MAX_SESSION_ID_LEN = 4096;

    public static final int DATA_PERSISTENCE_NONE = 0;
    public static final int DATA_PERSISTENCE = 1;

    public static final int TASK_STATUS_UNKOWN = 0;
    public static final int TASK_STATUS_START_WAITING = 3;
    public static final int TASK_STATUS_START_PENDING = 4;
    public static final int TASK_STATUS_STARTED = 5;
    public static final int TASK_STATUS_STOP_PENDING = 6;
    public static final int TASK_STATUS_STOPED = 7;
    public static final int TASK_STATUS_SUCCEEDED = 8;
    public static final int TASK_STATUS_FAILED = 9;

    public static final int TASK_TOKEN_NORMAL = 0;
    public static final int TASK_TOKEN_EXPIRED = 1;
    public static final int TASK_TOKEN_SESSION_EXPIRED = 2;
    public static final int TASK_TOKEN_OTHERS = 3;

    public static final int ERROR_SUCCESS = 0;
    public static final int ERROR_FAILED = 1;
    public static final int ERROR_ALREADY_INIT = 9101;
    public static final int ERROR_SDK_NOT_INIT = 9102;
    public static final int ERROR_TASK_ALREADY_EXIST = 9103;
    public static final int ERROR_TASK_NOT_EXIST = 9104;
    public static final int ERROR_TASK_ALREADY_STOPPED = 9105;
    public static final int ERROR_TASK_ALREADY_RUNNING = 9106;
    public static final int ERROR_TASK_NOT_START = 9107;
    public static final int ERROR_TASK_STILL_RUNNING = 9108;
    public static final int ERROR_FILE_EXISTED = 9109;
    public static final int ERROR_DISK_FULL = 9110;
    public static final int ERROR_TOO_MUCH_TASK = 9111;
    public static final int ERROR_PARAM_ERROR = 9112;
    public static final int ERROR_SCHEMA_NOT_SUPPORT = 9113;
    public static final int ERROR_DYNAMIC_PARAM_FAIL = 9114;
    public static final int ERROR_CONTINUE_NO_NAME = 9115;
    public static final int ERROR_APPNAME_APPKEY_ERROR = 9116;
    public static final int ERROR_CREATE_THREAD_ERROR = 9117;
    public static final int ERROR_TASK_FINISH = 9118;
    public static final int ERROR_TASK_NOT_RUNNING = 9119;
    public static final int ERROR_TASK_NOT_IDLE = 9120;
    public static final int ERROR_TASK_TYPE_NOT_SUPPORT = 9121;
    public static final int ERROR_ADD_RESOURCE_ERROR = 9122;
    public static final int ERROR_FUNCTION_NOT_SUPPORT = 9123;
    public static final int ERROR_ALREADY_HAS_FILENAME = 9124;
    public static final int ERROR_FILE_NAME_TOO_LONG = 9125;
    public static final int ERROR_ONE_PATH_LEVEL_NAME_TOO_LONG = 9126;
    public static final int ERROR_FULL_PATH_NAME_TOO_LONG = 9127;
    public static final int ERROR_FULL_PATH_NAME_OCCUPIED = 9128;
    public static final int ERROR_TASK_NO_FILE_NAME = 9129;
    public static final int ERROR_NOT_WIFI_MODE = 9130;
    public static final int ERROR_SPEED_LIMIT_TO_SMALL = 9131;
    public static final int ERROR_TASK_CONTROL_STRATEGY = 9501;
    public static final int ERROR_URL_IS_TOO_LONG = 9502;
    public static final int ERROR_FILE_DELETE_FAIL = 9503;
    public static final int ERROR_FILE_NOT_EXIST = 9504;
    public static final int ERROR_INFO_NAME_NOT_SUPPORT = 9505;
    public static final int ERROR_MEMORY_TOO_SMALL = 9601;
    public static final int ERROR_AUTH_TOKEN_VERIFY_FAILED = 9602;
    public static final int ERROR_AUTH_SCOPE_VERIFY_FAILED = 9603;
    public static final int ERROR_AUTH_SESSION_ID_VERIFY_FAILED = 9604;
    public static final int ERROR_AUTH_SESSION_ID_EXPIRED = 9605;
    public static final int ERROR_AUTH_RES_HAS_NO_QUOTA = 9606;
    public static final int ERROR_INSUFFICIENT_DISK_SPACE = 111085;
    public static final int ERROR_OPEN_FILE_ERR = 111128;
    public static final int ERROR_NO_DATA_PIPE = 111136;
    public static final int ERROR_RESTRICTION = 111151;
    public static final int ERROR_ACCOUNT_EXCEPTION = 111152;
    public static final int ERROR_RESTRICTION_AREA = 111153;
    public static final int ERROR_COPYRIGHT_BLOCKING = 111154;
    public static final int ERROR_TYPE2_BLOCKING = 111155;
    public static final int ERROR_TYPE3_BLOCKING = 111156;
    public static final int ERROR_LONG_TIME_NO_RECV_DATA = 111176;
    public static final int ERROR_TIME_OUT = 119212;
    public static final int ERROR_TASK_STATUS_ERR = 999999;

    private static native int init(InitParam param);

    public static native int uninit();

    private static native int login(String loginToken, StringGetter sessionIdGetter);

    private static native int getUnfinishedTask(long[] taskIdArray, IntGetter countGetter);

    private static native int getFinishedTask(long[] taskIdArray, IntGetter countGetter);

    private static native int createP2spTask(CreateP2spInfo createInfo, LongGetter taskIdGetter);

    public static native int startTask(long taskId);

    public static native int stopTask(long taskId);

    private static native int deleteTask(long taskId, int deleteFile);

    private static native int getTaskState(long taskId, TaskState taskState);

    private static native int getTaskInfo(long taskId, String infoName, StringGetter infoGetter);

    public static native int setConcurrentTaskCount(int count);

    public static native int setDownloadSpeedLimit(int speed);

    private static native int setUploadSwitch(int uploadSwitch);

    public static native int setUploadSpeedLimit(int speed);

    private static native int version(StringGetter versionGetter);

    public static native int setDownloadUrlAcceleration(boolean enable);

    public static int init(String appId, String appVersion, String cfgPath, boolean saveTasks) {
        InitParam param = new InitParam();
        param.appId = appId;
        param.appVersion = appVersion;
        param.cfgPath = cfgPath;
        param.saveTasks = saveTasks ? 1 : 0;
        return init(param);
    }

    public static LoginResult login(String loginToken) {
        StringGetter sessionIdGetter = new StringGetter();
        int result = login(loginToken, sessionIdGetter);
        return new LoginResult(result, result == ERROR_SUCCESS ? sessionIdGetter.value : "");
    }

    public static TaskListResult getUnfinishedTasks() {
        return getTaskList(false);
    }

    public static TaskListResult getFinishedTasks() {
        return getTaskList(true);
    }

    public static CreateTaskResult createP2spTask(String url, String savePath, String saveName) {
        CreateP2spInfo createInfo = new CreateP2spInfo();
        createInfo.url = url;
        createInfo.savePath = savePath;
        createInfo.saveName = saveName;

        LongGetter taskIdGetter = new LongGetter();
        int result = createP2spTask(createInfo, taskIdGetter);
        return new CreateTaskResult(result, taskIdGetter.value);
    }

    public static int deleteTask(long taskId, boolean deleteFile) {
        return deleteTask(taskId, deleteFile ? 1 : 0);
    }

    public static int setUploadEnabled(boolean enabled) {
        return setUploadSwitch(enabled ? 1 : 0);
    }

    public static TaskStateResult getTaskState(long taskId) {
        TaskState state = new TaskState();
        int result = getTaskState(taskId, state);
        return new TaskStateResult(result, result == ERROR_SUCCESS ? state : null);
    }

    public static StringResult getTaskInfo(long taskId, String infoName) {
        StringGetter infoGetter = new StringGetter();
        int result = getTaskInfo(taskId, infoName, infoGetter);
        return new StringResult(result, result == ERROR_SUCCESS ? infoGetter.value : "");
    }

    public static StringResult getVersion() {
        StringGetter versionGetter = new StringGetter();
        int result = version(versionGetter);
        return new StringResult(result, result == ERROR_SUCCESS ? versionGetter.value : "");
    }

    public static LoginTokenResult getLoginToken(String apiKey) throws IOException {
        return getLoginToken(apiKey, null, null);
    }

    public static LoginTokenResult getLoginToken(String apiKey, Integer expiresIn, String[] scopes)
            throws IOException {
        if (apiKey == null || apiKey.length() == 0) {
            throw new IllegalArgumentException("apiKey is required");
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(LOGIN_TOKEN_URL).openConnection();
        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("x-api-key", apiKey);
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            String body = buildLoginTokenRequestBody(expiresIn, scopes);
            if (body.length() > 0) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                OutputStream output = connection.getOutputStream();
                try {
                    output.write(body.getBytes("UTF-8"));
                } finally {
                    output.close();
                }
            }

            int httpCode = connection.getResponseCode();
            String response = readAll(httpCode >= 200 && httpCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream());
            if (httpCode < 200 || httpCode >= 300) {
                throw new IOException("login token request failed, httpCode=" + httpCode + ", body=" + response);
            }

            LoginTokenResult result = new LoginTokenResult();
            result.code = extractInt(response, "code", -1);
            result.message = extractString(response, "message");
            result.token = extractString(response, "token");
            result.expiresIn = extractInt(response, "expires_in", 0);
            return result;
        } finally {
            connection.disconnect();
        }
    }

    private static String buildLoginTokenRequestBody(Integer expiresIn, String[] scopes) {
        if (expiresIn == null && (scopes == null || scopes.length == 0)) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append('{');
        boolean hasField = false;
        if (expiresIn != null) {
            builder.append("\"expires_in\":").append(expiresIn.intValue());
            hasField = true;
        }
        if (scopes != null && scopes.length > 0) {
            if (hasField) {
                builder.append(',');
            }
            builder.append("\"scopes\":[");
            for (int i = 0; i < scopes.length; i++) {
                if (i > 0) {
                    builder.append(',');
                }
                builder.append('"').append(escapeJson(scopes[i])).append('"');
            }
            builder.append(']');
        }
        builder.append('}');
        return builder.toString();
    }

    private static String readAll(InputStream input) throws IOException {
        if (input == null) {
            return "";
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
        try {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } finally {
            reader.close();
        }
    }

    private static String extractString(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static int extractInt(String json, String key, int defaultValue) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+)").matcher(json);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : defaultValue;
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static TaskListResult getTaskList(boolean finished) {
        IntGetter countGetter = new IntGetter();
        int result = finished ? getFinishedTask(null, countGetter) : getUnfinishedTask(null, countGetter);
        if (result != ERROR_SUCCESS || countGetter.value <= 0) {
            return new TaskListResult(result, new long[0]);
        }

        long[] taskIds = new long[countGetter.value];
        result = finished ? getFinishedTask(taskIds, countGetter) : getUnfinishedTask(taskIds, countGetter);
        if (result != ERROR_SUCCESS) {
            return new TaskListResult(result, new long[0]);
        }
        if (countGetter.value < taskIds.length) {
            taskIds = Arrays.copyOf(taskIds, countGetter.value);
        }
        return new TaskListResult(result, taskIds);
    }

    private static class InitParam {
        public String appId;
        public String appVersion;
        public String cfgPath;
        public int saveTasks;
    }

    private static class CreateP2spInfo {
        public String savePath;
        public String saveName;
        public String url;
    }

    public static class TaskState {
        public long speed;
        public long totalSize;
        public long downloadedSize;
        public int stateCode;
        public int taskErrCode;
        public int taskTokenErr;
    }

    public static class TaskTrafficInfo {
        public long originSize;
        public long p2pSize;
        public long p2sSize;
        public long dcdnSize;
    }

    private static class StringGetter {
        public String value;
    }

    private static class LongGetter {
        public long value;
    }

    private static class IntGetter {
        public int value;
    }

    public static class StringResult {
        public final int result;
        public final String value;

        public StringResult(int result, String value) {
            this.result = result;
            this.value = value;
        }
    }

    public static class LoginResult {
        public final int result;
        public final String sessionId;

        public LoginResult(int result, String sessionId) {
            this.result = result;
            this.sessionId = sessionId;
        }
    }

    public static class CreateTaskResult {
        public final int result;
        public final long taskId;

        public CreateTaskResult(int result, long taskId) {
            this.result = result;
            this.taskId = taskId;
        }
    }

    public static class TaskStateResult {
        public final int result;
        public final TaskState state;

        public TaskStateResult(int result, TaskState state) {
            this.result = result;
            this.state = state;
        }
    }

    public static class TaskListResult {
        public final int result;
        public final long[] taskIds;

        public TaskListResult(int result, long[] taskIds) {
            this.result = result;
            this.taskIds = taskIds;
        }
    }

    public static class LoginTokenResult {
        public int code;
        public String token;
        public int expiresIn;
        public String message;
    }
}
