package com.gxk.jvm.instruction;

import com.gxk.jvm.rtda.Frame;
import com.gxk.jvm.rtda.heap.Heap;
import com.gxk.jvm.rtda.heap.KClass;
import com.gxk.jvm.rtda.heap.KMethod;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InvokestaticInst implements Instruction {

  public final String clazzName;
  public final String methodName;
  public final String descriptor;

  @Override
  public int offset() {
    return 3;
  }

  @Override
  public void execute(Frame frame) {
    KClass kClass = Heap.findClass(clazzName);
    KMethod method = kClass.getMethod(methodName, descriptor);
    Frame newFrame = new Frame(method, frame.thread);

    if (descriptor.startsWith("(I)")) {
      Integer tmp = frame.thread.currentFrame().operandStack.popInt();
      newFrame.localVars.setInt(0, tmp);
    }

    if (descriptor.startsWith("(II)")) {
      Integer o2 = frame.thread.currentFrame().operandStack.popInt();
      Integer o1 = frame.thread.currentFrame().operandStack.popInt();

      newFrame.localVars.setInt(0, o1);
      newFrame.localVars.setInt(1, o2);
    }

    if (method.name.equalsIgnoreCase("registerNatives")) {
      return;
    }

    frame.thread.pushFrame(newFrame);
  }
}

