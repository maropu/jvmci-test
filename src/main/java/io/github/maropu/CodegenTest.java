package io.github.maropu;

import java.lang.reflect.Method;

import io.github.maropu.jvmci.AsmInjector;
import io.github.maropu.nvlib.TestRuntimeNative;
import io.github.maropu.nvlib.TestRuntimeNativeLoader;

public class CodegenTest {

  private static final TestRuntimeNative testApi = TestRuntimeNativeLoader.loadApi();

  // Plus method based on `JVMCIBaseClass`
  private static JVMCIBaseClass genPlusObj;

  // Multiply method based on `JVMCIBaseClass`
  private static JVMCIBaseClass genMultiplyObj;

  static void initializeNativePlus() throws Exception {
    Class clazz = JVMCIBaseClass.createDerivedClass();
    Method m = clazz.getMethod("binaryIntOp", int.class, int.class);
    genPlusObj = (JVMCIBaseClass) clazz.getConstructor().newInstance();

    // Injects a generated native function from LLVM bitcode
    String bitcodeFile = "pyAdd-int32.bc";
    String funcNameInBitcode = "cfunc._ZN8__main__9pyAdd$241Eii";
    byte[] bitcode = Utils.byteArrayFromResource(bitcodeFile);
    long compileState = testApi.compileToFunc(bitcode, funcNameInBitcode, false);
    long funcAddr = testApi.getFuncAddrFromCompileState(compileState);
    new AsmInjector().injectFuncAddr(funcAddr, m);
    // testApi.releaseCompileState(compileState);
  }

  static void initializeNativeMultiply() throws Exception {
    Class clazz = JVMCIBaseClass.createDerivedClass();
    Method m = clazz.getMethod("binaryIntOp", int.class, int.class);
    genMultiplyObj = (JVMCIBaseClass) clazz.getConstructor().newInstance();

    // Injects a generated native function from LLVM bitcode
    String bitcodeFile = "pyMultiply-int32.bc";
    String funcNameInBitcode = "cfunc._ZN8__main__14pyMultiply$242Eii";
    byte[] bitcode = Utils.byteArrayFromResource(bitcodeFile);
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
