package com.xunlei.open.dlsdk_demo;

public class DownloadTask {
    private final long taskId;
    private final String fileName;
    private final int status;
    private final long downloadedSize;
    private final long totalSize;
    private final long speed;

    public DownloadTask(long taskId, String fileName, int status,
                        long downloadedSize, long totalSize, long speed) {
        this.taskId = taskId;
        this.fileName = fileName;
        this.status = status;
        this.downloadedSize = downloadedSize;
        this.totalSize = totalSize;
        this.speed = speed;
    }

    // Getters
    public long getTaskId() { return taskId; }
    public String getFileName() { return fileName; }
    public int getStatus() { return status; }
    public long getDownloadedSize() { return downloadedSize; }
    public long getTotalSize() { return totalSize; }
    public long getSpeed() { return speed; }

    public int getProgress() {
        return totalSize > 0 ? (int) ((float) downloadedSize / totalSize * 100) : 0;
    }
}
