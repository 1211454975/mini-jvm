package com.gxk.jvm.rtda.heap;

import java.util.List;
import java.util.Objects;

public class KObject {
  public final List<KMethod> methods;
  public final List<KField> fields;
  public final KClass clazz;
  private KObject superObject;

  public KObject(List<KMethod> methods, List<KField> fields, KClass clazz) {
    this.methods = methods;
    this.fields = fields;
    this.clazz = clazz;
  }

  public KField getField(String fieldName, String fieldDescriptor) {
    // this object
    for (KField field : fields) {
      if (Objects.equals(field.name, fieldName) && Objects.equals(field.descriptor, fieldDescriptor)) {
        return field;
      }
    }

    if (this.superObject == null) {
      return null;
    }

    // super object
    return this.superObject.getField(fieldName, fieldDescriptor);
  }

  public KMethod getMethod(String name, String descriptor) {
    // this object
    for (KMethod method : methods) {
      if (Objects.equals(method.name, name) && Objects.equals(method.descriptor, descriptor)) {
        return method;
      }
    }

    if (this.superObject == null) {
      return null;
    }

    // super object
    return this.superObject.getMethod(name, descriptor);
  }

  public void setSuperObject(KObject superObject) {
    this.superObject = superObject;
  }
}
