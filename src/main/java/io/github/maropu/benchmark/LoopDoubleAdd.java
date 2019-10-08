package io.github.maropu.benchmark;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import io.github.maropu.DoubleAddJVMCITest;

// -- openjdk version "11.0.2" 2019-01-15 (Intel Core i5)
// Benchmark                                 Mode  Cnt        Score        Error  Units
// LoopDoubleAdd.jniAdd                      avgt   10  1087055.429 ± 179824.014  ns/op
// LoopDoubleAdd.jvmAdd                      avgt   10    35211.822 ±    571.382  ns/op
// LoopDoubleAdd.nativeAdd                   avgt   10   375474.968 ±  10777.673  ns/op
// LoopDoubleAdd.nativeAddInGeneratedMethod  avgt   10   732111.493 ±  29137.571  ns/op
// LoopDoubleAdd.pyNativeAdd                 avgt   10   335338.161 ±   5401.079  ns/op
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@Fork(value = 1, jvmArgsAppend = {
  "--add-modules",
  "jdk.internal.vm.ci",
  "--add-modules",
  "jdk.internal.vm.compiler",
  "--add-exports",
  "jdk.internal.vm.ci/jdk.vm.ci.meta=ALL-UNNAMED",
  "--add-exports",
  "jdk.internal.vm.ci/jdk.vm.ci.code=ALL-UNNAMED",
  "--add-exports",
  "jdk.internal.vm.ci/jdk.vm.ci.code.site=ALL-UNNAMED",
  "--add-exports",
  "jdk.internal.vm.ci/jdk.vm.ci.runtime=ALL-UNNAMED,jdk.internal.vm.compiler",
  "--add-exports",
  "jdk.internal.vm.ci/jdk.vm.ci.hotspot=ALL-UNNAMED,jdk.internal.vm.compiler",
  "--add-exports",
  "jdk.internal.vm.ci/jdk.vm.ci.amd64=ALL-UNNAMED",
  "--add-exports",
  "jdk.internal.vm.compiler/org.graalvm.compiler.api.runtime=ALL-UNNAMED",
  "--add-exports",
  "jdk.internal.vm.compiler/org.graalvm.compiler.hotspot=ALL-UNNAMED",
  "--add-opens",
  "jdk.internal.vm.ci/jdk.vm.ci.hotspot=ALL-UNNAMED",
  "-XX:+UnlockExperimentalVMOptions",
  "-XX:+EnableJVMCI",
  "-XX:+UseSuperWord",
  // "-XX:+UnlockDiagnosticVMOptions",
  // "-XX:CompileCommand=print,*LoopDoubleAdd.*",
  // "-XX:+PrintAssembly", // Print all the assembly
  // "-XX:PrintAssemblyOptions=intel"
})
@Warmup(iterations = 5)
@Measurement(iterations = 10)
public class LoopDoubleAdd {
  final static int SIZE = 65536;

  @State(Scope.Thread)
  public static class Context {
    public final double[] ar1 = new double[SIZE];
    public final double[] ar2 = new double[SIZE];
    public final double[] ar3 = new double[SIZE];

    @Setup
    public void setup() {
      // Initializes the input with the same data
      Random random = new Random();
      for (int i = 0; i < SIZE; i++) {
        double value = random.nextDouble();
        ar1[i] = value;
        ar2[i] = value;
      }
    }
  }

  @Benchmark
  // This makes looking at assembly easier
  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  public void jvmAdd(Context context) {
    for (int i = 0; i < SIZE; i++) {
      context.ar3[i] = DoubleAddJVMCITest.jvmAdd(context.ar1[i], context.ar2[i]);
    }
  }

  @Benchmark
  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  public void jniAdd(Context context) {
    for (int i = 0; i < SIZE; i++) {
      context.ar3[i] = DoubleAddJVMCITest.jniAdd(context.ar1[i], context.ar2[i]);
    }
  }

  @Benchmark
  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  public void nativeAdd(Context context) {
    for (int i = 0; i < SIZE; i++) {
      context.ar3[i] = DoubleAddJVMCITest.nativeAdd(context.ar1[i], context.ar2[i]);
    }
  }

  @Benchmark
  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  public void nativeAddInGeneratedMethod(Context context) throws Exception {
    for (int i = 0; i < SIZE; i++) {
      context.ar3[i] = DoubleAddJVMCITest.nativeAddInGeneratedMethod(
        context.ar1[i], context.ar2[i]);
    }
  }

  @Benchmark
  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  public void pyNativeAdd(Context context) {
    for (int i = 0; i < SIZE; i++) {
      context.ar3[i] = DoubleAddJVMCITest.pyNativeAdd(context.ar1[i], context.ar2[i]);
    }
  }
}
