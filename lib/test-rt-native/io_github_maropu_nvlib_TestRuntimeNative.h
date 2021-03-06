/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class io_github_maropu_nvlib_TestRuntimeNative */

#ifndef _Included_io_github_maropu_nvlib_TestRuntimeNative
#define _Included_io_github_maropu_nvlib_TestRuntimeNative
#ifdef __cplusplus
extern "C" {
#endif
#undef io_github_maropu_nvlib_TestRuntimeNative_FUNCTION_ID_ADD
#define io_github_maropu_nvlib_TestRuntimeNative_FUNCTION_ID_ADD 1L
#undef io_github_maropu_nvlib_TestRuntimeNative_FUNCTION_ID_MULTIPLY
#define io_github_maropu_nvlib_TestRuntimeNative_FUNCTION_ID_MULTIPLY 2L
/*
 * Class:     io_github_maropu_nvlib_TestRuntimeNative
 * Method:    initialize
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_io_github_maropu_nvlib_TestRuntimeNative_initialize
  (JNIEnv *, jobject);

/*
 * Class:     io_github_maropu_nvlib_TestRuntimeNative
 * Method:    finalize
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_io_github_maropu_nvlib_TestRuntimeNative_finalize
  (JNIEnv *, jobject);

/*
 * Class:     io_github_maropu_nvlib_TestRuntimeNative
 * Method:    getIntFuncAddr
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_io_github_maropu_nvlib_TestRuntimeNative_getIntFuncAddr
  (JNIEnv *, jobject, jint);

/*
 * Class:     io_github_maropu_nvlib_TestRuntimeNative
 * Method:    callIntFuncFromAddr
 * Signature: (JII)I
 */
JNIEXPORT jint JNICALL Java_io_github_maropu_nvlib_TestRuntimeNative_callIntFuncFromAddr
  (JNIEnv *, jobject, jlong, jint, jint);

/*
 * Class:     io_github_maropu_nvlib_TestRuntimeNative
 * Method:    compileToFunc
 * Signature: ([BLjava/lang/String;Z)J
 */
JNIEXPORT jlong JNICALL Java_io_github_maropu_nvlib_TestRuntimeNative_compileToFunc
  (JNIEnv *, jobject, jbyteArray, jstring, jboolean);

/*
 * Class:     io_github_maropu_nvlib_TestRuntimeNative
 * Method:    getFuncAddrFromCompileState
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_io_github_maropu_nvlib_TestRuntimeNative_getFuncAddrFromCompileState
  (JNIEnv *, jobject, jlong);

/*
 * Class:     io_github_maropu_nvlib_TestRuntimeNative
 * Method:    releaseCompileState
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_io_github_maropu_nvlib_TestRuntimeNative_releaseCompileState
  (JNIEnv *, jobject, jlong);

/*
 * Class:     io_github_maropu_nvlib_TestRuntimeNative
 * Method:    toLLVMAssemblyCode
 * Signature: ([BII)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_io_github_maropu_nvlib_TestRuntimeNative_toLLVMAssemblyCode
  (JNIEnv *, jobject, jbyteArray, jint, jint);

/*
 * Class:     io_github_maropu_nvlib_TestRuntimeNative
 * Method:    toMachineAssemblyCode
 * Signature: (Ljava/lang/String;[BII)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_io_github_maropu_nvlib_TestRuntimeNative_toMachineAssemblyCode
  (JNIEnv *, jobject, jstring, jbyteArray, jint, jint);

#ifdef __cplusplus
}
#endif
#endif
