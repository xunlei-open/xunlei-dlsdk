import Foundation
import XlDlSwift

let appId = "eGwtcVo4SDEwMDMwAAAAAy4nxxx=" // TODO: Replace with your own app ID.
let apiKey = "xl_ba3edc87e2734c8bf177a04f3dd4xxx" // TODO: Replace with your own API key.
let appVersion = "1.0"
let taskURL = URL(string: "https://down.sandai.net/thunder11/XunLeiSetup12.0.12.2510.exe")!

let configPath = "/tmp/xl_dl_sdk_conf"
let savePath = "/tmp/ThunderDownload"
try FileManager.default.createDirectory(atPath: configPath, withIntermediateDirectories: true)
try FileManager.default.createDirectory(atPath: savePath, withIntermediateDirectories: true)

let sdk = XLDownloadAPI()
let version = sdk.version()
print("version result:\(version.result) version:\(version.version)")

let initResult = sdk.initialize(appId: appId, appVersion: appVersion, configPath: configPath, saveTasks: true)
print("init result:\(initResult)")
guard initResult == XLDLSuccess || initResult == XLDLAlreadyInit else {
    throw NSError(domain: "XlDlSwiftExample", code: Int(initResult), userInfo: [NSLocalizedDescriptionKey: "init failed: \(initResult)"])
}
defer {
    let uninitResult = sdk.uninit()
    print("uninit result:\(uninitResult)")
}

let token = try sdk.getLoginToken(apiKey: apiKey)
let login = sdk.login(token: token.token)
print("login result:\(login.result) session:\(login.sessionId)")
guard login.result == XLDLSuccess else {
    throw NSError(domain: "XlDlSwiftExample", code: Int(login.result), userInfo: [NSLocalizedDescriptionKey: "login failed: \(login.result)"])
}

let create = sdk.createP2SPTask(url: taskURL.absoluteString, savePath: savePath, saveName: taskURL.lastPathComponent)
print("create task result:\(create.result) taskId:\(create.taskId)")
guard create.result == XLDLSuccess else {
    throw NSError(domain: "XlDlSwiftExample", code: Int(create.result), userInfo: [NSLocalizedDescriptionKey: "create task failed: \(create.result)"])
}

let startResult = sdk.startTask(taskId: create.taskId)
print("start task result:\(startResult)")
guard startResult == XLDLSuccess else {
    throw NSError(domain: "XlDlSwiftExample", code: Int(startResult), userInfo: [NSLocalizedDescriptionKey: "start task failed: \(startResult)"])
}

while true {
    let stateResult = sdk.getTaskState(taskId: create.taskId)
    if let state = stateResult.state {
        print("state:\(state.stateCode) downloaded:\(state.downloadedSize)/\(state.totalSize) speed:\(state.speed)")
        if state.stateCode == XLDownloadTaskStatusSucceeded || state.stateCode == XLDownloadTaskStatusFailed {
            break
        }
    } else {
        print("get task state result:\(stateResult.result)")
        break
    }
    Thread.sleep(forTimeInterval: 1)
}
