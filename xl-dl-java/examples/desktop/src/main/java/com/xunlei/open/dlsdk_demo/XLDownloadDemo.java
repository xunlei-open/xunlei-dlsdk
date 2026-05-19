package com.xunlei.open.dlsdk_demo;

import com.xunlei.open.dlsdk.XLDownloadAPI;

import java.io.File;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

public class XLDownloadDemo {
    private static final String API_KEY = "xl_ba3edc87e2734c8bf177a04f3dd4xxx"; // TODO: Replace with your own API key.
    private static final String APP_ID = "eGwtcVo4SDEwMDMwAAAAAy4nxxx="; // TODO: Replace with your own app ID.
    private static final String APP_VERSION = "1.0";

    public static void main(String[] args) {

        String url = "https://down.sandai.net/mac/thunder_5.80.6.66655.dmg";

        if (args.length > 0) {
            url = args[0];
        }

        String saveName;
        try {
            saveName = new File(new URL(url).getPath()).getName();
        } catch (Exception e) {
            // 如果URL解析失败，使用默认文件名
            saveName = "download.tmp";
        }

        String savePath = "/tmp/ThunderDownload";
        String cfgPath = "/tmp/xl_dl_sdk_conf";

        //create savePath and cfgPath if not exist
        File savePathFile = new File(savePath);
        if (!savePathFile.exists()) {
            savePathFile.mkdirs();
        }
        File cfgPathFile = new File(cfgPath);
        if (!cfgPathFile.exists()) {
            cfgPathFile.mkdirs();
        }

        XLDownloadAPI.StringResult version = XLDownloadAPI.getVersion();
        System.out.println("sdk version:" + version.value);

        int initResult = XLDownloadAPI.init(APP_ID, APP_VERSION, cfgPath, true);

        System.out.println("XLDownloadAPI.init appId:" + APP_ID
            + "\n appVersion:" + APP_VERSION
            + "\n cfgPath:" + cfgPath
            + "\n saveTasks:true"
            + "\n initResult:" + initResult);

        if (initResult != XLDownloadAPI.ERROR_SUCCESS) {
            return;
        }

        XLDownloadAPI.setUploadEnabled(true);
        XLDownloadAPI.setDownloadSpeedLimit(-1);
        XLDownloadAPI.setUploadSpeedLimit(-1);
        XLDownloadAPI.setConcurrentTaskCount(5);

        String loginToken;
        try {
            loginToken = getLoginToken();
        } catch (Exception e) {
            System.out.println("获取 loginToken 失败：" + e.getMessage());
            XLDownloadAPI.uninit();
            return;
        }
        XLDownloadAPI.LoginResult login = XLDownloadAPI.login(loginToken);
        int loginResult = login.result;
        String sessionID = login.sessionId;

        System.out.println("XLDownloadAPI.login loginToken:" + loginToken
            + "\n loginResult:" + loginResult
            + "\n sessionID:" + sessionID);

        if (loginResult != XLDownloadAPI.ERROR_SUCCESS) {
//            XLDownloadAPI.uninit();
        }

        XLDownloadAPI.TaskListResult unfinishedTasks = XLDownloadAPI.getUnfinishedTasks();
        for (int nIndex = 0; nIndex < unfinishedTasks.taskIds.length; nIndex++) {
            int deleteTaskResult = XLDownloadAPI.deleteTask(unfinishedTasks.taskIds[nIndex], true);
            System.out.println("XLDownloadAPI.deleteTask of not completed, taskID:" + unfinishedTasks.taskIds[nIndex]+ ", deleteTaskResult:" + deleteTaskResult);
        }

        XLDownloadAPI.TaskListResult finishedTasks = XLDownloadAPI.getFinishedTasks();
        for (int nIndex = 0; nIndex < finishedTasks.taskIds.length; nIndex++) {
            int deleteTaskResult = XLDownloadAPI.deleteTask(finishedTasks.taskIds[nIndex], true);
            System.out.println("XLDownloadAPI.deleteTask of completed, taskID:" + finishedTasks.taskIds[nIndex]+ ", deleteTaskResult:" + deleteTaskResult);
        }

        XLDownloadAPI.CreateTaskResult create = XLDownloadAPI.createP2spTask(url, savePath, saveName);
        int createTaskResult = create.result;
        long taskID = create.taskId;

        System.out.println("XLDownloadAPI.createP2spTask savePath:" + savePath
                + "\n saveName:" + saveName
                + "\n url:" + url
                + "\n createTaskResult:" + createTaskResult
                + "\n taskID:" + taskID);

        if (createTaskResult != XLDownloadAPI.ERROR_SUCCESS) {
            XLDownloadAPI.uninit();
        }

        int startTaskResult = XLDownloadAPI.startTask(taskID);
        System.out.println("XLDownloadAPI.startTask taskID:" + taskID + ", startTaskResult:" + startTaskResult);
        if (startTaskResult != XLDownloadAPI.ERROR_SUCCESS) {
            XLDownloadAPI.deleteTask(taskID, true);
            XLDownloadAPI.uninit();
            return ;
        }

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            XLDownloadAPI.TaskStateResult taskStateResult = XLDownloadAPI.getTaskState(taskID);
            int getTaskStateResult = taskStateResult.result;
            if (getTaskStateResult != XLDownloadAPI.ERROR_SUCCESS) {
                System.out.println("XLDownloadAPI.getTaskState taskID:" + taskID + " failed:" + getTaskStateResult);
                break;
            }
            XLDownloadAPI.TaskState taskState = taskStateResult.state;

            System.out.println("XLDownloadAPI.getTaskInfo taskID:" + taskID
                    + ", stateCode:" + taskState.stateCode
                    + ", taskErrCode:" + taskState.taskErrCode
                    + ", taskTokenErr:" + taskState.taskTokenErr
                    + ", totalSize:" + taskState.totalSize
                    + ", downloadedSize:" + taskState.downloadedSize
                    + ", speed:" + taskState.speed);

            XLDownloadAPI.StringResult trafficInfoResult = XLDownloadAPI.getTaskInfo(taskID, "traffic");
            int getTrafficInfoResult = trafficInfoResult.result;
            if (getTrafficInfoResult != XLDownloadAPI.ERROR_SUCCESS) {
                System.out.println("XLDownloadAPI.getTaskInfo taskID:" + taskID + " failed:" + getTrafficInfoResult);
                break;
            }
            try {
                JSONObject trafficInfo = new JSONObject(trafficInfoResult.value);
                System.out.println("XLDownloadAPI.getTaskInfo(traffic) taskID:" + taskID
                        + ", origin_size:" + trafficInfo.getLong("origin_size")
                        + ", p2p_size:" + trafficInfo.getLong("p2p_size")
                        + ", p2s_size:" + trafficInfo.getLong("p2s_size")
                        + ", dcdn_size:" + trafficInfo.getLong("dcdn_size"));
            } catch (JSONException e) {
                e.printStackTrace();
                break;
            }

            if (taskState.stateCode == XLDownloadAPI.TASK_STATUS_SUCCEEDED
                || taskState.stateCode == XLDownloadAPI.TASK_STATUS_FAILED) {
                break;
            }
        }

        XLDownloadAPI.uninit();
        System.out.println("XLDownloadAPI.uninit");
    }

    private static String getLoginToken() throws Exception {
        XLDownloadAPI.LoginTokenResult result = XLDownloadAPI.getLoginToken(API_KEY);
        if (result.code != 0 || result.token == null || result.token.length() == 0) {
            throw new Exception("获取 loginToken 失败，code=" + result.code + ", message=" + result.message);
        }
        return result.token;
    }
}
