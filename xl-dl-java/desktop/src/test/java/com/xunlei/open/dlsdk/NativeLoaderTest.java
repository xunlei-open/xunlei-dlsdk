package com.xunlei.open.dlsdk;

public final class NativeLoaderTest {
    private NativeLoaderTest() {
    }

    public static void main(String[] args) {
        assertPlatform("Windows 11", "amd64", "windows-x64", "dk.dll", "jni_dk.dll");
        assertPlatform("Windows 10", "x86", "windows-x86", "dk.dll", "jni_dk.dll");
        assertPlatform("Mac OS X", "aarch64", "macos-universal", "libdk.dylib", "libjni_dk.dylib");
        assertPlatform("Linux", "x86_64", "linux-x64", "libdk.so", "libjni_dk.so");
    }

    private static void assertPlatform(
            String osName,
            String osArch,
            String expectedId,
            String expectedCoreLibrary,
            String expectedJniLibrary) {
        NativeLoader.Platform platform = NativeLoader.detectPlatformForTest(osName, osArch);
        if (!expectedId.equals(platform.id)) {
            throw new AssertionError("Expected platform " + expectedId + " but got " + platform.id);
        }
        if (!expectedCoreLibrary.equals(platform.coreLibraryName)) {
            throw new AssertionError("Expected core library " + expectedCoreLibrary
                    + " but got " + platform.coreLibraryName);
        }
        if (!expectedJniLibrary.equals(platform.jniLibraryName)) {
            throw new AssertionError("Expected JNI library " + expectedJniLibrary
                    + " but got " + platform.jniLibraryName);
        }
    }
}
