package io.github.maropu;

import java.lang.reflect.Method;

import io.github.maropu.jvmci.AsmInjector;
import io.github.maropu.nvlib.TestRuntimeNative;
import io.github.maropu.nvlib.TestRuntimeNativeLoader;

public class IntAddJVMCITest {

  private static final TestRuntimeNative testApi = TestRuntimeNativeLoader.loadApi();

  // Address of the function defined in `libtest.dylib`
  private static final long nativeFuncAddr =
    testApi.getIntFuncAddr(TestRuntimeNative.FUNCTION_ID_ADD);
  private static Method generatedMethod;
  private static JVMCIBaseClass generatedObj;

  static void initializeNativeAdd() throws Exception {
    Method m = IntAddJVMCITest.class.getMethod("nativeAdd", int.class, int.class);
    new AsmInjector().injectFuncAddr(nativeFuncAddr, m);
  }

  static void initializePyNativeAdd() throws Exception {
    // Injects a generated native function from LLVM bitcode
    String bitcodeFile = "pyAdd-int32.bc";
    String funcNameInBitcode = "cfunc._ZN8__main__9pyAdd$241Eii";
    byte[] bitcode = Utils.byteArrayFromResource(bitcodeFile);
    Utils.debugPrint(
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
    long compileState = testApi.compileToFunc(bitcode, funcNameInBitcode, false);
    long funcAddr = testApi.getFuncAddrFromCompileState(compileState);

     // Generates a base class to call native code
    Class clazz = JVMCIBaseClass.createDerivedClass();
    generatedMethod = clazz.getMethod("binaryIntOp", int.class, int.class);
    generatedObj = (JVMCIBaseClass) clazz.getConstructor().newInstance();
    new AsmInjector().injectFuncAddr(funcAddr, generatedMethod);
    // testApi.releaseCompileState(compileState);
  }

  static {
    try {
      testApi.initialize();
      initializeNativeAdd();
      initializePyNativeAdd();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  public static int nativeAdd(int a, int b) {
    throw new UnsupportedOperationException("from Java code");
  }

  public static int pyNativeAddThruInvoke(int a, int b) throws Exception {
    return (int) generatedMethod.invoke(generatedObj, a, b);
  }

  public static int pyNativeAdd(int a, int b) {
    return generatedObj.binaryIntOp(a, b);
  }

  public static int jvmAdd(int a, int b) {
    return a + b;
  }

  public static int jniAdd(int a, int b) {
    return testApi.callIntFuncFromAddr(nativeFuncAddr, a, b);
  }
}
