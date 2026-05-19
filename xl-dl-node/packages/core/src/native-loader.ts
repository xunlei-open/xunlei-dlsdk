import path from "node:path";
import { createRequire } from "node:module";
import { existsSync } from "node:fs";

const nodeRequire = createRequire(__filename);

type NativePackageInfo = {
  packageName: string;
  libraryPath: string[];
};

type NativePackage = {
  getLibraryPath?: () => string;
};

const nativePackages: Record<string, Record<string, NativePackageInfo>> = {
  win32: {
    x64: {
      packageName: "@xunlei-open/dlsdk-native-windows-x64",
      libraryPath: ["lib", "dk.dll"]
    },
    ia32: {
      packageName: "@xunlei-open/dlsdk-native-windows-x86",
      libraryPath: ["lib", "dk.dll"]
    }
  },
  darwin: {
    x64: {
      packageName: "@xunlei-open/dlsdk-native-macos-universal",
      libraryPath: ["lib", "libdk.dylib"]
    },
    arm64: {
      packageName: "@xunlei-open/dlsdk-native-macos-universal",
      libraryPath: ["lib", "libdk.dylib"]
    }
  },
  linux: {
    x64: {
      packageName: "@xunlei-open/dlsdk-native-linux-x64",
      libraryPath: ["lib", "libdk.so"]
    }
  }
};

export function resolveNativeLibraryPath(customPath?: string): string {
  if (customPath) {
    return customPath;
  }

  if (process.env.XL_DL_NATIVE_PATH) {
    return process.env.XL_DL_NATIVE_PATH;
  }

  const platformPackages = nativePackages[process.platform];
  const info = platformPackages?.[process.arch];
  if (!info) {
    throw new Error(`当前平台暂不支持：${process.platform}/${process.arch}`);
  }

  try {
    const nativePackage = nodeRequire(info.packageName) as NativePackage;
    if (typeof nativePackage.getLibraryPath === "function") {
      return nativePackage.getLibraryPath();
    }

    const packageJsonPath = nodeRequire.resolve(`${info.packageName}/package.json`);
    return path.join(path.dirname(packageJsonPath), ...info.libraryPath);
  } catch (error) {
    const workspaceLibraryPath = resolveWorkspaceLibraryPath(info);
    if (workspaceLibraryPath) {
      return workspaceLibraryPath;
    }

    throw new Error(
      `未找到当前平台动态库包 ${info.packageName}。请安装 @xunlei-open/dlsdk 和 ${info.packageName}。`
    );
  }
}

function resolveWorkspaceLibraryPath(info: NativePackageInfo): string | undefined {
  const workspacePackageDir = info.packageName.replace(/^@xunlei-open\/dlsdk-/, "");
  const libraryPath = path.resolve(__dirname, "..", "..", workspacePackageDir, ...info.libraryPath);
  return existsSync(libraryPath) ? libraryPath : undefined;
}
