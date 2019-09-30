#include "io_github_maropu_nvlib_TestRuntimeNative.h"
#include "jni_types.h"

#include <sstream>

#include <llvm/ADT/SmallVector.h>
#include <llvm/ADT/Triple.h>
#include <llvm/Bitcode/BitcodeReader.h>
#include <llvm/ExecutionEngine/MCJIT.h>
#include <llvm/ExecutionEngine/ExecutionEngine.h>
#include <llvm/IR/Module.h>
#include <llvm/IR/LegacyPassManager.h>
#include <llvm/IR/IRBuilder.h>
#include <llvm/IR/IRPrintingPasses.h>
#include <llvm/IR/TypeBuilder.h>
#include <llvm/Support/TargetSelect.h>
#include <llvm/Support/TargetRegistry.h>
#include <llvm/Target/TargetMachine.h>

#define test_rt_unreachable(msg) \
  test_rt_unreachable_internal(msg, __FILE__, __LINE__)

static void test_rt_unreachable_internal(const std::string& errMsg, const char *file, unsigned line) {
  std::stringstream msg;
  msg << "!!UNREACHABLE!! (file=" << file << " line=" << line << ") " << errMsg;
  throw msg.str();
}

static void throwException(JNIEnv *env, jobject& self, const std::string& errMsg) {
  jclass c = env->FindClass("io/github/maropu/TestRuntimeNative");
  assert(c != 0);
  jmethodID mth_throwex = env->GetMethodID(c, "throwException", "(Ljava/lang/String;)V");
  assert(mth_throwex != 0);
  env->CallVoidMethod(self, mth_throwex, env->NewStringUTF(errMsg.c_str()));
}

JNIEXPORT void JNICALL Java_io_github_maropu_nvlib_TestRuntimeNative_initialize
    (JNIEnv *, jobject) {
  // Initializes states for MCJIT
  llvm::InitializeNativeTarget();
  LLVMInitializeNativeAsmPrinter();
  LLVMInitializeNativeAsmParser();
}

JNIEXPORT void JNICALL Java_io_github_maropu_nvlib_TestRuntimeNative_finalize
  (JNIEnv *, jobject) {}

static std::unique_ptr<llvm::Module> parseBitcode(char *bitcode, size_t len, llvm::LLVMContext& ctx) {
  llvm::ErrorOr<std::unique_ptr<llvm::MemoryBuffer>> bufferOrError =
    llvm::MemoryBuffer::getMemBuffer(llvm::StringRef(bitcode, len));
  if (!bufferOrError) {
    throw "Can't load the given bitocde into memory.";
  }
  llvm::Expected<std::unique_ptr<llvm::Module>> modOrError =
    llvm::parseBitcodeFile(bufferOrError.get()->getMemBufferRef(), ctx);
  if (!modOrError) {
    throw "Can't parse the given bitcode to build a LLVM module.";
  }
  return std::move(modOrError.get());
}

static const std::string toLLVMAssemblyCodeFromModule(llvm::Module *m) {
  // Prepares an output pass for LLVM IR assembly code
  llvm::legacy::PassManager pm;
  std::string strbuf;
  llvm::raw_string_ostream out(strbuf);
  llvm::Pass *outputPass = llvm::createPrintModulePass(out, "", false);
  pm.add(outputPass);
  // Gets LLVM assembly code
  pm.run(*m);
  out.flush();

  return strbuf;
}

static const std::string toLLVMAssemblyCode(char *bitcode, size_t size) {
  llvm::LLVMContext context;
  std::unique_ptr<llvm::Module> m = parseBitcode(bitcode, size, context);
  return toLLVMAssemblyCodeFromModule(m.get());
}

int testIntAdd(void *obj, int a, int b) {
  return a + b;
}

JNIEXPORT jlong JNICALL Java_io_github_maropu_nvlib_TestRuntimeNative_getIntFuncAddr
    (JNIEnv *, jobject) {
  return (long) &testIntAdd;
}

