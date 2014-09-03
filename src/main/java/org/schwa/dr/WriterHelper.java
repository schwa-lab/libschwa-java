package org.schwa.dr;

import java.io.IOException;
import java.util.List;

import org.msgpack.core.MessagePacker;

import org.schwa.dr.runtime.RTFieldSchema;


/**
 * Internal helper class for {@link Writer}.
 *
 * @author Tim Dawborn
 * @see Writer
 **/
final class WriterHelper {
  private WriterHelper() { }

  public static boolean write(final MessagePacker packer, final RTFieldSchema rtFieldSchema, final Ann ann) throws IOException {
    final FieldSchema fieldSchema = rtFieldSchema.getDef();
    final int fieldId = rtFieldSchema.getFieldId();
    final Object value = fieldSchema.getFieldValue(ann);

    if (rtFieldSchema.isPointer() || rtFieldSchema.isSelfPointer()) {
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


  private static boolean writePrimitive(final MessagePacker packer, final int fieldId, final Object value, final Class<?> klass) throws IOException {
    if (klass == String.class)
      return writeString(packer, fieldId, (String) value);
    else if (klass == byte.class || klass == Byte.class) {
      final Byte v = (Byte) value;
      if (v != null) {
        packer.packInt(fieldId);
        packer.packByte(v.byteValue());
        return true;
      }
    }
    else if (klass == char.class || klass == Character.class) {
      final Character v = (Character) value;
      if (v != null) {
        packer.packInt(fieldId);
        packer.packInt(v.charValue());
        return true;
      }
    }
    else if (klass == short.class || klass == Short.class) {
      final Short v = (Short) value;
      if (v != null) {
        packer.packInt(fieldId);
        packer.packShort(v.shortValue());
        return true;
      }
    }
    else if (klass == int.class || klass == Integer.class) {
      final Integer v = (Integer) value;
      if (v != null) {
        packer.packInt(fieldId);
        packer.packInt(v.intValue());
        return true;
      }
    }
    else if (klass == long.class || klass == Long.class) {
      final Long v = (Long) value;
      if (v != null) {
        packer.packInt(fieldId);
        packer.packLong(v.longValue());
        return true;
      }
    }
    else if (klass == float.class || klass == Float.class) {
      final Float v = (Float) value;
      if (v != null) {
        packer.packInt(fieldId);
        packer.packFloat(v.floatValue());
        return true;
      }
    }
    else if (klass == double.class || klass == Double.class) {
      final Double v = (Double) value;
      if (v != null) {
        packer.packInt(fieldId);
        packer.packDouble(v.doubleValue());
        return true;
      }
    }
    else if (klass == boolean.class || klass == Boolean.class) {
      final Boolean v = (Boolean) value;
      if (v != null) {
        packer.packInt(fieldId);
        packer.packBoolean(v.booleanValue());
        return true;
      }
    }
    else
      throw new WriterException("Unknown type of field (" + klass + ")");
    return false;
  }


  private static boolean writeByteSlice(final MessagePacker packer, final int fieldId, final ByteSlice slice) throws IOException {
    if (slice == null)
      return false;
    packer.packInt(fieldId);
    packer.packArrayHeader(2);
    packer.packLong(slice.start);
    packer.packLong(slice.stop - slice.start);
    return true;
  }


  private static boolean writePointer(final MessagePacker packer, final int fieldId, final Ann ann) throws IOException {
    if (ann == null)
      return false;
    packer.packInt(fieldId);
    packer.packInt(ann.getDRIndex());
    return true;
  }


  private static boolean writePointerSlice(final MessagePacker packer, final int fieldId, final Slice<? extends Ann> slice) throws IOException {
    if (slice == null)
      return false;
    packer.packInt(fieldId);
    packer.packArrayHeader(2);
    packer.packInt(slice.start.getDRIndex());
    packer.packInt(slice.stop.getDRIndex() - slice.start.getDRIndex() + 1);  // Pointer slices in Java have to be [inclusive, inclusive].
    return true;
  }



  private static boolean writePointers(final MessagePacker packer, final int fieldId, final List<? extends Ann> annotations) throws IOException {
    if (annotations == null || annotations.isEmpty())
      return false;
    packer.packInt(fieldId);
    packer.packArrayHeader(annotations.size());
    for (Ann ann : annotations)
      packer.packInt(ann.getDRIndex());
    return true;
  }


  private static boolean writeString(final MessagePacker packer, final int fieldId, final String s) throws IOException {
    if (s == null || s.isEmpty())
      return false;
    packer.packInt(fieldId);
    packer.packString(s);
    return true;
  }
}
