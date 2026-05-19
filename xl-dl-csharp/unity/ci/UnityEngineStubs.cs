using System;

namespace UnityEngine
{
    public static class JsonUtility
    {
        public static T FromJson<T>(string json)
        {
            return default(T);
        }
    }
}

namespace UnityEngine.Networking
{
    public class UnityWebRequest : IDisposable
    {
        public const string kHttpVerbPOST = "POST";

        public enum Result
        {
            InProgress,
            Success,
            ConnectionError,
            ProtocolError,
            DataProcessingError
        }

        public UploadHandler uploadHandler;
        public DownloadHandler downloadHandler;
        public Result result;
        public bool isNetworkError;
        public bool isHttpError;
        public string error;

        public UnityWebRequest(string url, string method)
        {
        }

        public void SetRequestHeader(string name, string value)
        {
        }

        public object SendWebRequest()
        {
            return null;
        }

        public void Dispose()
        {
        }
    }

    public class UploadHandler
    {
    }

    public sealed class UploadHandlerRaw : UploadHandler
    {
        public UploadHandlerRaw(byte[] data)
        {
        }
    }

    public class DownloadHandler
    {
        public string text;
    }

    public sealed class DownloadHandlerBuffer : DownloadHandler
    {
    }
}
