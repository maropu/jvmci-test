## JMH benchmark results

    $ ./build/mvn clean package -DskipTests
    $ ./bin/run-jmh-benchmarks.sh
    # Run complete. Total time: 00:01:42

     Benchmark                           Mode  Cnt        Score       Error  Units
     LoopAdd.jniAdd                      avgt   10  1003890.060 ± 14423.020  ns/op
     LoopAdd.jvmAdd                      avgt   10    17700.934 ±   157.821  ns/op
     LoopAdd.nativeAdd                   avgt   10   367751.265 ±  5143.389  ns/op
     LoopAdd.nativeAddInGeneratedMethod  avgt   10   587851.174 ± 14336.564  ns/op
     LoopAdd.pyNativeAdd                 avgt   10   331251.631 ±  5216.444  ns/op

## Requirements

 - JDK11
 - Mac/x86_64

