/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package io.github.maropu.jvmci;

import java.util.ArrayList;

import jdk.vm.ci.code.DebugInfo;
import jdk.vm.ci.code.Register;
import jdk.vm.ci.code.StackSlot;
import jdk.vm.ci.code.site.Call;
import jdk.vm.ci.code.site.DataPatch;
import jdk.vm.ci.code.site.Mark;
import jdk.vm.ci.code.site.Site;
import jdk.vm.ci.hotspot.HotSpotCompiledCode;
import jdk.vm.ci.hotspot.HotSpotCompiledCode.Comment;
import jdk.vm.ci.hotspot.HotSpotCompiledNmethod;
import jdk.vm.ci.hotspot.HotSpotResolvedJavaMethod;
import jdk.vm.ci.meta.*;

/**
 * This code in this class is extracted from the JDK code base below;
 * http://hg.openjdk.java.net/jdk-updates/jdk9u/hotspot/file/bb73b31e70e3/test/compiler/jvmci/jdk.vm.ci.code.test/src/jdk/vm/ci/code/test/TestAssembler.java
 */
public abstract class AssemblerBase {

  protected final CodeBuffer code;
  protected final CodeBuffer data;

  private final ArrayList<Site> sites;
  private final ArrayList<DataPatch> dataPatches;
  private int frameSize;
  private int stackAlignment;
  private int curStackSlot;
  private StackSlot deoptRescue;

  private final Register[] registers;
  private int nextRegister;

  protected AssemblerBase(int initialFrameSize, int stackAlignment, Register... registers) {
    this.code = new CodeBuffer();
    this.data = new CodeBuffer();
    this.sites = new ArrayList<>();
    this.dataPatches = new ArrayList<>();
    this.frameSize = initialFrameSize;
    this.stackAlignment = stackAlignment;
    this.curStackSlot = initialFrameSize;
    this.deoptRescue = null;
    this.registers = registers;
    this.nextRegister = 0;
  }

  // Minimal API set that we need to implement for injecting native code
  public abstract void emitPrologue();
  public abstract void emitEpilogue();
  public abstract void emitGrowStack(int size);
  public abstract Register emitLoadLong(long value);
  public abstract void emitCall(long addr);
  public abstract void emitIntRet(Register a);

  protected void setDeoptRescueSlot(StackSlot deoptRescue) {
    this.deoptRescue = deoptRescue;
  }

  protected Register newRegister() {
    return registers[nextRegister++];
  }

  static class TestValueKind extends ValueKind<TestValueKind> {
    TestValueKind(PlatformKind kind) {
      super(kind);
    }
    @Override
    public TestValueKind changeType(PlatformKind kind) {
      return new TestValueKind(kind);
    }
  }

  protected void growFrame(int sizeInBytes) {
    curStackSlot += sizeInBytes;
    if (curStackSlot > frameSize) {
      int newFrameSize = curStackSlot;
      if (newFrameSize % stackAlignment != 0) {
          newFrameSize += stackAlignment - (newFrameSize % stackAlignment);
      }
      emitGrowStack(newFrameSize - frameSize);
      frameSize = newFrameSize;
    }
  }

  protected StackSlot newStackSlot(PlatformKind kind) {
    growFrame(kind.getSizeInBytes());
    return StackSlot.get(new TestValueKind(kind), -curStackSlot, true);
  }

  protected void recordMark(Object id) {
    sites.add(new Mark(code.position(), id));
  }

  protected void recordCall(InvokeTarget target, int size, boolean direct, DebugInfo debugInfo) {
    sites.add(new Call(target, code.position(), size, direct, debugInfo));
  }

  public HotSpotCompiledCode finish(HotSpotResolvedJavaMethod method) {
    int id = method.allocateCompileId(0);
    byte[] finishedCode = code.finish();
    Site[] finishedSites = sites.toArray(new Site[0]);
    byte[] finishedData = data.finish();
    DataPatch[] finishedDataPatches = dataPatches.toArray(new DataPatch[0]);
    return new HotSpotCompiledNmethod(
      method.getName(),                 // name
      finishedCode,                     // targetCode
      finishedCode.length,              // targetCodeSize
      finishedSites,                    // sites
      new Assumptions.Assumption[0],    // assumptions
      new ResolvedJavaMethod[]{method}, // methods
      new Comment[0],                   // comments
      finishedData,                     // dataSection
      16,                               // dataSectionAlignment
      finishedDataPatches,              // dataSectionPatches
      false,                            // isImmutablePIC
      frameSize,                        // totalFrameSize
      deoptRescue,                      // deoptRescueSlot
      method,                           // method
      -1,                               // entryBCI
      id,                               // id
      0L,                               // jvmciEnv
      false                             // hasUnsafeAccess
    );
  }
}
