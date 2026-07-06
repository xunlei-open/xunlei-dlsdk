import CXlDl
import Foundation

public let XLDLSuccess = Int32(XL_DL_ERROR_SUCCESS.rawValue)
public let XLDLAlreadyInit = Int32(XL_DL_ERROR_ALREADY_INIT.rawValue)

public let XLDownloadTaskStatusSucceeded = UInt8(XL_DL_TASK_STATUS_SUCCEEDED.rawValue)
public let XLDownloadTaskStatusFailed = UInt8(XL_DL_TASK_STATUS_FAILED.rawValue)

public struct InitParam {
    public var appId: String
    public var appVersion: String
    public var configPath: String
    public var saveTasks: Bool

    public init(appId: String, appVersion: String, configPath: String, saveTasks: Bool) {
        self.appId = appId
        self.appVersion = appVersion
        self.configPath = configPath
        self.saveTasks = saveTasks
    }
}

public struct TaskState {
    public let speed: UInt64
    public let totalSize: UInt64
    public let downloadedSize: UInt64
    public let stateCode: UInt8
    public let taskErrorCode: UInt32
    public let taskTokenError: UInt32
}

public struct LoginTokenResult {
    public let code: Int
    public let token: String
    public let expiresIn: Int
    public let message: String
}

public final class XLDownloadAPI {
    private let loginTokenURL = URL(string: "https://open.xunlei.com/api/v1/sdk/login_token")!

    public init() {}

    public func getLoginToken(apiKey: String) throws -> LoginTokenResult {
        try getLoginToken(apiKey: apiKey, expiresIn: nil, scopes: nil)
    }

    public func getLoginToken(apiKey: String, expiresIn: Int32?, scopes: [String]?) throws -> LoginTokenResult {
        var request = URLRequest(url: loginTokenURL)
        request.httpMethod = "POST"
        request.setValue(apiKey, forHTTPHeaderField: "x-api-key")
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        var body: [String: Any] = [:]
        if let expiresIn = expiresIn {
            body["expires_in"] = expiresIn
        }
        if let scopes = scopes, !scopes.isEmpty {
            body["scopes"] = scopes
        }
        if !body.isEmpty {
            request.httpBody = try JSONSerialization.data(withJSONObject: body)
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        }

        let semaphore = DispatchSemaphore(value: 0)
        var result: Result<LoginTokenResult, Error>!
        URLSession.shared.dataTask(with: request) { data, response, error in
            defer { semaphore.signal() }
            if let error = error {
                result = .failure(error)
                return
            }
            do {
                if let http = response as? HTTPURLResponse, http.statusCode < 200 || http.statusCode >= 300 {
                    throw NSError(domain: "XlDlSwift", code: http.statusCode)
                }
                let data = data ?? Data()
                let object = try JSONSerialization.jsonObject(with: data) as? [String: Any]
                let tokenData = object?["data"] as? [String: Any]
                result = .success(LoginTokenResult(
                    code: object?["code"] as? Int ?? -1,
                    token: tokenData?["token"] as? String ?? "",
                    expiresIn: tokenData?["expires_in"] as? Int ?? 0,
                    message: object?["message"] as? String ?? ""
                ))
            } catch {
                result = .failure(error)
            }
        }.resume()
        semaphore.wait()
        return try result.get()
    }

    public func initialize(appId: String, appVersion: String, configPath: String, saveTasks: Bool) -> Int32 {
        var param = xl_dl_init_param()
        return appId.withCString { appIdPointer in
            appVersion.withCString { appVersionPointer in
                configPath.withCString { configPathPointer in
                    param.app_id = appIdPointer
                    param.app_version = appVersionPointer
                    param.cfg_path = configPathPointer
                    param.save_tasks = saveTasks ? 1 : 0
                    return xl_dl_init(&param)
                }
            }
        }
    }

    public func uninit() -> Int32 {
        xl_dl_uninit()
    }

    public func version() -> (result: Int32, version: String) {
        var length: UInt32 = 0
        let first = xl_dl_version(nil, &length)
        guard first == XL_DL_ERROR_SUCCESS.rawValue else {
            return (first, "")
        }
        var buffer = [CChar](repeating: 0, count: Int(length) + 1)
        let result = xl_dl_version(&buffer, &length)
        return (result, result == XL_DL_ERROR_SUCCESS.rawValue ? String(cString: buffer) : "")
    }

    public func login(token: String) -> (result: Int32, sessionId: String) {
        var session = [CChar](repeating: 0, count: Int(XL_DL_MAX_SESSION_ID_LEN))
        let result = token.withCString { tokenPointer in
            xl_dl_login(tokenPointer, &session)
        }
        return (result, result == XL_DL_ERROR_SUCCESS.rawValue ? String(cString: session) : "")
    }

    public func createP2SPTask(url: String, savePath: String, saveName: String) -> (result: Int32, taskId: UInt64) {
        var info = xl_dl_create_p2sp_info()
        var taskId: UInt64 = 0
        let result = savePath.withCString { savePathPointer in
            saveName.withCString { saveNamePointer in
                url.withCString { urlPointer in
                    info.save_path = savePathPointer
                    info.save_name = saveNamePointer
                    info.url = urlPointer
                    return xl_dl_create_p2sp_task(&info, &taskId)
                }
            }
        }
        return (result, taskId)
    }

    public func startTask(taskId: UInt64) -> Int32 {
        xl_dl_start_task(taskId)
    }

    public func stopTask(taskId: UInt64) -> Int32 {
        xl_dl_stop_task(taskId)
    }

    public func deleteTask(taskId: UInt64, deleteFile: Bool) -> Int32 {
        xl_dl_delete_task(taskId, deleteFile ? 1 : 0)
    }

    public func getTaskState(taskId: UInt64) -> (result: Int32, state: TaskState?) {
        var nativeState = xl_dl_task_state()
        let result = xl_dl_get_task_state(taskId, &nativeState)
        guard result == XL_DL_ERROR_SUCCESS.rawValue else {
            return (result, nil)
        }
        return (result, TaskState(
            speed: nativeState.speed,
            totalSize: nativeState.total_size,
            downloadedSize: nativeState.downloaded_size,
            stateCode: nativeState.state_code,
            taskErrorCode: nativeState.task_err_code,
            taskTokenError: nativeState.task_token_err
        ))
    }

    public func setDownloadUrlAcceleration(enable: Bool) -> Int32 {
        return xl_dl_set_download_url_acceleration(enable)
    }
}
