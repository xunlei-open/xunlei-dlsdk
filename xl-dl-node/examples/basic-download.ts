import fs from "node:fs";
import path from "node:path";
import { ERROR_ALREADY_INIT, ERROR_SUCCESS, TASK_STATUS_FAILED, TASK_STATUS_SUCCEEDED, XLDownloadAPI } from "@xunlei-open/dlsdk";

const appId = "eGwtcVo4SDEwMDMwAAAAAy4nxxx="; // TODO: Replace with your own app ID.
const apiKey = "xl_ba3edc87e2734c8bf177a04f3dd4xxx"; // TODO: Replace with your own API key.
const appVersion = "1.0";
const taskUrl = "https://down.sandai.net/thunder11/XunLeiWebSetup25.0.90.1592xl11.exe";

async function main() {
  const configPath = "/tmp/xl_dl_sdk_conf";
  const savePath = "/tmp/ThunderDownload";
  fs.mkdirSync(configPath, { recursive: true });
  fs.mkdirSync(savePath, { recursive: true });

  const sdk = new XLDownloadAPI();
  const version = sdk.version();
  console.log("version", version);

  let initialized = false;
  try {
    const initResult = sdk.init({
      appId,
      appVersion,
      configPath,
      saveTasks: true
    });
    console.log("init", initResult);
    if (initResult !== ERROR_SUCCESS && initResult !== ERROR_ALREADY_INIT) {
      return;
    }
    initialized = true;

    const token = await sdk.getLoginToken(apiKey);
    if (token.code !== 0 || !token.token) {
      throw new Error(`获取 loginToken 失败：${token.message}`);
    }

    const login = sdk.login(token.token);
    console.log("login", login.result, login.sessionId);
    if (login.result !== ERROR_SUCCESS) {
      return;
    }

    const saveName = path.basename(new URL(taskUrl).pathname);
    const create = sdk.createP2spTask({
      url: taskUrl,
      savePath,
      saveName
    });
    console.log("create task", create);
    if (create.result !== ERROR_SUCCESS) {
      return;
    }

    const startResult = sdk.startTask(create.taskId);
    console.log("start", startResult);
    if (startResult !== ERROR_SUCCESS) {
      return;
    }

    while (true) {
      const state = sdk.getTaskState(create.taskId);
      console.log("state", state);
      if (
        state.result !== ERROR_SUCCESS ||
        state.state.stateCode === TASK_STATUS_SUCCEEDED ||
        state.state.stateCode === TASK_STATUS_FAILED
      ) {
        break;
      }
      await new Promise(resolve => setTimeout(resolve, 1000));
    }
  } finally {
    if (initialized) {
      console.log("uninit", sdk.uninit());
    }
  }
}

main().catch(error => {
  console.error(error);
  process.exitCode = 1;
});
