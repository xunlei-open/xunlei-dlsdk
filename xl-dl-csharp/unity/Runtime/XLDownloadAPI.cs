// Shared types: ../../shared/XLDownloadAPI.Core.cs — keep in sync when updating.
using System;
using System.Collections;
using System.Runtime.InteropServices;
using System.Text;
using UnityEngine;
using UnityEngine.Networking;

namespace Xunlei.XlDl.Unity
{
    public sealed class XLDownloadAPI : IDisposable
    {
        public const int MaxSessionIdLength = 4096;
        public const int ErrorSuccess = 0;
        public const int ErrorAlreadyInit = 9101;
        public const int TaskStatusSucceeded = 8;
        public const int TaskStatusFailed = 9;

        private const string LoginTokenUrl = "https://open.xunlei.com/api/v1/sdk/login_token";

#if UNITY_IOS && !UNITY_EDITOR
        private const string LibraryName = "__Internal";
#else
        private const string LibraryName = "dk";
#endif

        public IEnumerator GetLoginToken(string apiKey, Action<LoginTokenResult> onCompleted, Action<string> onError = null)
        {
            return GetLoginToken(apiKey, null, null, onCompleted, onError);
        }

        public IEnumerator GetLoginToken(
            string apiKey,
            int? expiresIn,
            string[] scopes,
            Action<LoginTokenResult> onCompleted,
            Action<string> onError = null)
        {
            if (string.IsNullOrEmpty(apiKey))
            {
                onError?.Invoke("apiKey 不能为空");
                yield break;
            }

            byte[] body = Encoding.UTF8.GetBytes(BuildLoginTokenRequestBody(expiresIn, scopes));
            using (var request = new UnityWebRequest(LoginTokenUrl, UnityWebRequest.kHttpVerbPOST))
            {
                request.uploadHandler = new UploadHandlerRaw(body);
                request.downloadHandler = new DownloadHandlerBuffer();
                request.SetRequestHeader("Content-Type", "application/json");
                request.SetRequestHeader("x-api-key", apiKey);

                yield return request.SendWebRequest();

#if UNITY_2020_2_OR_NEWER
                bool failed = request.result != UnityWebRequest.Result.Success;
#else
                bool failed = request.isNetworkError || request.isHttpError;
#endif
                if (failed)
                {
                    onError?.Invoke(request.error);
                    yield break;
                }

                LoginTokenResponse response = JsonUtility.FromJson<LoginTokenResponse>(request.downloadHandler.text);
                if (response == null)
                {
                    onError?.Invoke("loginToken 响应解析失败");
                    yield break;
                }

                onCompleted?.Invoke(new LoginTokenResult
                {
                    Code = response.code,
                    Token = response.data == null ? string.Empty : response.data.token,
                    ExpiresIn = response.data == null ? 0 : response.data.expires_in,
                    Message = response.message
                });
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
                return xl_dl_init(ref param);
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
            return xl_dl_uninit();
        }

        public (int Result, string SessionId) Login(string loginToken)
        {
            var session = new byte[MaxSessionIdLength];
            int result = xl_dl_login(loginToken, session);
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
                int result = xl_dl_create_p2sp_task(ref info, ref taskId);
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
            return xl_dl_start_task(taskId);
        }

        public int StopTask(ulong taskId)
        {
            return xl_dl_stop_task(taskId);
        }

        public int DeleteTask(ulong taskId, bool deleteFile)
        {
            return xl_dl_delete_task(taskId, deleteFile ? (byte)1 : (byte)0);
        }

        public (int Result, TaskState State) GetTaskState(ulong taskId)
        {
            var state = new TaskState();
            int result = xl_dl_get_task_state(taskId, ref state);
            return (result, state);
        }

        public (int Result, string Version) GetVersion()
        {
            uint length = 0;
            int result = xl_dl_version(null, ref length);
            if (result != ErrorSuccess)
            {
                return (result, string.Empty);
            }

            var buffer = new byte[length + 1];
            result = xl_dl_version(buffer, ref length);
            return result == ErrorSuccess
                ? (result, Utf8.Read(buffer, (int)length))
                : (result, string.Empty);
        }

        public int SetDynamicLinkAcceleration(bool enable)
        {
            return xl_dl_set_dynamic_link_acceleration(enable);
        }

        public void Dispose()
        {
            Uninit();
        }

        private static string BuildLoginTokenRequestBody(int? expiresIn, string[] scopes)
        {
            var builder = new StringBuilder();
            builder.Append('{');
            bool hasField = false;
            if (expiresIn.HasValue)
            {
                builder.Append("\"expires_in\":").Append(expiresIn.Value);
                hasField = true;
            }
            if (scopes != null && scopes.Length > 0)
            {
                if (hasField)
                {
                    builder.Append(',');
                }
                builder.Append("\"scopes\":[");
                for (int i = 0; i < scopes.Length; i++)
                {
                    if (i > 0)
                    {
                        builder.Append(',');
                    }
                    builder.Append('"').Append(EscapeJson(scopes[i])).Append('"');
                }
                builder.Append(']');
            }
            builder.Append('}');
            return builder.ToString();
        }

        private static string EscapeJson(string value)
        {
            return (value ?? string.Empty).Replace("\\", "\\\\").Replace("\"", "\\\"");
        }

        [DllImport(LibraryName)]
        private static extern int xl_dl_init(ref NativeInitParam param);

        [DllImport(LibraryName)]
        private static extern int xl_dl_uninit();

        [DllImport(LibraryName)]
        private static extern int xl_dl_login([MarshalAs(UnmanagedType.LPStr)] string loginToken, byte[] session);

        [DllImport(LibraryName)]
        private static extern int xl_dl_create_p2sp_task(ref NativeCreateP2spInfo createInfo, ref ulong taskId);

        [DllImport(LibraryName)]
        private static extern int xl_dl_start_task(ulong taskId);

        [DllImport(LibraryName)]
        private static extern int xl_dl_stop_task(ulong taskId);

        [DllImport(LibraryName)]
        private static extern int xl_dl_delete_task(ulong taskId, byte deleteFile);

        [DllImport(LibraryName)]
        private static extern int xl_dl_get_task_state(ulong taskId, ref TaskState state);

        [DllImport(LibraryName)]
        private static extern int xl_dl_version(byte[] buffer, ref uint bufferLength);

        [DllImport(LibraryName)]
        private static extern int xl_dl_set_dynamic_link_acceleration(bool enable);
    }

