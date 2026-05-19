using System;
using System.Net.Http;
using System.Runtime.InteropServices;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

namespace Xunlei.XlDl
{
    public sealed class XLDownloadAPI : IDisposable
    {
        public const int MaxSessionIdLength = XLConstants.MaxSessionIdLength;
        public const int ErrorSuccess = XLConstants.ErrorSuccess;
        public const int ErrorAlreadyInit = XLConstants.ErrorAlreadyInit;
        public const int TaskStatusSucceeded = XLConstants.TaskStatusSucceeded;
        public const int TaskStatusFailed = XLConstants.TaskStatusFailed;

        public async Task<LoginTokenResult> GetLoginTokenAsync(string apiKey)
        {
            return await GetLoginTokenAsync(apiKey, null, null).ConfigureAwait(false);
        }

        public async Task<LoginTokenResult> GetLoginTokenAsync(string apiKey, int? expiresIn, string[] scopes)
        {
            if (string.IsNullOrEmpty(apiKey))
            {
                throw new ArgumentException("apiKey is required", nameof(apiKey));
            }

            using (var request = new HttpRequestMessage(HttpMethod.Post, XLConstants.LoginTokenUrl))
            {
                request.Headers.TryAddWithoutValidation("x-api-key", apiKey);
                string body = JsonHelper.BuildLoginTokenRequestBody(expiresIn, scopes);
                if (body.Length > 0)
                {
                    request.Content = new StringContent(body, System.Text.Encoding.UTF8, "application/json");
                }

                using (var http = new HttpClient())
                using (var response = await http.SendAsync(request).ConfigureAwait(false))
                {
                    string responseBody = await response.Content.ReadAsStringAsync().ConfigureAwait(false);
                    if (!response.IsSuccessStatusCode)
                    {
                        throw new InvalidOperationException(
                            "获取 loginToken 失败，HTTP " + (int)response.StatusCode + "：" + responseBody);
                    }

                    return new LoginTokenResult
                    {
                        Code = ExtractInt(responseBody, "code", -1),
                        Token = ExtractString(responseBody, "token"),
                        ExpiresIn = ExtractInt(responseBody, "expires_in", 0),
                        Message = ExtractString(responseBody, "message")
                    };
                }
            }
        }

        public int Initialize(string appId, string appVersion, string configPath, bool saveTasks)
        {
            var param = new NativeInitParam
            {
                AppId = Utf8.Alloc(appId),
                AppVersion = Utf8.Alloc(appVersion),
                ConfigPath = Utf8.Alloc(configPath),
                SaveTasks = saveTasks ? (byte)1 : (byte)0
            };

            try
            {
                return NativeMethods.xl_dl_init(ref param);
            }
            finally
            {
                Marshal.FreeHGlobal(param.AppId);
                Marshal.FreeHGlobal(param.AppVersion);
                Marshal.FreeHGlobal(param.ConfigPath);
            }
        }

        public int Uninit()
        {
            return NativeMethods.xl_dl_uninit();
        }

        public (int Result, string SessionId) Login(string loginToken)
        {
            var session = new byte[MaxSessionIdLength];
            int result = NativeMethods.xl_dl_login(loginToken, session);
            return result == ErrorSuccess
                ? (result, Utf8.ReadNullTerminated(session))
                : (result, string.Empty);
        }

        public (int Result, ulong TaskId) CreateP2spTask(string url, string savePath, string saveName)
        {
            var info = new NativeCreateP2spInfo
            {
                SavePath = Utf8.Alloc(savePath),
                SaveName = Utf8.Alloc(saveName),
                Url = Utf8.Alloc(url)
            };

            try
            {
                ulong taskId = 0;
                int result = NativeMethods.xl_dl_create_p2sp_task(ref info, ref taskId);
                return (result, taskId);
            }
            finally
            {
                Marshal.FreeHGlobal(info.SavePath);
                Marshal.FreeHGlobal(info.SaveName);
                Marshal.FreeHGlobal(info.Url);
            }
        }

        public int StartTask(ulong taskId)
        {
            return NativeMethods.xl_dl_start_task(taskId);
        }

        public int StopTask(ulong taskId)
        {
            return NativeMethods.xl_dl_stop_task(taskId);
        }

        public int DeleteTask(ulong taskId, bool deleteFile)
        {
            return NativeMethods.xl_dl_delete_task(taskId, deleteFile ? (byte)1 : (byte)0);
        }

        public (int Result, TaskState State) GetTaskState(ulong taskId)
        {
            var state = new TaskState();
            int result = NativeMethods.xl_dl_get_task_state(taskId, ref state);
            return (result, state);
        }

        public (int Result, string Version) GetVersion()
        {
            uint length = 0;
            int result = NativeMethods.xl_dl_version(null, ref length);
            if (result != ErrorSuccess)
            {
                return (result, string.Empty);
            }

            var buffer = new byte[length + 1];
            result = NativeMethods.xl_dl_version(buffer, ref length);
            return result == ErrorSuccess
                ? (result, Utf8.Read(buffer, (int)length))
                : (result, string.Empty);
        }

        public void Dispose()
        {
            Uninit();
        }

        private static string ExtractString(string json, string key)
        {
            var match = Regex.Match(json, "\"" + Regex.Escape(key) + "\"\\s*:\\s*\"([^\"]*)\"");
            return match.Success ? match.Groups[1].Value : string.Empty;
        }

        private static int ExtractInt(string json, string key, int defaultValue)
        {
            var match = Regex.Match(json, "\"" + Regex.Escape(key) + "\"\\s*:\\s*(-?\\d+)");
            return match.Success ? int.Parse(match.Groups[1].Value) : defaultValue;
        }
    }

    public sealed class LoginTokenResult
    {
        public int Code { get; set; }
        public string Token { get; set; }
        public int ExpiresIn { get; set; }
        public string Message { get; set; }
    }

    internal static class NativeMethods
    {
        private const string LibraryName = "dk";

        [DllImport(LibraryName, CallingConvention = CallingConvention.Cdecl)]
        internal static extern int xl_dl_init(ref NativeInitParam param);

        [DllImport(LibraryName, CallingConvention = CallingConvention.Cdecl)]
        internal static extern int xl_dl_uninit();

        [DllImport(LibraryName, CallingConvention = CallingConvention.Cdecl)]
        internal static extern int xl_dl_login([MarshalAs(UnmanagedType.LPStr)] string loginToken, byte[] session);

        [DllImport(LibraryName, CallingConvention = CallingConvention.Cdecl)]
        internal static extern int xl_dl_create_p2sp_task(ref NativeCreateP2spInfo createInfo, ref ulong taskId);

        [DllImport(LibraryName, CallingConvention = CallingConvention.Cdecl)]
        internal static extern int xl_dl_start_task(ulong taskId);

        [DllImport(LibraryName, CallingConvention = CallingConvention.Cdecl)]
        internal static extern int xl_dl_stop_task(ulong taskId);

        [DllImport(LibraryName, CallingConvention = CallingConvention.Cdecl)]
        internal static extern int xl_dl_delete_task(ulong taskId, byte deleteFile);

        [DllImport(LibraryName, CallingConvention = CallingConvention.Cdecl)]
        internal static extern int xl_dl_get_task_state(ulong taskId, ref TaskState state);

        [DllImport(LibraryName, CallingConvention = CallingConvention.Cdecl)]
        internal static extern int xl_dl_version(byte[] buffer, ref uint bufferLength);
    }
}
