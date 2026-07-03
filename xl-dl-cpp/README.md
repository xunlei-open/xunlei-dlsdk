# 迅雷下载 C++ SDK 指南

XL Download C++ SDK 适用于在 Windows、macOS 和 Linux 桌面应用中接入迅雷下载能力。

## 支持平台

| Windows x64 | Windows x86 | macOS Universal | Linux x64 |
| --- | --- | --- | --- |
| ✅ | ✅ | ✅ | ✅ |

## 环境要求

- CMake 3.15 或更高版本。
- 支持 C++11 的编译器。

## 安装

下载 [xunlei-dlsdk-cpp-1.0.2.zip](https://github.com/xunlei-open/xunlei-dlsdk/releases/latest/download/xunlei-dlsdk-cpp-1.0.2.zip)，解压后安装到本地目录：

```bash
unzip xunlei-dlsdk-cpp-1.0.2.zip
cmake -S xunlei-dlsdk-cpp -B build-sdk \
  -DCMAKE_BUILD_TYPE=Release \
  -DCMAKE_INSTALL_PREFIX=/tmp/xunlei-dlsdk-cpp
cmake --install build-sdk
```

压缩包包含头文件、CMake package、示例工程和 `prebuilt/` 平台运行时库。

安装指定平台运行时库时，可以传入 `XL_DL_CPP_NATIVE_PLATFORM`：

| 值 | 安装的运行时库 |
| --- | --- |
| `windows-x64` | Windows x64：`bin/dk.dll`、`lib/dk.lib` |
| `windows-x86` | Windows x86：`bin/dk.dll`、`lib/dk.lib` |
| `macos-universal` | macOS Universal：`lib/libdk.dylib` |
| `linux-x64` | Linux x64：`lib/libdk.so` |

进入解压后的 SDK 目录，指定平台安装：

```bash
cd xunlei-dlsdk-cpp
cmake -S . -B build-win-x86 -DXL_DL_CPP_NATIVE_PLATFORM=windows-x86 -DCMAKE_INSTALL_PREFIX=/tmp/xunlei-dlsdk-cpp-win-x86
cmake --install build-win-x86
```

## 在项目中引用

```cmake
find_package(xunlei-dlsdk-cpp CONFIG REQUIRED)

add_executable(app main.cpp)
target_link_libraries(app PRIVATE xunlei::dlsdk)
```

配置你的应用项目：

```bash
cmake -S . -B build -DCMAKE_PREFIX_PATH=/tmp/xunlei-dlsdk-cpp
cmake --build build
```

运行时需要确保动态库可被系统找到：

- Windows：把安装目录下的 `bin/` 加入 `PATH`。
- macOS：把安装目录下的 `lib/` 加入 `DYLD_LIBRARY_PATH`，或把 dylib 一起打包进应用。
- Linux：把安装目录下的 `lib/` 加入 `LD_LIBRARY_PATH`，或在应用中配置 rpath。

## 最小示例

```cpp
#include <xl_dl/xl_dl_login_token.hpp>
#include <xl_dl/xl_dl_sdk.h>

#include <chrono>
#include <cstdio>
#include <cstring>
#include <string>
#include <thread>

int main() {
    xl_dl_init_param init_param;
    std::memset(&init_param, 0, sizeof(init_param));
    init_param.app_id = "your-app-id";
    init_param.app_version = "1.0";
    init_param.cfg_path = "/tmp/xl_dl_sdk_conf";
    init_param.save_tasks = 1;

    int code = xl_dl_init(&init_param);
    if (code != XL_DL_ERROR_SUCCESS && code != XL_DL_ERROR_ALREADY_INIT) {
        return code;
    }

    xl_dl::login_token_result token = xl_dl::get_login_token("your-api-key");
    if (token.code != 0 || token.token.empty()) {
        xl_dl_uninit();
        return token.code;
    }

    char session_id[XL_DL_MAX_SESSION_ID_LEN] = {0};
    code = xl_dl_login(token.token.c_str(), session_id);
    if (code != XL_DL_ERROR_SUCCESS) {
        xl_dl_uninit();
        return code;
    }

    xl_dl_create_p2sp_info task;
    std::memset(&task, 0, sizeof(task));
    task.url = "https://example.com/file.zip";
    task.save_path = "/tmp/ThunderDownload";
    task.save_name = "file.zip";

    uint64_t task_id = 0;
    code = xl_dl_create_p2sp_task(&task, &task_id);
    if (code != XL_DL_ERROR_SUCCESS) {
        xl_dl_uninit();
        return code;
    }

    code = xl_dl_start_task(task_id);
    if (code != XL_DL_ERROR_SUCCESS) {
        xl_dl_uninit();
        return code;
    }

    while (true) {
        xl_dl_task_state state;
        std::memset(&state, 0, sizeof(state));
        code = xl_dl_get_task_state(task_id, &state);
        std::printf("\rstate:%u downloaded:%llu/%llu speed:%llu",
                state.state_code,
                static_cast<unsigned long long>(state.downloaded_size),
                static_cast<unsigned long long>(state.total_size),
                static_cast<unsigned long long>(state.speed));
        std::fflush(stdout);
        if (code != XL_DL_ERROR_SUCCESS
                || state.state_code == XL_DL_TASK_STATUS_SUCCEEDED
                || state.state_code == XL_DL_TASK_STATUS_FAILED) {
            std::printf("\n");
            break;
        }
        std::this_thread::sleep_for(std::chrono::seconds(1));
    }

    xl_dl_uninit();
    return 0;
}
```

完整可运行示例见 [示例代码](https://github.com/xunlei-open/xunlei-dlsdk/tree/main/xl-dl-cpp/examples)。

## 相关文档

- [Github](https://github.com/xunlei-open/xunlei-dlsdk/tree/main/xl-dl-cpp)
- [接入流程与凭证申请](https://open.xunlei.com/doc?doc=access_flow)
- [API 参考文档](https://open.xunlei.com/doc?doc=xl_dl_init)
- [错误码说明](https://open.xunlei.com/doc?doc=error_code)
