package com.xunlei.open.dlsdk_demo;

import static android.content.Context.MODE_PRIVATE;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.xunlei.open.dlsdk.XLDownloadAPI;

import java.util.List;
import java.util.Objects;

public class DownloadViewModel extends AndroidViewModel {
    private final DownloadRepository repository;
    private final MutableLiveData<Integer> downloadingCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> completedCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> totalCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> operationResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> tokenSwitch = new MutableLiveData<>();
    private final SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "DownloadPrefs";
    private static final String KEY_TOKEN_SWITCH = "tokenSwitch";

    public DownloadViewModel(@NonNull Application application) {
        super(application);
        repository = new DownloadRepository(application, this);
        sharedPreferences = getApplication().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean savedState = sharedPreferences.getBoolean(KEY_TOKEN_SWITCH, false);
        tokenSwitch.setValue(savedState);
        // Observe tasks changes to update counts
        repository.getTasksLiveData().observeForever(tasks -> {
            int downloading = 0;
            int completed = 0;

            for (DownloadTask task : tasks) {
                if (task.getStatus() == XLDownloadAPI.TASK_STATUS_SUCCEEDED) {
                    completed++;
                } else if (task.getStatus() == XLDownloadAPI.TASK_STATUS_STARTED) {
                    downloading++;
                }
            }

            downloadingCount.setValue(downloading);
            completedCount.setValue(completed);
            totalCount.setValue(tasks.size());
        });
    }

    public LiveData<List<DownloadTask>> getTasks() {
        return repository.getTasksLiveData();
    }

    public LiveData<Integer> getDownloadingCount() {
        return downloadingCount;
    }

    public LiveData<Integer> getCompletedCount() {
        return completedCount;
    }

    public LiveData<Integer> getTotalCount() {
        return totalCount;
    }

    public LiveData<Integer> getOperationResult() {
        return operationResult;
    }

    public LiveData<Boolean> getTokenSwitch() {
        return tokenSwitch;
    }

    public void addDownloadTask(String url) {
        // 在后台线程执行下载任务创建
        new Thread(() -> {
            String saveName = URLUtil.guessFileName(url, null, null);
            int result = repository.addDownloadTask(url, "/tmp/ThunderDownload", saveName);
            operationResult.postValue(result);
        }).start();
    }

    public void pauseTask(long taskId) {
        new Thread(() -> {
            int result = repository.pauseTask(taskId);
            operationResult.postValue(result);
        }).start();
    }

    public void resumeTask(long taskId) {
        new Thread(() -> {
            int result = repository.resumeTask(taskId);
            operationResult.postValue(result);
        }).start();
    }

    public void deleteTask(long taskId) {
        new Thread(() -> {
            int result = repository.deleteTask(taskId);
            operationResult.postValue(result);
        }).start();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.cleanup();
    }

    public void setTokenSwitch(Boolean value) {
        tokenSwitch.setValue(value);
        sharedPreferences.edit().putBoolean(KEY_TOKEN_SWITCH, value).apply();
    }
}