    public sealed class LoginTokenResult
    {
        public int Code;
        public string Token;
        public int ExpiresIn;
        public string Message;
    }

    [Serializable]
    internal sealed class LoginTokenResponse
    {
        public int code;
        public LoginTokenData data;
        public string message;
    }

    [Serializable]
    internal sealed class LoginTokenData
    {
        public string token;
        public int expires_in;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct TaskState
    {
        public ulong Speed;
        public ulong TotalSize;
        public ulong DownloadedSize;
        public byte StateCode;
        public uint TaskErrorCode;
        public uint TaskTokenError;
    }

    [StructLayout(LayoutKind.Sequential)]
    internal struct NativeInitParam
    {
        public IntPtr AppId;
        public IntPtr AppVersion;
        public IntPtr ConfigPath;
        public byte SaveTasks;
    }

    [StructLayout(LayoutKind.Sequential)]
    internal struct NativeCreateP2spInfo
    {
        public IntPtr SavePath;
        public IntPtr SaveName;
        public IntPtr Url;
    }

    internal static class Utf8
    {
        internal static IntPtr Alloc(string value)
        {
            byte[] bytes = Encoding.UTF8.GetBytes(value ?? string.Empty);
            IntPtr pointer = Marshal.AllocHGlobal(bytes.Length + 1);
            Marshal.Copy(bytes, 0, pointer, bytes.Length);
            Marshal.WriteByte(pointer, bytes.Length, 0);
            return pointer;
        }

        internal static string ReadNullTerminated(byte[] buffer)
        {
            int length = Array.IndexOf(buffer, (byte)0);
            if (length < 0)
            {
                length = buffer.Length;
            }
            return Read(buffer, length);
        }

        internal static string Read(byte[] buffer, int length)
        {
            return Encoding.UTF8.GetString(buffer, 0, Math.Max(0, length)).TrimEnd('\0');
        }
    }
}
