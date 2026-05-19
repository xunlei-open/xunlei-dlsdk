# Python 示例

先按上级 README 安装 `xunlei-open-dlsdk` 和运行环境需要的 native 包。本仓库源码调试时，也可以安装本地包后运行：

```bash
python -m pip install -e .
python -m pip install -e native/xl_dl_python_native_macos_universal
python examples/basic_download.py
```

请根据要支持的目标平台安装对应 native 包，跨平台应用可以安装多个。
