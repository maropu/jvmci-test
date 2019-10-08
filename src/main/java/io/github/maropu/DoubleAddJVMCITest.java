package io.github.maropu;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import org.codehaus.janino.ClassBodyEvaluator;

import io.github.maropu.jvmci.AsmInjector;
import io.github.maropu.nvlib.TestRuntimeNative;
import io.github.maropu.nvlib.TestRuntimeNativeLoader;

public class DoubleAddJVMCITest {

  private static final TestRuntimeNative testApi = TestRuntimeNativeLoader.loadApi();

  // Address of the function defined in `libtest.dylib`
  private static final long nativeFuncAddr =
    testApi.getDoubleFuncAddr(TestRuntimeNative.FUNCTION_ID_ADD);
  private static Method generatedMethod;
  private static Object generatedObj;

  // Address of the generated function by `pyAdd-float64.bc`
  private static long pyNativeFuncAddr = 0L;

  static void debugPrint(String msg) {
    System.err.println(msg);
  }

  static void initializeNativeAdd() throws Exception {
    Method m = DoubleAddJVMCITest.class.getMethod("nativeAdd", double.class, double.class);
    new AsmInjector().injectFuncAddr(nativeFuncAddr, m);
  }

  static void initializeNativeAddInGeneratedMethod() throws Exception {
    // Generates a base class to call native code
    ClassBodyEvaluator ev = new ClassBodyEvaluator();
    String code = "public static double plus(double a, double b) { " +
      "throw new java.lang.UnsupportedOperationException(\"from generated Java code\"); }";
    ev.cook("generated.java", code);
    Class clazz = ev.getClazz();
    generatedMethod = clazz.getMethod("plus", double.class, double.class);
    generatedObj = clazz.newInstance();

    // Injects native code to a Java method in the generated class
    new AsmInjector().injectFuncAddr(nativeFuncAddr, generatedMethod);
  }

  private static byte[] byteArrayFromResource(String path) throws IOException {
    InputStream inputStream =
      Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    return inputStream.readAllBytes();
  }

  static void initializePyNativeAdd() throws Exception {
    // Injects a generated native function from LLVM bitcode
    String bitcodeFile = "pyAdd-float64.bc";
    String funcNameInBitcode = "cfunc._ZN8__main__9pyAdd$242Edd";
    byte[] bitcode = byteArrayFromResource(bitcodeFile);
    debugPrint(
      String.format(
        "'%s' compiled into code below:\n" +
        "========== LLVM Assembly Code =========\n" +
        "%s\n" +
        "========== Machine Code ===============\n" +
        "%s",
        bitcodeFile,
        testApi.toLLVMAssemblyCode(bitcode),
        testApi.toX86_64AssemblyCode(bitcode)
      ));
    long compileState = testApi.compileToFunc(bitcode, funcNameInBitcode, true);
    pyNativeFuncAddr = testApi.getFuncAddrFromCompileState(compileState);
    Method m = DoubleAddJVMCITest.class.getMethod("pyNativeAdd", double.class, double.class);
    new AsmInjector().injectFuncAddr(pyNativeFuncAddr, m);
    // testApi.releaseCompileState(compileState);
  }

  static {
    try {
      testApi.initialize();
      initializeNativeAdd();
      initializeNativeAddInGeneratedMethod();
      initializePyNativeAdd();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  public static double nativeAdd(double a, double b) {
    throw new UnsupportedOperationException("from Java code");
  }

  public static double nativeAddInGeneratedMethod(double a, double b) throws Exception {
    return (double) generatedMethod.invoke(generatedObj, a, b);
  }

  public static double pyNativeAdd(double a, double b) {
    throw new UnsupportedOperationException("from Java code");
  }

  public static double jvmAdd(double a, double b) {
    return a + b;
  }

  public static double jniAdd(double a, double b) {
    return testApi.callDoubleFuncFromAddr(nativeFuncAddr, a, b);
  }
}
