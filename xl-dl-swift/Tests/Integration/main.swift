import Foundation
import XlDlSwift

let appVersion = "1.0"
let defaultTaskURL = "https://down.sandai.net/thunder11/XunLeiWebSetup25.0.90.1592xl11.exe"
let errorSuccess: Int32 = 0
let errorAlreadyInit: Int32 = 9101

func require(_ condition: @autoclosure () -> Bool, _ message: String) throws {
    if !condition() {
        throw NSError(domain: "XlDlSwiftIntegrationTest", code: 1, userInfo: [NSLocalizedDescriptionKey: message])
    }
}

func env(_ name: String, default defaultValue: String = "") -> String {
    let value = ProcessInfo.processInfo.environment[name] ?? ""
    return value.isEmpty ? defaultValue : value
}

func downloadTest(sdk: XLDownloadAPI, taskURLString: String, saveURL: URL, timeoutSeconds: TimeInterval) throws {
    let taskURL = URL(string: taskURLString)!
    let create = sdk.createP2SPTask(
        url: taskURL.absoluteString,
        savePath: saveURL.path,
        saveName: taskURL.lastPathComponent.isEmpty ? "download.tmp" : taskURL.lastPathComponent
    )
    try require(create.result == errorSuccess, "create task failed: \(create.result)")
    try require(create.taskId > 0, "task id must be greater than zero")
    let taskId = create.taskId

    let startResult = sdk.startTask(taskId: taskId)
    try require(startResult == errorSuccess, "start task failed: \(startResult)")

    let deadline = Date().addingTimeInterval(timeoutSeconds)
    while Date() < deadline {
        let stateResult = sdk.getTaskState(taskId: taskId)
        try require(stateResult.result == errorSuccess, "get task state failed: \(stateResult.result)")
        guard let state = stateResult.state else {
            throw NSError(domain: "XlDlSwiftIntegrationTest", code: 1, userInfo: [NSLocalizedDescriptionKey: "task state is empty"])
        }
        if state.stateCode == XLDownloadTaskStatusSucceeded {
            return
        }
        try require(state.stateCode != XLDownloadTaskStatusFailed, "task failed")
        Thread.sleep(forTimeInterval: 1)
    }

    throw NSError(domain: "XlDlSwiftIntegrationTest", code: 1, userInfo: [NSLocalizedDescriptionKey: "task \(taskId) did not finish within \(Int(timeoutSeconds)) seconds"])
}

func main() throws {
    let apiKey = env("API_KEY")
    try require(!apiKey.isEmpty, "API_KEY environment variable is required")
    let appId = env("APP_ID")
    try require(!appId.isEmpty, "APP_ID environment variable is required")

    let taskURLString = env("XL_DL_TEST_URL", default: defaultTaskURL)
    let timeoutSeconds = TimeInterval(Int(env("XL_DL_TEST_TIMEOUT_SECONDS", default: "900")) ?? 900)
    let tempURL = FileManager.default.temporaryDirectory
    let configURL = tempURL.appendingPathComponent("xl-dl-swift-cfg-\(UUID().uuidString)")
    let saveURL = tempURL.appendingPathComponent("xl-dl-swift-downloads-\(UUID().uuidString)")
    try FileManager.default.createDirectory(at: configURL, withIntermediateDirectories: true)
    defer {
        try? FileManager.default.removeItem(at: configURL)
        try? FileManager.default.removeItem(at: saveURL)
    }
    try FileManager.default.createDirectory(at: saveURL, withIntermediateDirectories: true)

    let sdk = XLDownloadAPI()
    var taskId: UInt64 = 0
    defer {
        if taskId != 0 {
            _ = sdk.deleteTask(taskId: taskId, deleteFile: true)
        }
        _ = sdk.uninit()
    }

    let initResult = sdk.initialize(appId: appId, appVersion: appVersion, configPath: configURL.path, saveTasks: true)
    try require(initResult == errorSuccess || initResult == errorAlreadyInit, "init failed: \(initResult)")

    do {
        let token = try sdk.getLoginToken(apiKey: apiKey)
        if token.code != 0 {
            fputs("warning: get loginToken failed, code=\(token.code), message=\(token.message)\n", stderr)
        } else if token.token.isEmpty {
            fputs("warning: loginToken is empty, continue without login\n", stderr)
        } else {
            let login = sdk.login(token: token.token)
            if login.result != errorSuccess {
                fputs("warning: login failed: \(login.result), continue without login\n", stderr)
            } else if login.sessionId.isEmpty {
                fputs("warning: session id is empty, continue without login\n", stderr)
            }
        }
    } catch {
        fputs("warning: get loginToken failed: \(error), continue without login\n", stderr)
    }

    try downloadTest(sdk: sdk, taskURLString: taskURLString, saveURL: saveURL, timeoutSeconds: timeoutSeconds)

    let setAccelerationResult = sdk.setDynamicLinkAcceleration(enable: false)
    try require(setAccelerationResult == errorSuccess, "set dynamic link acceleration failed: \(setAccelerationResult)")

    try downloadTest(sdk: sdk, taskURLString: taskURLString, saveURL: saveURL, timeoutSeconds: timeoutSeconds)
}

do {
    try main()
} catch {
    fputs("\(error)\n", stderr)
    exit(1)
}
