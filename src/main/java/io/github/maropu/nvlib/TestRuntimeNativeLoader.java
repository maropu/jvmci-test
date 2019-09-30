package io.github.maropu.nvlib;

import java.io.File;

public class TestRuntimeNativeLoader {

  private static final NativeLibLoaderImpl testNative = new NativeLibLoaderImpl("test-rt");
  private static TestRuntimeNative testApi = null;

  public static synchronized boolean isLoaded() {
    return testApi != null;
  }

  public static synchronized TestRuntimeNative loadApi() throws RuntimeException {
    if (testApi != null) {
      return testApi;
    }
    File nativeLib = testNative.loadNativeLibFromJar();
    assert(nativeLib.exists());
    System.load(nativeLib.getAbsolutePath());
    testApi = new TestRuntimeNative();
    return testApi;
  }
}
