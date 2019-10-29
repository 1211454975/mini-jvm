package com.gxk.jvm.instruction;

import com.gxk.jvm.rtda.Frame;
import com.gxk.jvm.rtda.Slot;
import com.gxk.jvm.rtda.heap.Heap;
import com.gxk.jvm.rtda.heap.KClass;
import com.gxk.jvm.rtda.heap.KField;
import com.gxk.jvm.rtda.heap.KObject;

public class LdcInst implements Instruction {
  public final String descriptor;
  public final Object val;

  @Override
  public int offset() {
    return 2;
  }

  public LdcInst(String descriptor, Object val) {
    this.descriptor = descriptor;
    this.val = val;
  }

  @Override
  public void execute(Frame frame) {
    switch (descriptor) {
      case "I":
        frame.operandStack.pushInt(((Integer) val));
        break;
      case "F":
        frame.operandStack.pushFloat(((float) val));
      case "Ljava/lang/String":
        KClass klass = Heap.findClass("java/lang/String");
        if (klass == null) {
          klass = frame.method.clazz.getClassLoader().loadClass("java/lang/String");
        }
        if (!klass.isStaticInit()) {
          Frame newFrame = new Frame(klass.getMethod("<clinit>", "()V"), frame.thread);
          klass.setStaticInit(1);
          KClass finalKlass = klass;
          newFrame.setOnPop(() -> finalKlass.setStaticInit(2));
          frame.thread.pushFrame(newFrame);

          frame.nextPc = frame.thread.getPc();
          return;
        }
        KObject object = klass.newObject();
        KField field = object.getField("value", "[C");
        field.val = new Slot[]{new Slot(val)};
        frame.operandStack.pushRef(object);
        break;
      default:
        frame.operandStack.pushRef(val);
    }
  }
}
