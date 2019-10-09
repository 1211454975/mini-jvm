package com.gxk.jvm.instruction;

import com.gxk.jvm.rtda.Frame;

public class BiPushInst implements Instruction {

  public final byte val;

  public BiPushInst(byte val) {
    this.val = val;
  }

  @Override
  public int offset() {
    return 2;
  }

  @Override
  public void execute(Frame frame) {
    frame.operandStack.pushInt((int) (this.val));
  }
}
