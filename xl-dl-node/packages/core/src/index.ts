import koffi from "koffi";
import { getLoginToken, type LoginTokenOptions, type LoginTokenResult } from "./login-token";
import { resolveNativeLibraryPath } from "./native-loader";

export { getLoginToken, type LoginTokenOptions, type LoginTokenResult };

export const ERROR_SUCCESS = 0;
export const ERROR_ALREADY_INIT = 9101;
export const TASK_STATUS_SUCCEEDED = 8;
export const TASK_STATUS_FAILED = 9;

export type InitOptions = {
  appId: string;
  appVersion: string;
  configPath: string;
  saveTasks?: boolean;
};

export type CreateP2spTaskOptions = {
  url: string;
  savePath: string;
  saveName: string;
};

export type TaskState = {
  speed: bigint;
  totalSize: bigint;
  downloadedSize: bigint;
  stateCode: number;
  taskErrorCode: number;
  taskTokenError: number;
};

type NativeBinding = {
  xl_dl_init: (param: unknown) => number;
  xl_dl_uninit: () => number;
  xl_dl_login: (loginToken: string, sessionId: Buffer) => number;
  xl_dl_create_p2sp_task: (createInfo: unknown, taskId: bigint[]) => number;
  xl_dl_start_task: (taskId: bigint) => number;
  xl_dl_stop_task: (taskId: bigint) => number;
  xl_dl_delete_task: (taskId: bigint, deleteFile: number) => number;
  xl_dl_get_task_state: (taskId: bigint, state: Record<string, unknown>) => number;
  xl_dl_version: (buffer: Buffer | null, bufferLength: number[]) => number;
  xl_dl_set_external_setting: (domain: string, key: string, value: string) => number;
};

let nativeTypesDeclared = false;

export class XLDownloadAPI {
  private readonly native: NativeBinding;

  constructor(nativeLibraryPath?: string) {
    const library = koffi.load(resolveNativeLibraryPath(nativeLibraryPath));

    declareNativeTypes();

    this.native = {
      xl_dl_init: library.func("int32_t xl_dl_init(const xl_dl_init_param *param)"),
      xl_dl_uninit: library.func("int32_t xl_dl_uninit(void)"),
      xl_dl_login: library.func("int32_t xl_dl_login(const char *login_token, char *session_id)"),
      xl_dl_create_p2sp_task: library.func(
        "int32_t xl_dl_create_p2sp_task(const xl_dl_create_p2sp_info *create_info, _Out_ uint64_t *task_id)"
      ),
      xl_dl_start_task: library.func("int32_t xl_dl_start_task(uint64_t task_id)"),
      xl_dl_stop_task: library.func("int32_t xl_dl_stop_task(uint64_t task_id)"),
      xl_dl_delete_task: library.func("int32_t xl_dl_delete_task(uint64_t task_id, uint8_t delete_file)"),
      xl_dl_get_task_state: library.func("int32_t xl_dl_get_task_state(uint64_t task_id, _Out_ xl_dl_task_state *state)"),
      xl_dl_version: library.func("int32_t xl_dl_version(void *buff, _Inout_ uint32_t *buff_len)"),
      xl_dl_set_external_setting: library.func("int32_t xl_dl_set_external_setting(const char *domain, const char *key, const char *value)")
    };
  }

  static getLoginToken(apiKey: string, options?: LoginTokenOptions): Promise<LoginTokenResult> {
    return getLoginToken(apiKey, options);
  }

  getLoginToken(apiKey: string, options?: LoginTokenOptions): Promise<LoginTokenResult> {
    return getLoginToken(apiKey, options);
  }

  init(options: InitOptions): number {
    return this.native.xl_dl_init({
      app_id: options.appId,
      app_version: options.appVersion,
      cfg_path: options.configPath,
      save_tasks: options.saveTasks === false ? 0 : 1
    });
  }

  uninit(): number {
    return this.native.xl_dl_uninit();
  }

  login(loginToken: string): { result: number; sessionId: string } {
    const sessionId = Buffer.alloc(4096);
    const result = this.native.xl_dl_login(loginToken, sessionId);
    return {
      result,
      sessionId: result === ERROR_SUCCESS ? readNullTerminatedUtf8(sessionId) : ""
    };
  }

  createP2spTask(options: CreateP2spTaskOptions): { result: number; taskId: bigint } {
    const taskId = [0n];
    const result = this.native.xl_dl_create_p2sp_task(
      {
        save_path: options.savePath,
        save_name: options.saveName,
        url: options.url
      },
      taskId
    );
    return { result, taskId: taskId[0] };
  }

  startTask(taskId: bigint): number {
    return this.native.xl_dl_start_task(taskId);
  }

  stopTask(taskId: bigint): number {
    return this.native.xl_dl_stop_task(taskId);
  }

  deleteTask(taskId: bigint, deleteFile = false): number {
    return this.native.xl_dl_delete_task(taskId, deleteFile ? 1 : 0);
  }

  getTaskState(taskId: bigint): { result: number; state: TaskState } {
    const state: Record<string, unknown> = {};
    const result = this.native.xl_dl_get_task_state(taskId, state);
    return {
      result,
      state: {
        speed: BigInt(state.speed as bigint | number | string | undefined ?? 0),
        totalSize: BigInt(state.total_size as bigint | number | string | undefined ?? 0),
        downloadedSize: BigInt(state.downloaded_size as bigint | number | string | undefined ?? 0),
        stateCode: Number(state.state_code ?? 0),
        taskErrorCode: Number(state.task_err_code ?? 0),
        taskTokenError: Number(state.task_token_err ?? 0)
      }
    };
  }

  version(): { result: number; version: string } {
    const length = [0];
    let result = this.native.xl_dl_version(null, length);
    if (result !== ERROR_SUCCESS || length[0] <= 0) {
      return { result, version: "" };
    }

    const buffer = Buffer.alloc(length[0] + 1);
    result = this.native.xl_dl_version(buffer, length);
    return {
      result,
      version: result === ERROR_SUCCESS ? readNullTerminatedUtf8(buffer) : ""
    };
  }

  setDownloadUrlAcceleration(enable: boolean): number {
    return this.native.xl_dl_set_external_setting("task", "query_by_3_cid_switch", enable ? "true" : "false");
  }
}

function declareNativeTypes(): void {
  if (nativeTypesDeclared) {
    return;
  }

  koffi.struct("xl_dl_init_param", {
    app_id: "const char *",
    app_version: "const char *",
    cfg_path: "const char *",
    save_tasks: "uint8_t"
  });
  koffi.struct("xl_dl_create_p2sp_info", {
    save_path: "const char *",
    save_name: "const char *",
    url: "const char *"
  });
  koffi.struct("xl_dl_task_state", {
    speed: "uint64_t",
    total_size: "uint64_t",
    downloaded_size: "uint64_t",
    state_code: "uint8_t",
    task_err_code: "uint32_t",
    task_token_err: "uint32_t"
  });

  nativeTypesDeclared = true;
}

function readNullTerminatedUtf8(buffer: Buffer): string {
  const end = buffer.indexOf(0);
  return buffer.subarray(0, end >= 0 ? end : buffer.length).toString("utf8");
}
