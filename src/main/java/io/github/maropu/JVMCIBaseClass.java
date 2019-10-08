package io.github.maropu;

import org.codehaus.janino.ClassBodyEvaluator;

public abstract class JVMCIBaseClass {

  public abstract int binaryIntOp(int a, int b);

  static Class createDerivedClass() throws Exception {
    ClassBodyEvaluator ev = new ClassBodyEvaluator();
    ev.setClassName("io.github.maropu.GeneratedClass");
    ev.setParentClassLoader(Thread.currentThread().getContextClassLoader());
    ev.setExtendedClass(JVMCIBaseClass.class);
    String code = "public int binaryIntOp(int a, int b) { " +
      "throw new java.lang.UnsupportedOperationException(\"from generated Java code\"); }";
    ev.cook("generated.java", code);
    return ev.getClazz();
  }
}
