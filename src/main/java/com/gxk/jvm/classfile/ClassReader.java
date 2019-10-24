package com.gxk.jvm.classfile;

import com.gxk.jvm.classfile.attribute.*;
import com.gxk.jvm.classfile.cp.*;
import com.gxk.jvm.instruction.Instruction;
import com.gxk.jvm.util.Utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.gxk.jvm.classfile.ConstantPoolInfoEnum.CONSTANT_Double;
import static com.gxk.jvm.classfile.ConstantPoolInfoEnum.CONSTANT_Long;

public abstract class ClassReader {

  public static ClassFile read(Path path) throws IOException {

    try (InputStream is = Files.newInputStream(path);
         BufferedInputStream bis = new BufferedInputStream(is);
         DataInputStream stream = new DataInputStream(bis)) {
      return read(stream);
    }
  }

  public static ClassFile read(DataInputStream is) throws IOException {
    int magic = is.readInt();
    int minorVersion = is.readUnsignedShort();
    int majorVersion = is.readUnsignedShort();

    int cpSize = is.readUnsignedShort();
    ConstantPool constantPool = readConstantPool(is, cpSize - 1);

    int accessFlag = is.readUnsignedShort();
    int thisClass = is.readUnsignedShort();
    int superClass = is.readUnsignedShort();

    int interfaceCount = is.readUnsignedShort();
    Interfaces interfaces = readInterfaces(is, interfaceCount);

    int fieldCount = is.readUnsignedShort();
    Fields fields = readFields(is, fieldCount, constantPool);

    int methodCount = is.readUnsignedShort();
    Methods methods = readMethods(is, methodCount, constantPool);

    int attributeCount = is.readUnsignedShort();
    Attributes attributes = readAttributes(is, attributeCount, constantPool);

    return new ClassFile(
      magic,
      minorVersion,
      majorVersion,
      cpSize,
      constantPool,
      accessFlag,
      thisClass,
      superClass,
      interfaceCount,
      interfaces,
      fieldCount,
      fields,
      methodCount,
      methods,
      attributeCount,
      attributes
    );
  }

  private static Fields readFields(DataInputStream is, int fieldCount, ConstantPool constantPool) throws IOException {
    Field[] fields = new Field[fieldCount];
    for (int i = 0; i < fieldCount; i++) {
      int accessFlag = is.readUnsignedShort();
      int nameIndex = is.readUnsignedShort();
      int descriptorIndex = is.readUnsignedShort();
      int attributesCount = is.readUnsignedShort();

      Attributes attributes = readAttributes(is, attributesCount, constantPool);

      ConstantInfo info = constantPool.getInfos()[nameIndex - 1];
      String name = ((Utf8) info).getString();

      String descriptor = ((Utf8) constantPool.infos[descriptorIndex - 1]).getString();

      Field field = new Field(accessFlag, name, new Descriptor(descriptor), attributes);
      fields[i] = field;
    }
    return new Fields(fields);
  }

  private static Interfaces readInterfaces(DataInputStream is, int interfaceCount) throws IOException {
    for (int i = 0; i < interfaceCount; i++) {
      int xx = is.readUnsignedShort();
      System.out.println("interface: xxx" + xx);
    }
    return null;
  }

  //method_info {
//    u2             access_flags;
//    u2             name_index;
//    u2             descriptor_index;
//    u2             attributes_count;
//    attribute_info attributes[attributes_count];
//    }
  private static Methods readMethods(DataInputStream is, int methodCount,
                                     ConstantPool constantPool) throws IOException {
    Methods methods = new Methods(methodCount);

    for (int i = 0; i < methodCount; i++) {
      int accessFlag = is.readUnsignedShort();
      int nameIndex = is.readUnsignedShort();
      int descriptorIndex = is.readUnsignedShort();
      int attributesCount = is.readUnsignedShort();

      Attributes attributes = readAttributes(is, attributesCount, constantPool);

      ConstantInfo info = constantPool.getInfos()[nameIndex - 1];
      String name = ((Utf8) info).getString();

      String descriptor = ((Utf8) constantPool.infos[descriptorIndex - 1]).getString();

      methods.methods[i] = new Method(accessFlag, name, new Descriptor(descriptor), attributes);
    }
    return methods;
  }

