package com.gxk.jvm.instruction;

import com.gxk.jvm.rtda.Frame;

public class IfNonNullInst implements Instruction {
  public final int offset;

  public IfNonNullInst(int offset) {
    this.offset = offset;
  }

  @Override
  public int offset() {
    return 3;
  }

  @Override
  public void execute(Frame frame) {
    Object ref = frame.operandStack.popRef();
    if (ref != null) {
      frame.nextPc = frame.thread.getPc() + offset;
    }
  }
}
