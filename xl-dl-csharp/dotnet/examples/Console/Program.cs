using Xunlei.XlDl;

const string AppId = "eGwtcVo4SDEwMDMwAAAAAy4nxxx="; // TODO: Replace with your own app ID.
const string ApiKey = "xl_ba3edc87e2734c8bf177a04f3dd4xxx"; // TODO: Replace with your own API key.
const string AppVersion = "1.0";
const string TaskUrl = "https://down.sandai.net/thunder11/XunLeiSetup12.0.12.2510.exe";

const string configPath = "/tmp/xl_dl_sdk_conf";
const string savePath = "/tmp/ThunderDownload";
Directory.CreateDirectory(configPath);
Directory.CreateDirectory(savePath);

using var sdk = new XLDownloadAPI();

var version = sdk.GetVersion();
Console.WriteLine($"version result:{version.Result} version:{version.Version}");

int initResult = sdk.Initialize(AppId, AppVersion, configPath, saveTasks: true);
Console.WriteLine($"init result:{initResult}");
if (initResult != XLDownloadAPI.ErrorSuccess &&
    initResult != XLDownloadAPI.ErrorAlreadyInit)
{
    throw new InvalidOperationException($"init failed: {initResult}");
}

var token = await sdk.GetLoginTokenAsync(ApiKey);
var login = sdk.Login(token.Token);
Console.WriteLine($"login result:{login.Result} session:{login.SessionId}");
if (login.Result != XLDownloadAPI.ErrorSuccess)
{
    throw new InvalidOperationException($"login failed: {login.Result}");
}

string saveName = Path.GetFileName(new Uri(TaskUrl).LocalPath);
var create = sdk.CreateP2spTask(TaskUrl, savePath, saveName);
Console.WriteLine($"create task result:{create.Result} taskId:{create.TaskId}");
if (create.Result != XLDownloadAPI.ErrorSuccess)
{
    throw new InvalidOperationException($"create task failed: {create.Result}");
}

int startResult = sdk.StartTask(create.TaskId);
Console.WriteLine($"start task result:{startResult}");
if (startResult != XLDownloadAPI.ErrorSuccess)
{
    throw new InvalidOperationException($"start task failed: {startResult}");
}

while (true)
{
    var state = sdk.GetTaskState(create.TaskId);
    Console.Write($"\rtask state result:{state.Result} state:{state.State.StateCode} downloaded:{state.State.DownloadedSize}/{state.State.TotalSize} speed:{state.State.Speed}");

    if (state.State.StateCode == XLDownloadAPI.TaskStatusSucceeded ||
        state.State.StateCode == XLDownloadAPI.TaskStatusFailed)
    {
        Console.WriteLine();
        break;
    }

    await Task.Delay(TimeSpan.FromSeconds(1));
}
