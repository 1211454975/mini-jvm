package com.gxk.jvm.instruction;

import com.gxk.jvm.rtda.Frame;
import lombok.Data;

@Data
public class I2sInst implements Instruction{

  @Override
  public int offset() {
    return 1;
  }

  @Override
  public void execute(Frame frame) {
    // do nothings
  }
}