package io.github.maropu;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import org.codehaus.janino.ClassBodyEvaluator;

import io.github.maropu.jvmci.AsmInjector;
import io.github.maropu.nvlib.TestRuntimeNative;
import io.github.maropu.nvlib.TestRuntimeNativeLoader;

public class CodegenTest {

  private static final TestRuntimeNative testApi = TestRuntimeNativeLoader.loadApi();

  // Plus method based on `JVMCIBaseClass`
  private static JVMCIBaseClass genPlusObj;

  // Multiply method based on `JVMCIBaseClass`
  private static JVMCIBaseClass genMultiplyObj;

  static Class newJVMCIBaseClass() throws Exception {
    ClassBodyEvaluator ev = new ClassBodyEvaluator();
    ev.setClassName("io.github.maropu.GeneratedClass");
    ev.setParentClassLoader(Thread.currentThread().getContextClassLoader());
    ev.setExtendedClass(JVMCIBaseClass.class);
    String code = "public int binaryIntOp(int a, int b) { " +
      "throw new java.lang.UnsupportedOperationException(\"from generated Java code\"); }";
    ev.cook("generated.java", code);
    return ev.getClazz();
  }

  private static byte[] byteArrayFromResource(String path) throws IOException {
    InputStream inputStream =
      Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    return inputStream.readAllBytes();
  }

  static void initializeNativePlus() throws Exception {
    Class clazz = newJVMCIBaseClass();
    Method m = clazz.getMethod("binaryIntOp", int.class, int.class);
    genPlusObj = (JVMCIBaseClass) clazz.getConstructor().newInstance();

    // Injects a generated native function from LLVM bitcode
    String bitcodeFile = "pyAdd-int32.bc";
    String funcNameInBitcode = "cfunc._ZN8__main__9pyAdd$241Eii";
    byte[] bitcode = byteArrayFromResource(bitcodeFile);
    long compileState = testApi.compileToFunc(bitcode, funcNameInBitcode, false);
    long funcAddr = testApi.getFuncAddrFromCompileState(compileState);
    new AsmInjector().injectFuncAddr(funcAddr, m);
    // testApi.releaseCompileState(compileState);
  }

  static void initializeNativeMultiply() throws Exception {
    Class clazz = newJVMCIBaseClass();
    Method m = clazz.getMethod("binaryIntOp", int.class, int.class);
    genMultiplyObj = (JVMCIBaseClass) clazz.getConstructor().newInstance();

    // Injects a generated native function from LLVM bitcode
    String bitcodeFile = "pyMultiply-int32.bc";
    String funcNameInBitcode = "cfunc._ZN8__main__14pyMultiply$242Eii";
    byte[] bitcode = byteArrayFromResource(bitcodeFile);
    long compileState = testApi.compileToFunc(bitcode, funcNameInBitcode, false);
    long funcAddr = testApi.getFuncAddrFromCompileState(compileState);
    new AsmInjector().injectFuncAddr(funcAddr, m);
    // testApi.releaseCompileState(compileState);
  }

  static {
    try {
      testApi.initialize();
      initializeNativePlus();
      initializeNativeMultiply();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  public static int nativePlus(int a, int b) {
    return genPlusObj.binaryIntOp(a, b);
  }

  public static int nativeMultiply(int a, int b) {
    return genMultiplyObj.binaryIntOp(a, b);
  }
}
