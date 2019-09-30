package io.github.maropu.jvmci;

import java.lang.reflect.Method;

import jdk.vm.ci.code.CodeCacheProvider;
import jdk.vm.ci.code.InstalledCode;
import jdk.vm.ci.code.Register;
import jdk.vm.ci.hotspot.HotSpotCompiledCode;
import jdk.vm.ci.hotspot.HotSpotJVMCIRuntime;
import jdk.vm.ci.hotspot.HotSpotResolvedJavaMethod;
import jdk.vm.ci.meta.JavaKind;
import jdk.vm.ci.meta.MetaAccessProvider;
import jdk.vm.ci.runtime.JVMCI;
import jdk.vm.ci.runtime.JVMCIBackend;

import io.github.maropu.jvmci.amd64.AMD64Assembler;

// The original author of this class is Yasumasa Suenaga, OpenJDK Reviewer (https://github.com/YaSuenag)
public class AsmInjector {
  private final MetaAccessProvider metaAccess;
  private final CodeCacheProvider codeCache;
  private final AMD64Assembler asm;

  public AsmInjector() {
    JVMCIBackend backend = JVMCI.getRuntime().getHostJVMCIBackend();
    this.metaAccess = backend.getMetaAccess();
    this.codeCache = backend.getCodeCache();
    this.asm = new AMD64Assembler(HotSpotJVMCIRuntime.runtime().getConfigStore());
  }

  private Register getReturnReg(Class<?> klass){
    return codeCache.getRegisterConfig().getReturnRegister(JavaKind.fromJavaClass(klass));
  }

  public InstalledCode injectFuncAddr(long addr, Method method) {
    HotSpotResolvedJavaMethod resolvedMethod =
      (HotSpotResolvedJavaMethod) metaAccess.lookupJavaMethod(method);
    // To avoid the injection invalidation caused by OSR（On-Stack Replacement)
    resolvedMethod.setNotInlinableOrCompilable();
    asm.emitPrologue();
    asm.emitCall(addr);
    asm.emitIntRet(getReturnReg(int.class));
    asm.emitEpilogue();
    HotSpotCompiledCode code = asm.finish(resolvedMethod);
    return codeCache.setDefaultCode(resolvedMethod, code);
  }
}
