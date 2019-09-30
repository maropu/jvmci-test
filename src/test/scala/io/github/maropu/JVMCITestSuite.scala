package io.github.maropu

import org.scalatest.FunSuite

class JVMCITestSuite extends FunSuite {

  private val intAddFunctions = Seq(
    ("jvmAdd", IntAddJVMCITest.jvmAdd _),
    ("jniAdd", IntAddJVMCITest.jniAdd _),
    ("nativeAdd", IntAddJVMCITest.nativeAdd _),
    ("nativeAddInGeneratedMethod", IntAddJVMCITest.nativeAddInGeneratedMethod _),
    ("pyNativeAdd", IntAddJVMCITest.pyNativeAdd _)
  )

  private val jvmciFunctions = Seq(
    IntAddJVMCITest.nativeAdd _,
    IntAddJVMCITest.nativeAddInGeneratedMethod _,
    IntAddJVMCITest.pyNativeAdd _
  )

  intAddFunctions.foreach { case (testName, f) =>
    test(s"${classOf[IntAddJVMCITest].getSimpleName}.$testName") {
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
