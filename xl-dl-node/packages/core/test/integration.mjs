import fs from "node:fs";
import { createRequire } from "node:module";
import os from "node:os";
import path from "node:path";
import assert from "node:assert/strict";

const require = createRequire(import.meta.url);
const {
  ERROR_ALREADY_INIT,
  ERROR_SUCCESS,
  TASK_STATUS_FAILED,
  TASK_STATUS_SUCCEEDED,
  XLDownloadAPI
} = require("../dist/index.cjs");

const APP_VERSION = "1.0";
const DEFAULT_TASK_URL = "https://down.sandai.net/thunder11/XunLeiSetup12.0.12.2510.exe";
const DEFAULT_TIMEOUT_SECONDS = 900;

const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));

async function main() {
  const apiKey = process.env.API_KEY;
  assert.ok(apiKey, "API_KEY environment variable is required");
  const appId = process.env.APP_ID;
  assert.ok(appId, "APP_ID environment variable is required");

  const taskUrl = process.env.XL_DL_TEST_URL || DEFAULT_TASK_URL;
  const timeoutSeconds = Number(process.env.XL_DL_TEST_TIMEOUT_SECONDS ?? DEFAULT_TIMEOUT_SECONDS);

  let sdk;
  let taskId = 0n;
  let configPath;
  let savePath;
  try {
    configPath = fs.mkdtempSync(path.join(os.tmpdir(), "xl-dl-node-cfg-"));
    savePath = fs.mkdtempSync(path.join(os.tmpdir(), "xl-dl-node-downloads-"));
    sdk = new XLDownloadAPI();
    const initResult = sdk.init({ appId, appVersion: APP_VERSION, configPath, saveTasks: true });
    assert.ok(initResult === ERROR_SUCCESS || initResult === ERROR_ALREADY_INIT, `init failed: ${initResult}`);

    try {
      const token = await sdk.getLoginToken(apiKey);
      if (token.code !== 0) {
        console.warn(`warning: get loginToken failed, code=${token.code}, message=${token.message}`);
      } else if (!token.token) {
        console.warn("warning: loginToken is empty, continue without login");
      } else {
        try {
          const login = sdk.login(token.token);
          if (login.result !== ERROR_SUCCESS) {
            console.warn(`warning: login failed: ${login.result}, continue without login`);
          } else if (!login.sessionId) {
            console.warn("warning: session id is empty, continue without login");
          }
        } catch (error) {
          console.warn(`warning: login failed: ${error?.message ?? error}, continue without login`);
        }
      }
    } catch (error) {
      console.warn(`warning: get loginToken failed: ${error?.message ?? error}, continue without login`);
    }

    const saveName = path.basename(new URL(taskUrl).pathname) || "download.tmp";
    const create = sdk.createP2spTask({ url: taskUrl, savePath, saveName });
    assert.equal(create.result, ERROR_SUCCESS, `create task failed: ${create.result}`);
    assert.ok(create.taskId > 0n, "task id must be greater than zero");
    taskId = create.taskId;

    const startResult = sdk.startTask(taskId);
    assert.equal(startResult, ERROR_SUCCESS, `start task failed: ${startResult}`);

    const deadline = Date.now() + timeoutSeconds * 1000;
    while (Date.now() < deadline) {
      const state = sdk.getTaskState(taskId);
      assert.equal(state.result, ERROR_SUCCESS, `get task state failed: ${state.result}`);
      if (state.state.stateCode === TASK_STATUS_SUCCEEDED) {
        return;
      }
      assert.notEqual(state.state.stateCode, TASK_STATUS_FAILED, "task failed");
      await sleep(1000);
    }

    throw new Error(`task ${taskId} did not finish within ${timeoutSeconds} seconds`);
  } finally {
    try {
      if (taskId !== 0n && sdk) {
        sdk.deleteTask(taskId, true);
      }
      if (sdk) {
        sdk.uninit();
      }
    } finally {
      if (configPath) {
        fs.rmSync(configPath, { recursive: true, force: true });
      }
      if (savePath) {
        fs.rmSync(savePath, { recursive: true, force: true });
      }
    }
  }
}

main().catch(error => {
  console.error(error);
  process.exitCode = 1;
});