JNIEXPORT jint JNICALL Java_io_github_maropu_nvlib_TestRuntimeNative_callIntFuncFromAddr
    (JNIEnv *, jobject, jlong addr, jint a, jint b) {
  int (*fpFunc)(void *, int, int) = (int (*)(void *, int, int)) addr;
  return fpFunc(NULL, a, b);
}

double testDoubleAdd(void *obj, double a, double b) {
  return a + b;
}

JNIEXPORT jlong JNICALL Java_io_github_maropu_nvlib_TestRuntimeNative_getDoubleFuncAddr
    (JNIEnv *, jobject) {
  return (long) &testDoubleAdd;
}

JNIEXPORT jdouble JNICALL Java_io_github_maropu_nvlib_TestRuntimeNative_callDoubleFuncFromAddr
    (JNIEnv *, jobject, jlong addr, jdouble a, jdouble b) {
  double (*fpFunc)(void *, double, double) = (double (*)(void *, double, double)) addr;
  return fpFunc(NULL, a, b);
}

struct CompileState {
  llvm::ExecutionEngine *engine_;
  long  f_;

  CompileState(llvm::ExecutionEngine *engine, long f)
    : engine_(engine), f_(f) {}
  ~CompileState() { delete engine_; }
};

JNIEXPORT jlong JNICALL Java_io_github_maropu_nvlib_TestRuntimeNative_compileToFunc
    (JNIEnv *env, jobject self, jbyteArray bitcode, jstring funcName) {

  const std::string entryPointFuncName = "entry_point";

  try {
    // First, loads the input bitcode
    llvm::LLVMContext context;
    JniPrimitiveArrayPtr bitcodeArray(env, bitcode);
    std::unique_ptr<llvm::Module> m = parseBitcode((char *)bitcodeArray.get(), bitcodeArray.size(), context);
    JniString funcNameStr(env, funcName);
    llvm::Function *f = m->getFunction(funcNameStr.str());
    if (f == NULL) {
      std::stringstream errMsg;
      errMsg << "Function not found: " << funcNameStr.str();
      test_rt_unreachable(errMsg.str());
    }

    // Inserts a trampoline code in the LLVM module:
    //  - http://releases.llvm.org/7.0.1/docs/tutorial/index.html
    // llvm::FunctionType *epf_signature = llvm::TypeBuilder<int(void *, int, int), false>::get(context);
    llvm::FunctionType *epf_signature = llvm::FunctionType::get(
      llvm::Type::getInt32Ty(context),
      {llvm::PointerType::get(llvm::Type::getVoidTy(context), 0), llvm::Type::getInt32Ty(context), llvm::Type::getInt32Ty(context)},
      false
    );
    llvm::Function *epf = llvm::cast<llvm::Function>(m->getOrInsertFunction(entryPointFuncName, epf_signature));
    epf->setCallingConv(llvm::CallingConv::C);
    epf->arg_begin();
    llvm::Function::arg_iterator args = epf->arg_begin();
    llvm::Value *obj = args++;
    obj->setName("obj");
    llvm::Value *x = args++;
    x->setName("x");
    llvm::Value *y = args++;
    y->setName("y");
    llvm::BasicBlock *block = llvm::BasicBlock::Create(context, "entry", epf);
    llvm::IRBuilder<> builder(block);
    llvm::Value *result = builder.CreateCall(f, {x, y});
    builder.CreateRet(result);
    // fprintf(stderr, "%s", toLLVMAssemblyCodeFromModule(m.get()).c_str());

    // Creates execution engine
    std::string errStr;
    llvm::EngineBuilder eb(std::move(m));
    eb.setUseOrcMCJITReplacement(false);
    std::unique_ptr<llvm::ExecutionEngine> engine(
      eb.setErrorStr(&errStr).setEngineKind(llvm::EngineKind::JIT).create());
    if (!engine) {
      std::stringstream errMsg;
      errMsg << "Can't create ExecutionEngine: " << errStr;
      throw errMsg.str();
    }

    long funcAddr = engine->getFunctionAddress(entryPointFuncName);
    // long funcAddr = engine->getFunctionAddress(funcNameStr.str());
    if (funcAddr == 0) {
      std::stringstream errMsg;
      errMsg << "Function not found: " << funcNameStr.str();
      test_rt_unreachable(errMsg.str());
    }

    return (long) new CompileState(engine.release(), funcAddr);
  } catch (const std::string &e) {
    throwException(env, self, e);
  }
  return 0L;
}

