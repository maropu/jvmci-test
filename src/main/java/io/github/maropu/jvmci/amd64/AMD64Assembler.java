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

package io.github.maropu.jvmci.amd64;

import jdk.vm.ci.amd64.AMD64;
import jdk.vm.ci.amd64.AMD64Kind;
import jdk.vm.ci.code.Register;
import jdk.vm.ci.hotspot.HotSpotForeignCallTarget;
import jdk.vm.ci.hotspot.HotSpotVMConfigAccess;
import jdk.vm.ci.hotspot.HotSpotVMConfigStore;

import io.github.maropu.jvmci.AssemblerBase;

/**
 * This code in this class is extracted from the JDK code base below;
 * http://hg.openjdk.java.net/jdk-updates/jdk9u/hotspot/file/bb73b31e70e3/test/compiler/jvmci/jdk.vm.ci.code.test/src/jdk/vm/ci/code/test/amd64/AMD64TestAssembler.java
 */
public class AMD64Assembler extends AssemblerBase {

  private static class HotSpotVMConfigAccessor extends HotSpotVMConfigAccess {
    HotSpotVMConfigAccessor(HotSpotVMConfigStore store) {
      super(store);
    }
    final int MARKID_DEOPT_HANDLER_ENTRY =
      getConstant("CodeInstaller::DEOPT_HANDLER_ENTRY", Integer.class);
    final long handleDeoptStub =
      getFieldValue("CompilerToVM::Data::SharedRuntime_deopt_blob_unpack", Long.class, "address");
  }

  private final HotSpotVMConfigAccessor config;

  public AMD64Assembler(HotSpotVMConfigStore store) {
    super(16, 16, AMD64.rax, AMD64.rcx, AMD64.rdi, AMD64.r8, AMD64.r9, AMD64.r10);
    this.config = new HotSpotVMConfigAccessor(store);
  }

  private void emitFatNop() {
    // 5 byte NOP:
    // NOP DWORD ptr [EAX + EAX*1 + 00H]
    code.emitByte(0x0F);
    code.emitByte(0x1F);
    code.emitByte(0x44);
    code.emitByte(0x00);
    code.emitByte(0x00);
  }

  @Override
  public void emitPrologue() {
    // WARNING: Initial instruction MUST be 5 bytes or longer so that
    // NativeJump::patch_verified_entry will be able to patch out the entry
    // code safely.
    emitFatNop();
    code.emitByte(0x50 | AMD64.rbp.encoding);  // PUSH rbp
    emitMove(true, AMD64.rbp, AMD64.rsp);      // MOV rbp, rsp
    setDeoptRescueSlot(newStackSlot(AMD64Kind.QWORD));
  }

  @Override
  public void emitEpilogue() {
    recordMark(config.MARKID_DEOPT_HANDLER_ENTRY);
    recordCall(new HotSpotForeignCallTarget(config.handleDeoptStub), 5, true, null);
    code.emitByte(0xE8); // CALL rel32
    code.emitInt(0xDEADDEAD);
  }

  @Override
  public void emitGrowStack(int size) {
    // SUB rsp, size
    code.emitByte(0x48);
    code.emitByte(0x81);
    code.emitByte(0xEC);
    code.emitInt(size);
  }

  private Register emitLoadLong(Register ret, long c) {
    emitREX(true, 0, 0, ret.encoding);
    code.emitByte(0xB8 | (ret.encoding & 0x7)); // MOV r64, imm64
    code.emitLong(c);
    return ret;
  }

  @Override
  public Register emitLoadLong(long c) {
    Register ret = newRegister();
    return emitLoadLong(ret, c);
  }

  @Override
  public void emitCall(long addr) {
    Register target = emitLoadLong(addr);
    code.emitByte(0xFF); // CALL r/m64
    int enc = target.encoding;
    if (enc >= 8) {
      code.emitByte(0x41);
      enc -= 8;
    }
    code.emitByte(0xD0 | enc);
  }

  @Override
  public void emitIntRet(Register a) {
    emitMove(false, AMD64.rax, a);             // MOV eax, ...
    emitMove(true, AMD64.rsp, AMD64.rbp);      // MOV rsp, rbp
    code.emitByte(0x58 | AMD64.rbp.encoding);  // POP rbp
    code.emitByte(0xC3);                       // RET
  }

  private void emitREX(boolean w, int r, int x, int b) {
    int wrxb = (w ? 0x08 : 0) | ((r >> 3) << 2) | ((x >> 3) << 1) | (b >> 3);
    if (wrxb != 0) {
      code.emitByte(0x40 | wrxb);
    }
  }

  private void emitModRMReg(boolean w, int opcode, int r, int m) {
    emitREX(w, r, 0, m);
    code.emitByte((byte) opcode);
    code.emitByte((byte) 0xC0 | ((r & 0x7) << 3) | (m & 0x7));
  }

  private void emitMove(boolean w, Register to, Register from) {
    if (to != from) {
      emitModRMReg(w, 0x8B, to.encoding, from.encoding);
    }
  }
}
