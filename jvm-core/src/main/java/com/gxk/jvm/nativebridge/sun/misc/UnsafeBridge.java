package com.gxk.jvm.nativebridge.sun.misc;

import com.gxk.jvm.rtda.MetaSpace;
import com.gxk.jvm.rtda.heap.KField;
import com.gxk.jvm.rtda.heap.KObject;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public abstract class UnsafeBridge {

  private static Map<Long, byte[]> mem = new HashMap<>();
  private static Long next = 1L;

  public static void registerNatives0() {
    MetaSpace.registerMethod("sun/misc/Unsafe_registerNatives_()V", frame -> {
    });
    MetaSpace.registerMethod("sun/misc/Unsafe_getUnsafe_()Lsun/misc/Unsafe;", frame -> {
      frame.pushRef(null);
    });
    MetaSpace.registerMethod("sun/misc/Unsafe_objectFieldOffset_(Ljava/lang/reflect/Field;)J", frame -> {
      frame.popRef();
      frame.popRef();
      frame.pushLong(1L);
    });
    MetaSpace.registerMethod("sun/misc/Unsafe_getAndAddInt_(Ljava/lang/Object;JI)I", frame -> {
      Integer delta = frame.popInt();
      Long offset = frame.popLong();
      KObject obj = (KObject) frame.popRef();
      Object thisObj = frame.popRef();

      KField field = obj.getField("value", "I");
      int val = field.val.getInt();
      frame.pushInt(val - delta);
    });

    MetaSpace.registerMethod("sun/misc/Unsafe_allocateMemory_(J)J", frame -> {
      Long val = frame.popLong();
      frame.popRef();

      byte[] data = new byte[val.intValue()];
      mem.put(next, data);
      next += val.intValue();

      frame.pushLong(val);
    });

    MetaSpace.registerMethod("sun/misc/Unsafe_putLong_(JJ)V", frame -> {
      Long v2 = frame.popLong();
      Long v1 = frame.popLong();
      frame.popRef(); // this

      ByteBuffer buffer = ByteBuffer.allocate(8);
      buffer.putLong(0, v2);
      byte[] bytes = buffer.array();
      mem.put(v1, bytes);
    });

    MetaSpace.registerMethod("sun/misc/Unsafe_getByte_(J)B", frame -> {
      Long arg = frame.popLong();
      frame.popRef();

      byte[] bytes = mem.get(arg);
      byte b = bytes[0];
      frame.pushInt(((int) (b)));
    });
  }
}
