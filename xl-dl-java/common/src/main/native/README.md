# Java JNI 层（Desktop / Android 共用）

这是 `com.xunlei.open.dlsdk.XLDownloadAPI` 的 native bridge，Desktop 与 Android 共用同一份源码。

Desktop 构建：

```bash
cmake -S . -B build -DXL_DL_NATIVE_DIR=/path/to/current/platform/native
cmake --build build
```

Android 通过 `android/src/main/cpp/CMakeLists.txt` 引用本目录源码，由 NDK 编译为 `libjni_dk.so`。
