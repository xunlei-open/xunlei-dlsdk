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
            System.loadLibrary("jni_dk");
            loaded = true;
        } catch (UnsatisfiedLinkError error) {
            UnsatisfiedLinkError wrapped = new UnsatisfiedLinkError(
                    "Failed to load Android native libraries libdk.so / libjni_dk.so from xl-dl-android. "
                            + "Check that the AAR contains jni/arm64-v8a/libdk.so and libjni_dk.so. Cause: "
                            + error.getMessage());
            wrapped.initCause(error);
            throw wrapped;
        }
    }
}