JNIEXPORT jlong JNICALL Java_io_github_maropu_nvlib_TestRuntimeNative_getFuncAddrFromCompileState
    (JNIEnv *, jobject, jlong state) {
  return ((CompileState *) state)->f_;
}

JNIEXPORT void JNICALL Java_io_github_maropu_nvlib_TestRuntimeNative_releaseCompileState
    (JNIEnv *, jobject, jlong state) {
  delete (CompileState *) state;
}

JNIEXPORT jstring JNICALL Java_io_github_maropu_nvlib_TestRuntimeNative_toLLVMAssemblyCode
    (JNIEnv *env, jobject self, jbyteArray bitcode, jint optLevel, jint sizeLevel) {

  try {
    JniPrimitiveArrayPtr bitcodeArray(env, bitcode);
    std::string llvmAsm = toLLVMAssemblyCode((char *)bitcodeArray.get(), bitcodeArray.size());
    return env->NewStringUTF(llvmAsm.c_str());
  } catch (const std::string &e) {
    throwException(env, self, e);
  }
  return env->NewStringUTF("N/A");
}

static llvm::TargetMachine *lookupTargetMachine(const std::string& arch) {
  llvm::TargetOptions options;
  llvm::Triple triple;
  std::string errStr;
  // Creates a target machine given an architecture name.
  // For a list of architectures:
  //  $ llc -help
  // For a list of available CPUs:
  //  $ llvm-as < /dev/null | llc -march=xyz -mcpu=help
  // For a list of available attributes (features):
  //  $ llvm-as < /dev/null | llc -march=xyz -mattr=help
  const llvm::Target *target = llvm::TargetRegistry::lookupTarget(arch, triple, errStr);
  if (!target) {
    std::stringstream errMsg;
    errMsg << "Can't create Target: " << errStr;
    throw errMsg.str();
  }
  const std::string cpu = "";
  const std::string features = "";
  llvm::TargetMachine *machine = target->createTargetMachine(
    triple.str(), cpu, features, options, llvm::Reloc::Model::Static);
  if (!machine) {
    std::stringstream errMsg;
    errMsg << "Can't create TargetMachine: " << arch;
    throw errMsg.str();
  }
  return machine;
}

static const std::string toMachineAssemblyCode(const std::string& arch, char *bitcode, size_t size) {
  // Prepares an output pass for machine assembly code
  llvm::LLVMContext context;
  llvm::legacy::PassManager pm;
  llvm::SmallVector<char, 1024> strbuf;
  llvm::raw_svector_ostream output(strbuf);
  if (lookupTargetMachine(arch)->addPassesToEmitFile(
      pm, output, nullptr, llvm::TargetMachine::CGFT_AssemblyFile)) {
    throw "Can't add an output pass for machine assembly code";
  }

  // Gets machine assembly code
  std::unique_ptr<llvm::Module> m = parseBitcode(bitcode, size, context);
  pm.run(*m.get());

  return output.str().str();
}

JNIEXPORT jstring JNICALL Java_io_github_maropu_nvlib_TestRuntimeNative_toMachineAssemblyCode
    (JNIEnv *env, jobject self, jstring arch, jbyteArray bitcode, jint optLevel, jint sizeLevel) {

  try {
    JniString archStr(env, arch);
    JniPrimitiveArrayPtr bitcodeArray(env, bitcode);
    const std::string machineAsm = toMachineAssemblyCode(
      archStr.str(), (char *)bitcodeArray.get(), bitcodeArray.size());
    return env->NewStringUTF(machineAsm.c_str());
  } catch (const std::string &e) {
    throwException(env, self, e);
  }
  return env->NewStringUTF("N/A");
}

