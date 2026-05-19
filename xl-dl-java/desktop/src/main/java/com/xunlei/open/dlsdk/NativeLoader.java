package com.xunlei.open.dlsdk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

final class NativeLoader {
    private static final String NATIVE_DIR_PROPERTY = "com.xunlei.open.dlsdk.native.dir";
    private static final String RESOURCE_ROOT = "META-INF/xunlei/open/dlsdk/native";

    private static boolean loaded;

    private NativeLoader() {
    }

    static synchronized void load() {
        if (loaded) {
            return;
        }

        Platform platform = detectPlatform();
        String overrideDir = System.getProperty(NATIVE_DIR_PROPERTY);
        if (overrideDir != null && !overrideDir.trim().isEmpty()) {
            loadFromDirectory(new File(overrideDir), platform);
        } else {
            loadFromClasspath(platform);
        }
        loaded = true;
    }

    static String expectedNativePackageName() {
        Platform platform = detectPlatform();
        return "com.xunlei.open:xl-dl-native-" + platform.id;
    }

    static Platform detectPlatformForTest(String osName, String osArch) {
        return detectPlatform(osName, osArch);
    }

    private static void loadFromDirectory(File directory, Platform platform) {
        File coreLibrary = new File(directory, platform.coreLibraryName);
        if (!coreLibrary.isFile()) {
            throw new UnsatisfiedLinkError(
                    "Native directory " + directory.getAbsolutePath()
                            + " must contain " + platform.coreLibraryName + " for " + platform.id + ".");
        }
        System.load(coreLibrary.getAbsolutePath());
        loadJniBridge(new File(directory, platform.jniLibraryName), platform);
    }

    private static void loadFromClasspath(Platform platform) {
        File extractDir;
        try {
            extractDir = createTempDirectory(platform);
        } catch (IOException e) {
            UnsatisfiedLinkError error = new UnsatisfiedLinkError(
                    "Failed to create temporary directory for XL Download native libraries: " + e.getMessage());
            error.initCause(e);
            throw error;
        }
        extractDir.deleteOnExit();

        File coreLibrary = extractLibrary(platform, platform.coreLibraryName, extractDir);
        System.load(coreLibrary.getAbsolutePath());
        File jniLibrary = extractLibrary(platform, platform.jniLibraryName, extractDir);
        loadJniBridge(jniLibrary, platform);
    }

    private static File createTempDirectory(Platform platform) throws IOException {
        File marker = File.createTempFile("xunlei-dlsdk-" + platform.id + "-", ".tmp");
        if (!marker.delete()) {
            throw new IOException("failed to delete temporary marker " + marker.getAbsolutePath());
        }
        if (!marker.mkdirs()) {
            throw new IOException("failed to create temporary directory " + marker.getAbsolutePath());
        }
        return marker;
    }

    private static File extractLibrary(Platform platform, String libraryName, File extractDir) {
        String resourceName = RESOURCE_ROOT + "/" + platform.id + "/" + libraryName;
        ClassLoader classLoader = NativeLoader.class.getClassLoader();
        URL resource = classLoader == null
                ? ClassLoader.getSystemResource(resourceName)
                : classLoader.getResource(resourceName);
        if (resource == null) {
            throw new UnsatisfiedLinkError(
                    "Missing native library resource " + resourceName + ". Add the current platform native package "
                            + expectedNativePackageName() + " or set -D" + NATIVE_DIR_PROPERTY
                            + "=/path/to/native/libs.");
        }

        File target = new File(extractDir, libraryName);
        InputStream input = null;
        FileOutputStream output = null;
        try {
            input = resource.openStream();
            output = new FileOutputStream(target);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = input.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
        } catch (IOException e) {
            UnsatisfiedLinkError error = new UnsatisfiedLinkError(
                    "Failed to extract native library " + resourceName + ": " + e.getMessage());
            error.initCause(e);
            throw error;
        } finally {
            closeQuietly(input);
            closeQuietly(output);
        }
        target.deleteOnExit();
        return target;
    }

    private static void loadJniBridge(File jniLibrary, Platform platform) {
        if (!jniLibrary.isFile()) {
            throw new UnsatisfiedLinkError(
                    "Native package " + expectedNativePackageName() + " must contain "
                            + platform.jniLibraryName + " for " + platform.id + ".");
        }
        System.load(jniLibrary.getAbsolutePath());
    }

    private static Platform detectPlatform() {
        return detectPlatform(System.getProperty("os.name"), System.getProperty("os.arch"));
    }

    private static Platform detectPlatform(String osName, String osArch) {
        String os = normalize(osName);

        String osId;
        if (os.contains("win")) {
            osId = "windows";
        } else if (os.contains("mac") || os.contains("darwin")) {
            return new Platform("macos-universal", coreLibraryName("macos"), jniLibraryName("macos"));
        } else if (os.contains("linux")) {
            osId = "linux";
        } else {
            throw new UnsatisfiedLinkError("Unsupported operating system for XL Download SDK: " + osName);
        }

        String arch = normalize(osArch);
        String archId;
        if (arch.equals("x86_64") || arch.equals("amd64")) {
            archId = "x64";
        } else if (osId.equals("windows")
                && (arch.equals("x86") || arch.equals("i386") || arch.equals("i686"))) {
            archId = "x86";
        } else if (arch.equals("aarch64") || arch.equals("arm64")) {
            archId = "arm64";
        } else {
            throw new UnsatisfiedLinkError("Unsupported CPU architecture for XL Download SDK: " + osArch);
        }

        return new Platform(osId + "-" + archId, coreLibraryName(osId), jniLibraryName(osId));
    }

    private static String coreLibraryName(String osId) {
        if ("windows".equals(osId)) {
            return "dk.dll";
        }
        if ("macos".equals(osId)) {
            return "libdk.dylib";
        }
        return "libdk.so";
    }

    private static String jniLibraryName(String osId) {
        if ("windows".equals(osId)) {
            return "jni_dk.dll";
        }
        if ("macos".equals(osId)) {
            return "libjni_dk.dylib";
        }
        return "libjni_dk.so";
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replace('-', '_');
    }

    private static void closeQuietly(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static void closeQuietly(FileOutputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignored) {
            }
        }
    }

    static final class Platform {
        final String id;
        final String coreLibraryName;
        final String jniLibraryName;

        Platform(String id, String coreLibraryName, String jniLibraryName) {
            this.id = id;
            this.coreLibraryName = coreLibraryName;
            this.jniLibraryName = jniLibraryName;
        }
    }
}
