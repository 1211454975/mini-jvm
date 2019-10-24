package com.gxk.jvm.instruction;

import com.gxk.jvm.rtda.Frame;

public class LnegInst implements Instruction {

  @Override
  public void execute(Frame frame) {
    Long tmp = frame.operandStack.popLong();
    frame.operandStack.pushLong(-tmp);
  }
}
