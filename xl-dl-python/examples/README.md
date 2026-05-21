# 迅雷下载 Python 示例指南

先安装 `xunlei-open-dlsdk` 和运行环境需要的 native 包。安装说明见 [Python SDK 文档](https://github.com/xunlei-open/xunlei-dlsdk/tree/main/xl-dl-python)。

在 SDK 仓库内调试本示例时，也可以安装本地包后运行：

```bash
python -m pip install -e .
python -m pip install -e native/xl_dl_python_native_macos_universal
python examples/basic_download.py
```

请根据要支持的目标平台安装对应 native 包，跨平台应用可以安装多个。
