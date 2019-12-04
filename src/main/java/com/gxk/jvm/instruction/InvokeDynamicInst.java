package com.gxk.jvm.instruction;

import com.gxk.jvm.classfile.attribute.BootstrapMethods;
import com.gxk.jvm.classfile.cp.MethodHandle;
import com.gxk.jvm.classfile.cp.MethodType;
import com.gxk.jvm.rtda.Frame;
import com.gxk.jvm.rtda.LocalVars;
import com.gxk.jvm.rtda.Slot;
import com.gxk.jvm.rtda.heap.Heap;
import com.gxk.jvm.rtda.heap.KClass;
import com.gxk.jvm.rtda.heap.KLambdaObject;
import com.gxk.jvm.rtda.heap.KMethod;
import com.gxk.jvm.util.Utils;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class InvokeDynamicInst implements Instruction {

  public final String methodName;
  public final String methodDescriptor;

  public final int bsIdx;

  @Override
  public int offset() {
    return 5;
  }

  @Override
  public void execute(Frame frame) {
    BootstrapMethods bootstrapMethods = frame.method.clazz.bootstrapMethods;
    if (bootstrapMethods == null) {
      throw new IllegalStateException();
    }

    BootstrapMethods.BootstrapMethod bootstrapMethod = bootstrapMethods.methods[bsIdx];
    Integer argsRef = bootstrapMethod.getArgsRefs()[1];
    MethodHandle info = (MethodHandle) frame.method.clazz.constantPool.infos[argsRef - 1];
    String bsTargetClass = Utils.getClassNameByMethodDefIdx(frame.method.clazz.constantPool, info.referenceIndex);
    String bsTargetMethod = Utils.getMethodNameByMethodDefIdx(frame.method.clazz.constantPool, info.referenceIndex);

    Integer descRef0 = bootstrapMethod.getArgsRefs()[0];
    MethodType methodType0= (MethodType) frame.method.clazz.constantPool.infos[descRef0 - 1];
    String bstMethodDesc0 = Utils.getString(frame.method.clazz.constantPool, methodType0.descriptorIndex);

    Integer descRef = bootstrapMethod.getArgsRefs()[2];
    MethodType methodType= (MethodType) frame.method.clazz.constantPool.infos[descRef - 1];
    String bstMethodDesc = Utils.getString(frame.method.clazz.constantPool, methodType.descriptorIndex);

    KClass clazz = Heap.findClass(bsTargetClass);
    KMethod method = clazz.getLambdaMethod(bsTargetMethod);
    int maxLocals = method.getMaxLocals();

    String lcname = frame.method.clazz.getName() + "$" + frame.method.getName() + "$" + bsTargetClass + "$" + bsTargetMethod;
    List<KMethod> lcMehods = new ArrayList<>();
    KMethod lm = new KMethod(method.getAccessFlags(), methodName, bstMethodDesc0, method.getMaxStacks(), maxLocals + 1, null);
    lcMehods.add(lm);

    String format = String.format("%s_%s_%s", lcname, lm.name, lm.descriptor);
    if (Heap.findMethod(format) == null) {
      Heap.registerMethod(format, (f) -> {
        KClass bsc= Heap.findClass(bsTargetClass);
        KMethod bsm = bsc.getLambdaMethod(bsTargetMethod);

        List<String> args = bsm.getArgs();
        int bsSize = Utils.parseMethodDescriptor(bstMethodDesc).size();

        List<Object> argObjs = new ArrayList<>();
        for (int i = bsSize - 1; i >= 0; i--) {
          String arg = args.get(i);
          switch (arg) {
            case "I":
            case "B":
            case "C":
            case "S":
            case "Z":
              argObjs.add(f.popInt());
              break;
            case "F":
              argObjs.add(f.popFloat());
              break;
            case "J":
              argObjs.add(f.popLong());
              break;
            case "D":
              argObjs.add(f.popDouble());
              break;
            default:
              argObjs.add(f.popRef());
              break;
          }
        }

        KLambdaObject ref = (KLambdaObject) f.popRef();
        Collections.reverse(argObjs);

        Frame newFrame = new Frame(bsm, f.thread);

        // FIXME 稍有不妥
        for (Object arg : ref.args) {
          argObjs.add(0, arg);
        }

        int slotIdx = bsm.isStatic() ? 0 : 1;
        int aoi = bsm.isStatic() ? 0 : 1;
        for (int i = 0; i < args.size(); i++) {
          String arg = args.get(i);
          switch (arg) {
            case "I":
            case "B":
            case "C":
            case "S":
            case "Z":
              newFrame.setInt(slotIdx, (Integer) argObjs.get(aoi));
              break;
            case "J":
              newFrame.setLong(slotIdx, (Long) argObjs.get(aoi));
              slotIdx++;
              break;
            case "F":
              newFrame.setFloat(slotIdx, (Float) argObjs.get(aoi));
              break;
            case "D":
              newFrame.setDouble(slotIdx, (Double) argObjs.get(aoi));
              slotIdx++;
              break;
            default:
              newFrame.setRef(slotIdx, argObjs.get(aoi));
              break;
          }
          slotIdx++;
          aoi++;
        }

        if (!bsm.isStatic()) {
          newFrame.setRef(0, argObjs.get(0));
        }

        f.thread.pushFrame(newFrame);
      });
    }

    KClass lcClazz = new KClass(lcname, "java/lang/Object", new ArrayList<>(), lcMehods, new ArrayList<>(), null, null, frame.method.clazz.classLoader);

    int realSize = method.getArgs().size();
    int bsSize = Utils.parseMethodDescriptor(bstMethodDesc).size();

    List<Object> args = new ArrayList<>(realSize - bsSize);
    while (realSize > bsSize) {
      String arg = method.getArgs().get(bsSize);
        switch (arg) {
          case "I":
          case "B":
          case "C":
          case "S":
          case "Z":
            args.add(frame.popInt());
            break;
          case "F":
            args.add(frame.popFloat());
            break;
          case "J":
            args.add(frame.popLong());
            break;
          case "D":
            args.add(frame.popDouble());
            break;
          default:
            args.add(frame.popRef());
            break;
        }
      bsSize++;
    }
    if (!lm.isStatic()) {
      // this
      args.add(frame.popRef());
    }

    KLambdaObject kObject = lcClazz.newLambdaObject(args);
    frame.pushRef(kObject);
  }
}

