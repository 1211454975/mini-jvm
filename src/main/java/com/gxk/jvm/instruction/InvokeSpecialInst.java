package com.gxk.jvm.instruction;

import com.gxk.jvm.rtda.Frame;
import com.gxk.jvm.rtda.heap.Heap;
import com.gxk.jvm.rtda.heap.KClass;
import com.gxk.jvm.rtda.heap.KMethod;
import com.gxk.jvm.rtda.heap.NativeMethod;

import java.util.Objects;

public class InvokeSpecialInst implements Instruction {

  public final String clazz;
  public final String methodName;
  public final String methodDescriptor;

  public InvokeSpecialInst(String clazz, String methodName, String methodDescriptor) {
    this.clazz = clazz;
    this.methodName = methodName;
    this.methodDescriptor = methodDescriptor;
  }

  @Override
  public int offset() {
    return 3;
  }

  @Override
  public void execute(Frame frame) {
    NativeMethod nm = Heap.findMethod(String.format("%s_%s_%s", clazz, methodName, methodDescriptor));
    if (nm != null) {
      nm.invoke(frame);
      return;
    }

    KClass kClass = Heap.findClass(clazz);
    if (kClass == null) {
      throw new IllegalStateException();
    }

    KMethod method = kClass.getMethod(methodName, methodDescriptor);
    if (method == null) {
      throw new IllegalStateException();
    }

    Frame newFrame = new Frame(method, frame.thread);
    if (method.getDescriptor().startsWith("()")) {
      Object thisObj = frame.operandStack.popRef();
      newFrame.localVars.setRef(0, thisObj);
      frame.thread.pushFrame(newFrame);
      return;
    }

    if (method.getDescriptor().startsWith("(I)")) {
      Integer arg2 = frame.operandStack.popInt();
      Object thisObj = frame.operandStack.popRef();

      newFrame.localVars.setRef(0, thisObj);
      newFrame.localVars.setInt(1, arg2);
      frame.thread.pushFrame(newFrame);
      return;
    }
  }
}
