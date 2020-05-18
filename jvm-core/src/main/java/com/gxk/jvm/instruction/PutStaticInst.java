package com.gxk.jvm.instruction;

import com.gxk.jvm.rtda.Frame;
import com.gxk.jvm.rtda.Slot;
import com.gxk.jvm.rtda.memory.KClass;
import com.gxk.jvm.rtda.memory.KField;
import com.gxk.jvm.rtda.memory.MethodArea;

public class PutStaticInst implements Instruction {

  public final String clazz;
  public final String fieldName;
  public final String fieldDescriptor;

  @Override
  public int offset() {
    return 3;
  }

  public PutStaticInst(String clazz, String fieldName, String fieldDescriptor) {
    this.clazz = clazz;
    this.fieldName = fieldName;
    this.fieldDescriptor = fieldDescriptor;
  }

  @Override
  public void execute(Frame frame) {
    KClass kClass = MethodArea.findClass(clazz);
    if (kClass == null) {
      kClass = frame.method.clazz.classLoader.loadClass(clazz);
    }
    KField field = kClass.getField(fieldName, fieldDescriptor);

    if (fieldDescriptor.equals("J")) {
      Slot low = frame.popSlot();
      Slot high = frame.popSlot();
      field.setVal(new Slot[]{high, low});
      return;
    }
    field.setVal(new Slot[]{frame.popSlot()});
  }

  @Override
  public String format() {
    return "putstatic " + clazz + " " + fieldName + " " + fieldDescriptor;
  }

  @Override
  public String toString() {
    return "PutStaticInst{" +
        "clazz='" + clazz + '\'' +
        ", fieldName='" + fieldName + '\'' +
        ", fieldDescriptor='" + fieldDescriptor + '\'' +
        '}';
  }
}
