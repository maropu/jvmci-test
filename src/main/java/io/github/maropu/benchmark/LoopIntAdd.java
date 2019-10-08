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

import io.github.maropu.IntAddJVMCITest;

// -- openjdk version "11.0.2" 2019-01-15 (Intel Core i5)
// Benchmark                           Mode  Cnt        Score       Error  Units
// LoopIntAdd.jniAdd                      avgt   10  1003890.060 ± 14423.020  ns/op
// LoopIntAdd.jvmAdd                      avgt   10    17700.934 ±   157.821  ns/op
// LoopIntAdd.nativeAdd                   avgt   10   367751.265 ±  5143.389  ns/op
// LoopIntAdd.nativeAddInGeneratedMethod  avgt   10   587851.174 ± 14336.564  ns/op
// LoopIntAdd.pyNativeAdd                 avgt   10   331251.631 ±  5216.444  ns/op
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
  // "-XX:CompileCommand=print,*LoopIntAdd.*",
  // "-XX:+PrintAssembly", // Print all the assembly
  // "-XX:PrintAssemblyOptions=intel"
})
@Warmup(iterations = 5)
@Measurement(iterations = 10)
public class LoopIntAdd {
  final static int SIZE = 65536;

  @State(Scope.Thread)
  public static class Context {
    public final int[] ar1 = new int[SIZE];
    public final int[] ar2 = new int[SIZE];
    public final int[] ar3 = new int[SIZE];

    @Setup
    public void setup() {
      // Initializes the input with the same data
      Random random = new Random();
      for (int i = 0; i < SIZE; i++) {
        int value = random.nextInt() % 32;
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
      context.ar3[i] = IntAddJVMCITest.jvmAdd(context.ar1[i], context.ar2[i]);
    }
  }

  @Benchmark
  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  public void jniAdd(Context context) {
    for (int i = 0; i < SIZE; i++) {
      context.ar3[i] = IntAddJVMCITest.jniAdd(context.ar1[i], context.ar2[i]);
    }
  }

  @Benchmark
  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  public void nativeAdd(Context context) {
    for (int i = 0; i < SIZE; i++) {
      context.ar3[i] = IntAddJVMCITest.nativeAdd(context.ar1[i], context.ar2[i]);
    }
  }

  @Benchmark
  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  public void nativeAddInGeneratedMethod(Context context) throws Exception {
    for (int i = 0; i < SIZE; i++) {
      context.ar3[i] = IntAddJVMCITest.nativeAddInGeneratedMethod(context.ar1[i], context.ar2[i]);
    }
  }

  @Benchmark
  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  public void pyNativeAdd(Context context) {
    for (int i = 0; i < SIZE; i++) {
      context.ar3[i] = IntAddJVMCITest.pyNativeAdd(context.ar1[i], context.ar2[i]);
    }
  }
}