  private static ConstantPool readConstantPool(DataInputStream is, int cpSize) throws IOException {
    ConstantPool constantPool = new ConstantPool(cpSize);
    for (int i = 0; i < cpSize; i++) {
      int tag = is.readUnsignedByte();

      ConstantPoolInfoEnum infoEnum = ConstantPoolInfoEnum.of(tag);
      if (infoEnum == null) {
        throw new IllegalStateException("cccccc");
      }

      ConstantInfo info = null;
      switch (infoEnum) {
        case CONSTANT_Fieldref:
          info = new FieldDef(infoEnum, is.readUnsignedShort(), is.readUnsignedShort());
          break;
        case CONSTANT_Methodref:
          info = new MethodDef(infoEnum, is.readUnsignedShort(), is.readUnsignedShort());
          break;
        case CONSTANT_InterfaceMethodref:
          info = new InterfaceMethodDef(infoEnum, is.readUnsignedShort(), is.readUnsignedShort());
          break;
        case CONSTANT_String:
          info = new StringCp(infoEnum, is.readUnsignedShort());
          break;
        case CONSTANT_Class:
          info = new ClassCp(infoEnum, is.readUnsignedShort());
          break;
        case CONSTANT_NameAndType:
          info = new NameAndType(infoEnum, is.readUnsignedShort(), is.readUnsignedShort());
          break;
        case CONSTANT_Utf8:
          int length = is.readUnsignedShort();
          byte[] bytes = Utils.readNBytes(is, length);
          info = new Utf8(infoEnum, bytes);
          break;
        case CONSTANT_MethodHandle:
          break;
        case CONSTANT_MethodType:
          break;
        case CONSTANT_InvokeDynamic:
          break;
        case CONSTANT_Integer:
          info = new IntegerCp(infoEnum, is.readInt());
          break;
        case CONSTANT_Long:
          info = new LongCp(infoEnum, is.readLong());
          break;
        case CONSTANT_Float:
          info = new FloatCp(infoEnum, is.readFloat());
          break;
        case CONSTANT_Double:
          info = new DoubleCp(infoEnum, is.readDouble());
          break;
      }
      if (info == null) {
        throw new UnsupportedOperationException("un parse cp " + infoEnum);
      }

      constantPool.infos[i] = info;
      if (info.infoEnum.equals(CONSTANT_Long) || info.infoEnum.equals(CONSTANT_Double)) {
        i++;
      }
    }
    return constantPool;
  }

  public static String byteArrayToHex(byte[] a) {
    StringBuilder sb = new StringBuilder(a.length * 2);
    for (byte b : a) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  //  attribute_info {
//    u2 attribute_name_index;
//    u4 attribute_length;
//    u1 info[attribute_length];
//  }
  private static Attributes readAttributes(DataInputStream is, int attributeCount, ConstantPool constantPool)
    throws IOException {
    Attributes attributes = new Attributes(attributeCount);

    for (int i = 0; i < attributeCount; i++) {
      Attribute attribute = null;
      int attributeNameIndex = is.readUnsignedShort();
      String attributeName = Utils.getString(constantPool, attributeNameIndex);

      AttributeEnum attributeEnum = AttributeEnum.of(attributeName);
      if (attributeEnum == null) {
        throw new IllegalStateException(attributeName);
      }
      int attributeLength = is.readInt();
      switch (attributeEnum) {
        case SourceFile:
          int sourceFileIndex = is.readUnsignedShort();
          String file = Utils.getString(constantPool, sourceFileIndex);
          attribute = new SourceFile(file);
          break;
        case Code:
          int maxStack = is.readUnsignedShort();
          int maxLocals = is.readUnsignedShort();

          int codeLength = is.readInt();
          byte[] byteCode = Utils.readNBytes(is, codeLength);

          Instruction[] instructions = readByteCode(byteCode, constantPool);

          int exceptionTableLength = is.readUnsignedShort();
          if (exceptionTableLength > 0) {
            // 定长 8
            byte[] bytes1 = Utils.readNBytes(is, exceptionTableLength * 8);
            System.out.println("byteArrayToHex(bytes) = " + byteArrayToHex(bytes1));
          }

          int codeAttributeCount = is.readUnsignedShort();
          Attributes codeAttributes = readAttributes(is, codeAttributeCount, constantPool);

          attribute = new Code(maxStack, maxLocals, instructions, null, codeAttributes);
          break;
        case LineNumberTable:
          int length = is.readUnsignedShort();
          LineNumberTable.Line[] lines = new LineNumberTable.Line[length];
          for (int i1 = 0; i1 < length; i1++) {
            lines[i1] = new LineNumberTable.Line(is.readUnsignedShort(), is.readUnsignedShort());
          }
          attribute = new LineNumberTable(lines);
          break;
        default:
          byte[] bytes = Utils.readNBytes(is, attributeLength);
//          System.out.println("bytes = " + byteArrayToHex(bytes));
      }

      attributes.attributes[i] = attribute;
    }

    return attributes;
  }

  public static Instruction[] readByteCode(byte[] byteCode, ConstantPool constantPool) throws IOException {
    List<Instruction> instructions = new ArrayList<>();
    try (MyByteArrayInputStream in = new MyByteArrayInputStream(byteCode);) {
      while (in.available() > 0) {
        int opCode = in.read();
        try {
          Instruction inst = InstructionReader.read(opCode, in, constantPool);
          if (inst == null) {
            System.out.println(Integer.toHexString(opCode));
            break;
          }
          instructions.add(inst);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    Instruction[] ret = new Instruction[instructions.size()];
    instructions.toArray(ret);
    return ret;
  }
}
