package org.schwa.dr;

import java.io.IOException;
import java.util.List;

import org.msgpack.packer.Packer;

import org.schwa.dr.runtime.RTFieldSchema;


final class WriterHelper {
  private WriterHelper() { }

  public static boolean write(final Packer packer, final RTFieldSchema rtFieldSchema, final Ann ann) throws IOException {
    final FieldSchema fieldSchema = rtFieldSchema.getDef();
    final int fieldId = rtFieldSchema.getFieldId();
    final Object value = fieldSchema.getFieldValue(ann);

    if (rtFieldSchema.isPointer()) {
      if (rtFieldSchema.isSlice())
        return writePointerSlice(packer, fieldId, (Slice<? extends Ann>) value);
      else if (rtFieldSchema.isCollection())
        return writePointers(packer, fieldId, (List<? extends Ann>) value);
      else
        return writePointer(packer, fieldId, (Ann) value);
    }
    else {
      final Class<?> klass = fieldSchema.getField().getType();
      if (klass == ByteSlice.class)
        return writeByteSlice(packer, fieldId, (ByteSlice) value);
      else
        return writePrimitive(packer, fieldId, value, klass);
    }
  }


  private static boolean writePrimitive(final Packer p, final int fieldId, final Object value, final Class<?> klass) throws IOException {
    if (klass == String.class)
      return writeString(p, fieldId, (String) value);
    else if (klass == byte.class || klass == Byte.class) {
      final Byte v = (Byte) value;
      if (v != null) {
        p.write(fieldId);
        p.write(v.byteValue());
        return true;
      }
    }
    else if (klass == char.class || klass == Character.class) {
      final Character v = (Character) value;
      if (v != null) {
        p.write(fieldId);
        p.write(v.charValue());
        return true;
      }
    }
    else if (klass == short.class || klass == Short.class) {
      final Short v = (Short) value;
      if (v != null) {
        p.write(fieldId);
        p.write(v.shortValue());
        return true;
      }
    }
    else if (klass == int.class || klass == Integer.class) {
      final Integer v = (Integer) value;
      if (v != null) {
        p.write(fieldId);
        p.write(v.intValue());
        return true;
      }
    }
    else if (klass == long.class || klass == Long.class) {
      final Long v = (Long) value;
      if (v != null) {
        p.write(fieldId);
        p.write(v.longValue());
        return true;
      }
    }
    else if (klass == float.class || klass == Float.class) {
      final Float v = (Float) value;
      if (v != null) {
        p.write(fieldId);
        p.write(v.floatValue());
        return true;
      }
    }
    else if (klass == double.class || klass == Double.class) {
      final Double v = (Double) value;
      if (v != null) {
        p.write(fieldId);
        p.write(v.doubleValue());
        return true;
      }
    }
    else if (klass == boolean.class || klass == Boolean.class) {
      final Boolean v = (Boolean) value;
      if (v != null) {
        p.write(fieldId);
        p.write(v.booleanValue());
        return true;
      }
    }
    else
      throw new WriterException("Unknown type of field (" + klass + ")");
    return false;
  }


  private static boolean writeByteSlice(final Packer p, final int fieldId, final ByteSlice slice) throws IOException {
    if (slice == null)
      return false;
    p.write(fieldId);
    p.writeArrayBegin(2);
    p.write(slice.start);
    p.write(slice.stop - slice.start);
    p.writeArrayEnd();
    return true;
  }


  private static boolean writePointer(final Packer p, final int fieldId, final Ann ann) throws IOException {
    if (ann == null)
      return false;
    p.write(fieldId);
    p.write(ann.getDRIndex());
    return true;
  }


  private static boolean writePointerSlice(final Packer p, final int fieldId, final Slice<? extends Ann> slice) throws IOException {
    if (slice == null)
      return false;
    p.write(fieldId);
    p.writeArrayBegin(2);
    p.write(slice.start.getDRIndex());
    p.write(slice.stop.getDRIndex() - slice.start.getDRIndex() + 1);  // Pointer slices in Java have to be [inclusive, inclusive].
    p.writeArrayEnd();
    return true;
  }



  private static boolean writePointers(final Packer p, final int fieldId, final List<? extends Ann> annotations) throws IOException {
    if (annotations == null || annotations.isEmpty())
      return false;
    p.write(fieldId);
    p.writeArrayBegin(annotations.size());
    for (Ann ann : annotations)
      p.write(ann.getDRIndex());
    p.writeArrayEnd();
    return true;
  }


  private static boolean writeString(final Packer p, final int fieldId, final String s) throws IOException {
    if (s == null || s.isEmpty())
      return false;
    p.write(fieldId);
    p.write(s);
    return true;
  }
}
