package io.github.maropu

import org.scalatest.FunSuite

class CodegenSuite extends FunSuite {

  test("codegen") {
    assert(CodegenTest.nativePlus(3, 4) === 7)
    assert(CodegenTest.nativeMultiply(3, 4) === 12)
  }
}
