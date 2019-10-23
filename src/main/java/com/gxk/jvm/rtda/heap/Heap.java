package com.gxk.jvm.rtda.heap;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * jvm heap
 */
@Data
public abstract class Heap {
  private static final Map<String, NativeMethod> NATIVE_METHOD_MAP;
  private static final Map<String, KClass> STRING_K_CLASS_MAP;

  static {
    NATIVE_METHOD_MAP = new HashMap<>();
    STRING_K_CLASS_MAP = new HashMap<>();
  }

  public static void registerMethod(String key, NativeMethod method) {
    if (NATIVE_METHOD_MAP.containsKey(key)) {
      throw new IllegalStateException("xxxxxxxxxxxxx");
    }
    NATIVE_METHOD_MAP.put(key, method);
  }

  public static NativeMethod findMethod(String key) {
    return NATIVE_METHOD_MAP.get(key);
  }

  public static KClass findClass(String name) {
    return STRING_K_CLASS_MAP.get(name);
  }

  public static void registerClass(String name, KClass clazz) {
    STRING_K_CLASS_MAP.putIfAbsent(name, clazz);
  }

  // for test
  public static void clear() {
    NATIVE_METHOD_MAP.clear();
    STRING_K_CLASS_MAP.clear();
  }
}
