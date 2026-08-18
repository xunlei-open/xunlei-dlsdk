using System.Collections;
using System.IO;
using UnityEngine;
using Xunlei.XlDl.Unity;

public sealed class BasicDownloadExample : MonoBehaviour
{
    [SerializeField] private string appId = "eGwtcVo4SDEwMDMwAAAAAy4nxxx="; // TODO: Replace with your own app ID.
    [SerializeField] private string apiKey = "xl_ba3edc87e2734c8bf177a04f3dd4xxx"; // TODO: Replace with your own API key.
    [SerializeField] private string appVersion = "1.0";
    [SerializeField] private string taskUrl = "https://down.sandai.net/thunder11/XunLeiWebSetup25.0.90.1592xl11.exe";

    private readonly XLDownloadAPI sdk = new XLDownloadAPI();

    private void downloadTest(string savePath) {
        string saveName = Path.GetFileName(new System.Uri(taskUrl).LocalPath);
        var create = sdk.CreateP2spTask(taskUrl, savePath, saveName);
        Debug.Log($"create task result:{create.Result} taskId:{create.TaskId}");
        if (create.Result != XLDownloadAPI.ErrorSuccess)
        {
            sdk.Uninit();
            yield break;
        }

        int startResult = sdk.StartTask(create.TaskId);
        Debug.Log($"start task result:{startResult}");
        if (startResult != XLDownloadAPI.ErrorSuccess)
        {
            sdk.Uninit();
            yield break;
        }

        while (true)
        {
            var state = sdk.GetTaskState(create.TaskId);
            Debug.Log($"task state result:{state.Result} state:{state.State.StateCode} downloaded:{state.State.DownloadedSize}/{state.State.TotalSize} speed:{state.State.Speed}");

            if (state.Result != XLDownloadAPI.ErrorSuccess ||
                state.State.StateCode == XLDownloadAPI.TaskStatusSucceeded ||
                state.State.StateCode == XLDownloadAPI.TaskStatusFailed)
            {
                break;
            }

            yield return new WaitForSeconds(1);
        }
        
    }

    private IEnumerator Start()
    {
        const string configPath = "/tmp/xl_dl_sdk_conf";
        const string savePath = "/tmp/ThunderDownload";
        Directory.CreateDirectory(configPath);
        Directory.CreateDirectory(savePath);

        var version = sdk.GetVersion();
        Debug.Log($"version result:{version.Result} version:{version.Version}");

        int initResult = sdk.Initialize(appId, appVersion, configPath, true);
        Debug.Log($"init result:{initResult}");
        if (initResult != XLDownloadAPI.ErrorSuccess && initResult != XLDownloadAPI.ErrorAlreadyInit)
        {
            yield break;
        }

        LoginTokenResult token = null;
        string tokenError = null;
        yield return sdk.GetLoginToken(apiKey, result => token = result, error => tokenError = error);
        if (!string.IsNullOrEmpty(tokenError) || token == null || token.Code != 0 || string.IsNullOrEmpty(token.Token))
        {
            Debug.LogError($"获取 loginToken 失败：{tokenError ?? token?.Message}");
            sdk.Uninit();
            yield break;
        }

        var login = sdk.Login(token.Token);
        Debug.Log($"login result:{login.Result} session:{login.SessionId}");
        if (login.Result != XLDownloadAPI.ErrorSuccess)
        {
            sdk.Uninit();
            yield break;
        }

        downloadTest(savePath);

        int setAccelerationresult = sdk.SetDynamicLinkAcceleration(false);
        Debug.Log($"Dynamic link acceleration disabled result: {setAccelerationresult}");

        downloadTest(savePath);

        int uninitResult = sdk.Uninit();
        Debug.Log($"uninit result:{uninitResult}");
    }
}
