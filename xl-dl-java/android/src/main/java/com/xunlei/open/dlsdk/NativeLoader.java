package com.xunlei.open.dlsdk;

final class NativeLoader {
    private static boolean loaded;

    private NativeLoader() {
    }

    static synchronized void load() {
        if (loaded) {
            return;
        }
        try {
            System.loadLibrary("dk");
            loaded = true;
        } catch (UnsatisfiedLinkError error) {
            UnsatisfiedLinkError wrapped = new UnsatisfiedLinkError(
                    "Failed to load Android native library libdk.so from xl-dl-android. "
                            + "Check that the AAR contains jni/arm64-v8a/libdk.so. Cause: "
                            + error.getMessage());
            wrapped.initCause(error);
            throw wrapped;
        }
    }
}
