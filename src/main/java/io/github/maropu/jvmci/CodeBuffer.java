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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * This code in this class is extracted from the JDK code base below;
 * http://hg.openjdk.java.net/jdk-updates/jdk9u/hotspot/file/bb73b31e70e3/test/compiler/jvmci/jdk.vm.ci.code.test/src/jdk/vm/ci/code/test/TestAssembler.java
 */
public class CodeBuffer {
  private ByteBuffer data = ByteBuffer.allocate(32).order(ByteOrder.nativeOrder());

  private void ensureSize(int length) {
    if (length >= data.limit()) {
      byte[] newBuf = Arrays.copyOf(data.array(), length * 4);
      ByteBuffer newData = ByteBuffer.wrap(newBuf);
      newData.order(data.order());
      newData.position(data.position());
      data = newData;
    }
  }

  public int position() {
    return data.position();
  }

  public void emitByte(int b) {
    ensureSize(data.position() + 1);
    data.put((byte) (b & 0xFF));
  }

  public void emitShort(int b) {
    ensureSize(data.position() + 2);
    data.putShort((short) b);
  }

  public void emitInt(int b) {
    ensureSize(data.position() + 4);
    data.putInt(b);
  }

  public void emitLong(long b) {
    ensureSize(data.position() + 8);
    data.putLong(b);
  }

  public void emitFloat(float f) {
    ensureSize(data.position() + 4);
    data.putFloat(f);
  }

  public void emitDouble(double f) {
    ensureSize(data.position() + 8);
    data.putDouble(f);
  }

  public void align(int alignment) {
    int pos = data.position();
    int misaligned = pos % alignment;
    if (misaligned != 0) {
      pos += alignment - misaligned;
      data.position(pos);
    }
  }

  public byte[] finish() {
    return Arrays.copyOf(data.array(), data.position());
  }
}
