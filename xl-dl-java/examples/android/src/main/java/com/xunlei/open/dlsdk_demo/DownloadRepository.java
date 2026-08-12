package com.xunlei.open.dlsdk_demo;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.xunlei.open.dlsdk.XLDownloadAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadRepository {
    private static final String TAG = "DownloadRepository";
    private static final String API_KEY = "xl_ba3edc87e2734c8bf177a04f3dd4xxx"; // TODO: Replace with your own API key.
    private static final String APP_ID = "eGwtcVo4SDEwMDMwAAAAAy4nxxx="; // TODO: Replace with your own app ID.
    private static final String APP_VERSION = "1.0";
    private static final int MAX_CONCURRENT_DOWNLOADS = 5;
    private static final int STATUS_CHECK_INTERVAL = 1000;
    private final MutableLiveData<List<DownloadTask>> tasksLiveData;
    private final Context context;
    private final DownloadViewModel downloadViewModel;
    private HandlerThread progressHandlerThread;
    private Handler progressHandler;
    private final ConcurrentMap<Long, DownloadTask> taskMap;
    private final AtomicBoolean isMonitoringProgress;
    private final Object taskOperationLock = new Object();
    private String mSessionID;

    public DownloadRepository(Context context, DownloadViewModel downloadViewModel) {
        this.downloadViewModel = downloadViewModel;
        this.context = context.getApplicationContext();
        this.tasksLiveData = new MutableLiveData<>(new ArrayList<>());
        progressHandlerThread = new HandlerThread("DownloadProgress");
        progressHandlerThread.start();
        progressHandler = new Handler(progressHandlerThread.getLooper());
        this.taskMap = new ConcurrentHashMap<>();
        this.isMonitoringProgress = new AtomicBoolean(false);
        initXunleiSDK(context);
        progressHandler.post(this::ensureInitialized);
    }

    @Nullable
    private void initXunleiSDK(Context context) {
        // 初始化SDK
        XLDownloadAPI.StringResult version = XLDownloadAPI.getVersion();
        int versionResult = version.result;
        if (versionResult != XLDownloadAPI.ERROR_SUCCESS) {
            Log.e(TAG, "Get SDK version failed: " + versionResult);
            return;
        }
        Log.d(TAG, "SDK version: " + version.value);

        int result = XLDownloadAPI.init(
                APP_ID,
                APP_VERSION,
                context.getFilesDir().getAbsolutePath(),
                true
        );
        if (result != XLDownloadAPI.ERROR_SUCCESS && result != XLDownloadAPI.ERROR_ALREADY_INIT) {
            Log.e(TAG, "XunLei SDK init failed: " + result);
            return;
        }

        // 设置下载配置
        XLDownloadAPI.setUploadEnabled(true);
        XLDownloadAPI.setDownloadSpeedLimit(-1);
        XLDownloadAPI.setUploadSpeedLimit(-1);
        XLDownloadAPI.setConcurrentTaskCount(MAX_CONCURRENT_DOWNLOADS);
    }

    private boolean ensureInitialized() {
        if (!innerInitXunleiSDK()) {
            return false;
        }

        startProgressMonitoring();
        loadTasks();
        return true;
    }

    private boolean innerInitXunleiSDK() {
        synchronized (taskOperationLock) {
            if (mSessionID != null) {
                return true;
            }
            try {
                // 同步获取登录token
                if (Boolean.TRUE.equals(downloadViewModel.getTokenSwitch().getValue())) {
                    String loginToken = getLoginToken();
                    if (loginToken == null) {
                        Log.e(TAG, "Get login token failed");
                        return false;
                    }
                    // 登录获取会话ID
                    Log.i(TAG, "XLDownloadAPI.login loginToken:" + loginToken);
                    XLDownloadAPI.LoginResult login = XLDownloadAPI.login(loginToken);
                    int loginResult = login.result;
                    if (loginResult != XLDownloadAPI.ERROR_SUCCESS) {
                        Log.e(TAG, "Login failed: " + loginResult);
                        return false;
                    }
                    mSessionID = login.sessionId;
                    Log.d(TAG, "Login success, sessionID: " + mSessionID);
                    return true;
                }
            } catch (Exception e) {
                Log.e(TAG, "Init XunLei SDK failed", e);
                return false;
            }
        }
        return true;
    }

    private String getTaskFileName(long taskId) {
        XLDownloadAPI.StringResult name = XLDownloadAPI.getTaskInfo(taskId, "save_name");
        int result = name.result;
        if (result != XLDownloadAPI.ERROR_SUCCESS) {
            return "Unknown";
        }

        try {
            JSONObject json = new JSONObject(name.value);
            return json.optString("save_name");
        } catch (JSONException e) {
            Log.e(TAG, "Parse task name failed", e);
        }
        return "Unknown";
    }

    public LiveData<List<DownloadTask>> getTasksLiveData() {
        return tasksLiveData;
    }

    private void startProgressMonitoring() {
        if (!isMonitoringProgress.compareAndSet(false, true)) {
            return;
        }

        progressHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!isMonitoringProgress.get()) {
                    return;
                }

                updateAllTasksProgress();
                if (isMonitoringProgress.get()) {
                    progressHandler.postDelayed(this, STATUS_CHECK_INTERVAL);
                }
            }
        });
    }

    private void stopProgressMonitoring() {
        isMonitoringProgress.set(false);
        progressHandler.removeCallbacksAndMessages(null);
    }

    private void updateAllTasksProgress() {
        boolean hasChanges = false;
        List<DownloadTask> currentTasks = new ArrayList<>(taskMap.values());

        synchronized (taskOperationLock) {
            for (DownloadTask task : currentTasks) {
                XLDownloadAPI.TaskStateResult stateResult = XLDownloadAPI.getTaskState(task.getTaskId());
                int result = stateResult.result;

                if (result != XLDownloadAPI.ERROR_SUCCESS) {
                    continue;
                }

                XLDownloadAPI.TaskState state = stateResult.state;
                if (state.stateCode != task.getStatus() || state.downloadedSize != task.getDownloadedSize() || state.speed != task.getSpeed()) {
                    DownloadTask updatedTask = new DownloadTask(task.getTaskId(), task.getFileName(), state.stateCode, state.downloadedSize, state.totalSize, state.speed);

                    taskMap.put(task.getTaskId(), updatedTask);
                    hasChanges = true;
                }
            }

            if (hasChanges) {
                notifyTasksChanged();
            }
        }
    }

    public int addDownloadTask(String url, String savaPath, String fileName) {
        if (TextUtils.isEmpty(url)) {
            Log.e(TAG, "URL cannot be empty");
            return XLDownloadAPI.ERROR_PARAM_ERROR;
        }

        synchronized (taskOperationLock) {
            try {
                if (!ensureInitialized()) {
                    return XLDownloadAPI.ERROR_SDK_NOT_INIT;
                }
                XLDownloadAPI.CreateTaskResult create = XLDownloadAPI.createP2spTask(url, savaPath, fileName);
                int result = create.result;

                if (result != XLDownloadAPI.ERROR_SUCCESS) {
                    Log.e(TAG, "Create task failed: " + result);
                    return result;
                }

                try {
                    int startResult = XLDownloadAPI.startTask(create.taskId);
                    if (startResult != XLDownloadAPI.ERROR_SUCCESS) {
                        XLDownloadAPI.deleteTask(create.taskId, true);
                        Log.e(TAG, "Failed to start task: " + startResult);
                        return startResult;
                    }

                    loadTasks();
                    return XLDownloadAPI.ERROR_SUCCESS;
                } catch (Exception e) {
                    Log.e(TAG, "Error getting task token", e);
                    XLDownloadAPI.deleteTask(create.taskId, true);
                    return XLDownloadAPI.ERROR_AUTH_TOKEN_VERIFY_FAILED;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creating download task", e);
                return XLDownloadAPI.ERROR_FAILED;
            }
        }
    }

    public void loadTasks() {
        synchronized (taskOperationLock) {
            XLDownloadAPI.TaskListResult unfinishedTasks = XLDownloadAPI.getUnfinishedTasks();
            XLDownloadAPI.TaskListResult finishedTasks = XLDownloadAPI.getFinishedTasks();

            taskMap.clear();

            for (int i = 0; i < unfinishedTasks.taskIds.length; i++) {
                DownloadTask task = getTaskInfo(unfinishedTasks.taskIds[i]);
                if (task != null) {
                    taskMap.put(task.getTaskId(), task);
                }
            }

            for (int i = 0; i < finishedTasks.taskIds.length; i++) {
                DownloadTask task = getTaskInfo(finishedTasks.taskIds[i]);
                if (task != null) {
                    taskMap.put(task.getTaskId(), task);
                }
            }

            notifyTasksChanged();
        }
    }

    public int pauseTask(long taskId) {
        if (!ensureInitialized()) {
            return XLDownloadAPI.ERROR_SDK_NOT_INIT;
        }

        synchronized (taskOperationLock) {
            int result = XLDownloadAPI.stopTask(taskId);
            if (result != XLDownloadAPI.ERROR_SUCCESS) {
                Log.e(TAG, "Failed to pause task: " + result);
                return result;
            }
            updateTaskStatus(taskId);
            return result;
        }
    }

    public int resumeTask(long taskId) {
        if (!ensureInitialized()) {
            return XLDownloadAPI.ERROR_SDK_NOT_INIT;
        }

        synchronized (taskOperationLock) {
            int result = XLDownloadAPI.startTask(taskId);
            if (result != XLDownloadAPI.ERROR_SUCCESS) {
                Log.e(TAG, "Failed to resume task: " + result);
                return result;
            }
            updateTaskStatus(taskId);
            return result;
        }
    }

    public int deleteTask(long taskId) {
        if (!ensureInitialized()) {
            return XLDownloadAPI.ERROR_SDK_NOT_INIT;
        }

        synchronized (taskOperationLock) {
            int result = XLDownloadAPI.deleteTask(taskId, true);
            if (result != XLDownloadAPI.ERROR_SUCCESS) {
                Log.e(TAG, "Failed to delete task: " + result);
                return result;
            }
            taskMap.remove(taskId);
            notifyTasksChanged();
            return result;
        }
    }

    private void updateTaskStatus(long taskId) {
        synchronized (taskOperationLock) {
            DownloadTask task = getTaskInfo(taskId);
            if (task != null) {
                taskMap.put(taskId, task);
                notifyTasksChanged();
            }
        }
    }

    private void notifyTasksChanged() {
        List<DownloadTask> tasks = new ArrayList<>(taskMap.values());
        tasksLiveData.postValue(tasks);
    }

    public void cleanup() {
        stopProgressMonitoring();
        synchronized (taskOperationLock) {
            taskMap.clear();
            XLDownloadAPI.uninit(); // 清理SDK
        }
        // 清理进度监控线程
        if (progressHandlerThread != null) {
            progressHandlerThread.quitSafely();
            try {
                progressHandlerThread.join(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, "Error stopping progress handler thread", e);
            }
            progressHandlerThread = null;
            progressHandler = null;
        }
    }

    private DownloadTask getTaskInfo(long taskId) {
        XLDownloadAPI.TaskStateResult stateResult = XLDownloadAPI.getTaskState(taskId);
        int result = stateResult.result;

        if (result != XLDownloadAPI.ERROR_SUCCESS) {
            return null;
        }

        XLDownloadAPI.TaskState state = stateResult.state;
        return new DownloadTask(taskId, getTaskFileName(taskId), state.stateCode, state.downloadedSize, state.totalSize, state.speed);
    }

    private String getLoginToken() throws Exception {
        XLDownloadAPI.LoginTokenResult result = XLDownloadAPI.getLoginToken(API_KEY);
        if (result.code != 0 || result.token == null || result.token.length() == 0) {
            throw new Exception("获取 loginToken 失败，code=" + result.code + ", message=" + result.message);
        }
        return result.token;
    }

}
