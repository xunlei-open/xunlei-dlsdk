# 示例

这是独立 CMake 示例，依赖本地安装后的 C++ SDK。

先安装 SDK 到本地目录：

```bash
cmake -S .. -B ../build
cmake --install ../build --prefix /tmp/xunlei-dlsdk-cpp
```

再构建示例：

```bash
cmake -S . -B build -DCMAKE_PREFIX_PATH=/tmp/xunlei-dlsdk-cpp
cmake --build build
./build/xl_dl_cpp_example
```

示例只使用公开入口：

```cpp
#include <xl_dl/xl_dl_sdk.h>
#include <xl_dl/xl_dl_login_token.hpp>
```

依赖目标：

```cmake
find_package(xunlei-dlsdk-cpp CONFIG REQUIRED)
target_link_libraries(app PRIVATE xunlei::dlsdk)
```
