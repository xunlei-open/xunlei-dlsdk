# 迅雷下载 Python SDK 指南

XL Download Python SDK 适用于在 Python 脚本或桌面工具中接入迅雷下载能力。

## 支持平台

| Windows x64 | Windows x86 | macOS Universal | Linux x64 |
| --- | --- | --- | --- |
| ✅ | ✅ | ✅ | ✅ |

## 环境要求

- Python 3.8 或更高版本。

## 包说明

- `xunlei-open-dlsdk`：主 API 包。
- `xunlei-open-dlsdk-native-windows-x64`
- `xunlei-open-dlsdk-native-windows-x86`
- `xunlei-open-dlsdk-native-macos-universal`
- `xunlei-open-dlsdk-native-linux-x64`

## pip 安装

在应用虚拟环境中安装主包，并按应用要支持的平台安装对应 native 包。跨平台应用可以同时安装多个 native 包，运行时会加载当前平台对应的动态库。

| 目标平台 | 安装命令 |
| --- | --- |
| Windows x64 | `python -m pip install xunlei-open-dlsdk xunlei-open-dlsdk-native-windows-x64` |
| Windows x86 | `python -m pip install xunlei-open-dlsdk xunlei-open-dlsdk-native-windows-x86` |
| macOS Universal | `python -m pip install xunlei-open-dlsdk xunlei-open-dlsdk-native-macos-universal` |
| Linux x64 | `python -m pip install xunlei-open-dlsdk xunlei-open-dlsdk-native-linux-x64` |

例如接入 macOS：

```bash
python -m pip install xunlei-open-dlsdk xunlei-open-dlsdk-native-macos-universal
```

Python import 名称保持为 `xl_dl`：

```python
from xl_dl import XLDownloadAPI
```

也可以不安装 native wheel，直接指定本地原生库路径：

```bash
export XL_DL_NATIVE_PATH=/absolute/path/to/libdk.dylib
```

| 平台 | 文件名 |
| --- | --- |
| Windows x64 / x86 | `dk.dll` |
| macOS Universal | `libdk.dylib` |
| Linux x64 | `libdk.so` |

`XL_DL_NATIVE_PATH` 必须是绝对路径。适用于无法安装 native wheel 的场景（如 CI 环境、自定义存放路径）。

## 最小示例

```python
import time
from pathlib import Path

from xl_dl import ERROR_ALREADY_INIT, ERROR_SUCCESS, TASK_STATUS_SUCCEEDED, TASK_STATUS_FAILED, XLDownloadAPI

app_id = "your-app-id"
api_key = "your-api-key"
config_path = "/tmp/xl_dl_sdk_conf"
save_path = "/tmp/ThunderDownload"
Path(config_path).mkdir(parents=True, exist_ok=True)
Path(save_path).mkdir(parents=True, exist_ok=True)

sdk = XLDownloadAPI()

init_result = sdk.init(app_id, "1.0", config_path, save_tasks=True)
if init_result not in (ERROR_SUCCESS, ERROR_ALREADY_INIT):
    raise RuntimeError(f"init failed: {init_result}")

try:
    code, login_token, _expires_in, message = sdk.get_login_token(api_key)
    if code != 0:
        raise RuntimeError(f"get loginToken failed: {message}")

    login_result, session_id = sdk.login(login_token)
    if login_result != ERROR_SUCCESS:
        raise RuntimeError(f"login failed: {login_result}")

    create_result, task_id = sdk.create_p2sp_task(
        "https://example.com/file.zip",
        save_path,
        "file.zip",
    )
    if create_result != ERROR_SUCCESS:
        raise RuntimeError(f"create task failed: {create_result}")

    start_result = sdk.start_task(task_id)
    if start_result != ERROR_SUCCESS:
        raise RuntimeError(f"start task failed: {start_result}")

    while True:
        result, state = sdk.get_task_state(task_id)
        if state is None:
            break
        print(f"\rstate:{state.state_code} downloaded:{state.downloaded_size}/{state.total_size} speed:{state.speed}", end="", flush=True)
        if state.state_code in (TASK_STATUS_SUCCEEDED, TASK_STATUS_FAILED):
            print()
            break
        time.sleep(1)

finally:
    sdk.uninit()
```

完整可运行示例见 [示例代码](https://github.com/xunlei-open/xunlei-dlsdk/blob/main/xl-dl-python/examples/basic_download.py)。

## 相关文档

- [Github](https://github.com/xunlei-open/xunlei-dlsdk/tree/main/xl-dl-python)
- [接入流程与凭证申请](https://open.xunlei.com/doc?doc=access_flow)
- [API 参考文档](https://open.xunlei.com/doc?doc=xl_dl_init)
- [错误码说明](https://open.xunlei.com/doc?doc=error_code)
