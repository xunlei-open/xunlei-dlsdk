using System;
using System.Runtime.InteropServices;
using System.Text;

namespace Xunlei.XlDl
{
    public static class XLConstants
    {
        public const int MaxSessionIdLength = 4096;
        public const int ErrorSuccess = 0;
        public const int ErrorAlreadyInit = 9101;

        public const int TaskStatusUnknown = 0;
        public const int TaskStatusStartWaiting = 3;
        public const int TaskStatusStartPending = 4;
        public const int TaskStatusStarted = 5;
        public const int TaskStatusStopPending = 6;
        public const int TaskStatusStopped = 7;
        public const int TaskStatusSucceeded = 8;
        public const int TaskStatusFailed = 9;

        public const string LoginTokenUrl = "https://open.xunlei.com/api/v1/sdk/login_token";
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

    internal static class JsonHelper
    {
        internal static string BuildLoginTokenRequestBody(int? expiresIn, string[] scopes)
        {
            if (!expiresIn.HasValue && (scopes == null || scopes.Length == 0))
            {
                return string.Empty;
            }

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

        internal static string EscapeJson(string value)
        {
            return (value ?? string.Empty).Replace("\\", "\\\\").Replace("\"", "\\\"");
        }
    }
}
