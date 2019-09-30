package io.github.maropu

import org.scalatest.FunSuite

class JVMCITestSuite extends FunSuite {
  import JVMCITest._

  private val allFunctions = Seq(
    ("jvmAdd", jvmAdd _),
    ("jniAdd", jniAdd _),
    ("nativeAdd", nativeAdd _),
    ("nativeAddInGeneratedMethod", nativeAddInGeneratedMethod _),
    ("pyNativeAdd", pyNativeAdd _)
  )

  private val jvmciFunctions =
    Seq(nativeAdd _, nativeAddInGeneratedMethod _, pyNativeAdd _)

  allFunctions.foreach { case (testName, f) =>
    test(testName) {
      assert(f(-3, 7) === 4)
      assert(f(3, 2) === 5)
      assert(f(1, 6) === 7)
      assert(f(3, 9) === 12)
    }
  }

  test("injection invalidation caused by OSR（On-Stack Replacement)") {
    jvmciFunctions.foreach { f =>
      val numLoop = 1000
      var sum = 0
      (0 until numLoop).foreach { _ => sum = f(sum, 1) }
      assert(sum === numLoop)
    }
  }
}
