import importlib
import os
import platform
from pathlib import Path


def load_library():
    override = os.environ.get("XL_DL_NATIVE_PATH")
    if override:
        import ctypes

        return ctypes.cdll.LoadLibrary(override)

    module_name, library_name = _native_package()
    try:
        module = importlib.import_module(module_name)
    except ImportError as exc:
        raise RuntimeError(
            f"未安装当前平台 native 包：{_native_distribution_name(module_name)}"
        ) from exc

    library_path = Path(module.__file__).resolve().parent / library_name
    if not library_path.exists():
        raise RuntimeError(f"native 包缺少动态库：{library_path}")

    import ctypes

    return ctypes.cdll.LoadLibrary(str(library_path))


def _native_package():
    system = platform.system()
    machine = platform.machine().lower()

    if system == "Windows":
        if machine in ("amd64", "x86_64"):
            return "xl_dl_python_native_windows_x64", "dk.dll"
        if machine in ("x86", "i386", "i686"):
            return "xl_dl_python_native_windows_x86", "dk.dll"
    if system == "Linux" and machine in ("x86_64", "amd64"):
        return "xl_dl_python_native_linux_x64", "libdk.so"
    if system == "Darwin":
        return "xl_dl_python_native_macos_universal", "libdk.dylib"

    raise RuntimeError(f"不支持的 Python SDK 运行平台：{system}/{machine}")


def _native_distribution_name(module_name):
    prefix = "xl_dl_python_native_"
    suffix = module_name[len(prefix) :] if module_name.startswith(prefix) else module_name
    suffix = suffix.replace("_", "-")
    return f"xunlei-open-dlsdk-native-{suffix}"
