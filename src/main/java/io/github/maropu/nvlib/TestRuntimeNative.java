package io.github.maropu.nvlib;

public class TestRuntimeNative {

  public static final int FUNCTION_ID_ADD = 1;
  public static final int FUNCTION_ID_MULTIPLY= 2;

  public native void initialize() throws RuntimeException;
  public native void finalize() throws RuntimeException;

  // funcId is 1 for add and 2 for multiply
  // (int, int) => int
  public native long getIntFuncAddr(int funcId);
  public native int callIntFuncFromAddr(long f, int a, int b);

  // For APIs to handle LLVM bitcode
  public native long compileToFunc(byte[] bitcode, String funcName, boolean isStatic);
  public native long getFuncAddrFromCompileState(long state);
  public native void releaseCompileState(long state);

  // For debug logging, returns LLVM IR code for given LLVM bitcode
  public native String toLLVMAssemblyCode(
    byte[] bitcode, int optLevel, int sizeLevel) throws RuntimeException;

  // For debug logging, returns human-readable Machine code for given LLVM bitcode
  public native String toMachineAssemblyCode(
    String arch, byte[] bitcode, int optLevel, int sizeLevel) throws RuntimeException;

  public String toLLVMAssemblyCode(byte[] bitcode) throws RuntimeException {
    return toLLVMAssemblyCode(bitcode, 0, 0);
  }

  public String toX86_64AssemblyCode(byte[] bitcode) throws RuntimeException {
    return toMachineAssemblyCode("x86-64", bitcode, 0, 0);
  }

  // This helper method is mainly used from native code
  public void throwException(String message) throws RuntimeException {
    throw new RuntimeException(message);
  }
}
