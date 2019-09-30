package io.github.maropu.nvlib;

public class TestRuntimeNative {
  public native void initialize() throws RuntimeException;
  public native void finalize() throws RuntimeException;

  public native long getFuncAddr();
  public native int callFuncFromAddr(long f, int a, int b);
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
