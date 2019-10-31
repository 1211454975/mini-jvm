package com.gxk.jvm.rtda.heap;

import com.gxk.jvm.instruction.Instruction;
import com.gxk.jvm.util.Utils;
import java.util.List;
import lombok.Data;

import java.util.Map;

@Data
public class KMethod {

  public final int accessFlags;
  public final String name;
  public final String descriptor;

  public final int maxStacks;
  public final int maxLocals;
  public final Map<Integer, Instruction> instructionMap;

  public KClass clazz;

  public String getReturnType() {
    return this.descriptor.substring(this.descriptor.indexOf(")") + 1);
  }

  public List<String> getArgs() {
    return Utils.parseMethodDescriptor(this.descriptor);
  }

  @Override
  public String toString() {
    return "KMethod{" +
        "accessFlags=" + accessFlags +
        ", name='" + name + '\'' +
        ", descriptor='" + descriptor + '\'' +
        ", maxStacks=" + maxStacks +
        ", maxLocals=" + maxLocals +
        ", instructionMap=" + instructionMap +
        ", clazz=" + clazz.getName() +
        '}';
  }

  public boolean isNative() {
    return (this.accessFlags & 0x0100) != 0;
  }
}
