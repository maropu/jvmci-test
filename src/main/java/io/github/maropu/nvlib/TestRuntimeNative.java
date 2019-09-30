package io.github.maropu.nvlib;

public class TestRuntimeNative {
  public native void initialize() throws RuntimeException;
  public native void finalize() throws RuntimeException;

  // (int, int) => int
  public native long getIntFuncAddr();
  public native int callIntFuncFromAddr(long f, int a, int b);
  // (double, double) => double
  public native long getDoubleFuncAddr();
  public native double callDoubleFuncFromAddr(long f, double a, double b);

  // For APIs to handle LLVM bitcode
  public native long compileToFunc(byte[] bitcode, String funcName);
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
