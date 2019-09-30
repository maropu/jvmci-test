package io.github.maropu

import org.scalatest.FunSuite

class JVMCITestSuite extends FunSuite {

  test("jvmAdd") {
    assert(io.github.maropu.JVMCITest.jvmAdd(11, 12) === 23)
  }

  test("jniAdd") {
    assert(io.github.maropu.JVMCITest.jniAdd(3, 7) === 10)
  }

  test("nativeAdd") {
    assert(io.github.maropu.JVMCITest.nativeAdd(1, 4) === 5)
  }

  test("nativeAddInGeneratedMethod") {
    assert(io.github.maropu.JVMCITest.nativeAddInGeneratedMethod(9, 8) === 17)
  }

  test("pyNativeAdd") {
    assert(io.github.maropu.JVMCITest.pyNativeAdd(3, 9) === 12)
  }
}
