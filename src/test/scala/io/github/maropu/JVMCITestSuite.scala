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

  private val doubleAddFunctions = Seq(
    ("jvmAdd", DoubleAddJVMCITest.jvmAdd _),
    ("jniAdd", DoubleAddJVMCITest.jniAdd _),
    ("nativeAdd", DoubleAddJVMCITest.nativeAdd _),
    ("nativeAddInGeneratedMethod", DoubleAddJVMCITest.nativeAddInGeneratedMethod _),
    ("pyNativeAdd", DoubleAddJVMCITest.pyNativeAdd _)
  )

  intAddFunctions.foreach { case (testName, f) =>
    test(s"${classOf[IntAddJVMCITest].getSimpleName}.$testName") {
      assert(f(-3, 7) === 4)
      assert(f(3, 2) === 5)
      assert(f(1, 6) === 7)
      assert(f(3, 9) === 12)
    }
  }

  doubleAddFunctions.foreach { case (testName, f) =>
    test(s"${classOf[DoubleAddJVMCITest].getSimpleName}.$testName") {
      assert(f(-3.0, 7.0) === 4.0)
      assert(f(3.0, 2.0) === 5.0)
      assert(f(1.0, 6.0) === 7.0)
      assert(f(3.0, 9.0) === 12.0)
    }
  }

  test("injection invalidation caused by OSR（On-Stack Replacement)") {
    val testFuncName = Seq("nativeAdd", "nativeAddInGeneratedMethod", "pyNativeAdd")
    intAddFunctions.filter(f => testFuncName.contains(f._1)).map(_._2).foreach { f =>
      val numLoop = 1000
      var sum = 0
      (0 until numLoop).foreach { _ => sum = f(sum, 1) }
      assert(sum === numLoop)
    }
    doubleAddFunctions.filter(f => testFuncName.contains(f._1)).map(_._2).foreach { f =>
      val numLoop = 1000
      var sum = 0.0
      (0 until numLoop).foreach { _ => sum = f(sum, 1.0) }
      assert(sum === numLoop)
    }
  }
}
