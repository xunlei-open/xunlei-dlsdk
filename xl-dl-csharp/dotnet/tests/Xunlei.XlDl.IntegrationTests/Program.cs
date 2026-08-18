using Xunlei.XlDl;

const string AppVersion = "1.0";
const string DefaultTaskUrl = "https://down.sandai.net/thunder11/XunLeiWebSetup25.0.90.1592xl11.exe";

string apiKey = Environment.GetEnvironmentVariable("API_KEY") ?? string.Empty;
Require(!string.IsNullOrEmpty(apiKey), "API_KEY environment variable is required");
string appId = Environment.GetEnvironmentVariable("APP_ID") ?? string.Empty;
Require(!string.IsNullOrEmpty(appId), "APP_ID environment variable is required");

string taskUrl = Environment.GetEnvironmentVariable("XL_DL_TEST_URL") ?? string.Empty;
if (string.IsNullOrEmpty(taskUrl))
{
    taskUrl = DefaultTaskUrl;
}
int timeoutSeconds = int.Parse(Environment.GetEnvironmentVariable("XL_DL_TEST_TIMEOUT_SECONDS") ?? "900");

XLDownloadAPI? sdk = null;
DirectoryInfo? configDir = null;
DirectoryInfo? saveDir = null;
ulong taskId = 0;

try
{
    configDir = Directory.CreateTempSubdirectory("xl-dl-csharp-cfg-");
    saveDir = Directory.CreateTempSubdirectory("xl-dl-csharp-downloads-");
    string configPath = configDir.FullName;
    string savePath = saveDir.FullName;

    sdk = new XLDownloadAPI();
    int initResult = sdk.Initialize(appId, AppVersion, configPath, saveTasks: true);
    Require(initResult == XLDownloadAPI.ErrorSuccess || initResult == XLDownloadAPI.ErrorAlreadyInit, $"init failed: {initResult}");

    try
    {
        LoginTokenResult token = await sdk.GetLoginTokenAsync(apiKey);
        if (token.Code != 0)
        {
            Console.Error.WriteLine($"warning: get loginToken failed, code={token.Code}, message={token.Message}");
        }
        else if (string.IsNullOrEmpty(token.Token))
        {
            Console.Error.WriteLine("warning: loginToken is empty, continue without login");
        }
        else
        {
            try
            {
                var login = sdk.Login(token.Token);
                if (login.Result != XLDownloadAPI.ErrorSuccess)
                {
                    Console.Error.WriteLine($"warning: login failed: {login.Result}, continue without login");
                }
                else if (string.IsNullOrEmpty(login.SessionId))
                {
                    Console.Error.WriteLine("warning: session id is empty, continue without login");
                }
            }
            catch (Exception error)
            {
                Console.Error.WriteLine($"warning: login failed: {error.Message}, continue without login");
            }
        }
    }
    catch (Exception error)
    {
        Console.Error.WriteLine($"warning: get loginToken failed: {error.Message}, continue without login");
    }

    string saveName = Path.GetFileName(new Uri(taskUrl).LocalPath);
    if (string.IsNullOrEmpty(saveName))
    {
        saveName = "download.tmp";
    }

    var create = sdk.CreateP2spTask(taskUrl, savePath, saveName);
    Require(create.Result == XLDownloadAPI.ErrorSuccess, $"create task failed: {create.Result}");
    Require(create.TaskId > 0, "task id must be greater than zero");
    taskId = create.TaskId;

    int startResult = sdk.StartTask(taskId);
    Require(startResult == XLDownloadAPI.ErrorSuccess, $"start task failed: {startResult}");

    DateTimeOffset deadline = DateTimeOffset.UtcNow.AddSeconds(timeoutSeconds);
    while (DateTimeOffset.UtcNow < deadline)
    {
        var state = sdk.GetTaskState(taskId);
        Require(state.Result == XLDownloadAPI.ErrorSuccess, $"get task state failed: {state.Result}");
        if (state.State.StateCode == XLDownloadAPI.TaskStatusSucceeded)
        {
            return;
        }
        Require(state.State.StateCode != XLDownloadAPI.TaskStatusFailed, "task failed");
        await Task.Delay(TimeSpan.FromSeconds(1));
    }

    throw new TimeoutException($"task {taskId} did not finish within {timeoutSeconds} seconds");
}
finally
{
    try
    {
        if (taskId != 0 && sdk != null)
        {
            sdk.DeleteTask(taskId, deleteFile: true);
        }
        if (sdk != null)
        {
            sdk.Uninit();
        }
    }
    finally
    {
        configDir?.Delete(recursive: true);
        saveDir?.Delete(recursive: true);
    }
}

static void Require(bool condition, string message)
{
    if (!condition)
    {
        throw new InvalidOperationException(message);
    }
}
