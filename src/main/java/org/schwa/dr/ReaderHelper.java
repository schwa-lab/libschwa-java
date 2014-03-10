package org.schwa.dr;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.msgpack.unpacker.Unpacker;

import org.schwa.dr.runtime.RTFieldSchema;
import org.schwa.dr.runtime.RTStoreSchema;


final class ReaderHelper {
  private ReaderHelper() { }

  public static void read(final RTFieldSchema rtFieldSchema, final Ann ann, final Doc doc, final Store<? extends Ann> currentStore, final Unpacker unpacker) throws IOException, IllegalAccessException {
    final FieldSchema fieldSchema = rtFieldSchema.getDef();
    final Field field = fieldSchema.getField();
    final RTStoreSchema rtStoreSchema = rtFieldSchema.getContainingStore();
    final StoreSchema storeSchema = (rtStoreSchema == null) ? null : rtStoreSchema.getDef();
    if (rtFieldSchema.isPointer() && rtStoreSchema.isLazy())
      throw new ReaderException("Pointer field '" + field + "' cannot point into a lazy store");
    if (rtFieldSchema.isPointer()) {
      Store<? extends Ann> store;
      if (rtFieldSchema.isSelfPointer())
        store = currentStore;
      else
        store = rtStoreSchema.getDef().getStore(doc);

      if (rtFieldSchema.isSlice())
        readPointerSlice(field, ann, store, unpacker);
      else if (rtFieldSchema.isCollection())
        readPointers(field, ann, store, unpacker);
      else
        readPointer(field, ann, store, unpacker);
    }
    else
      readPrimitive(field, ann, unpacker);
  }


  private static void readByteSlice(final Field field, final Ann ann, final Unpacker unpacker) throws IOException, IllegalAccessException {
    final int npair = unpacker.readArrayBegin();
    if (npair != 2)
      throw new ReaderException("Invalid sized list read in for SLICE: expected 2 elements but found " + npair);
    final long a = unpacker.readLong();
    final long b = unpacker.readLong();
    ByteSlice slice = (ByteSlice) field.get(ann);
    if (slice == null) {
      slice = new ByteSlice();
      field.set(ann, slice);
    }
    slice.start = a;
    slice.stop = a + b;
    unpacker.readArrayEnd();
  }


  private static void readPointer(final Field field, final Ann ann, final Store<? extends Ann> store, final Unpacker unpacker) throws IOException, IllegalAccessException {
    final int index = unpacker.readInt();
    field.set(ann, store.get(index));
  }


  private static void readPointerSlice(final Field field, final Ann ann, final Store<? extends Ann> store, final Unpacker unpacker) throws IOException, IllegalAccessException {
    final int npair = unpacker.readArrayBegin();
    if (npair != 2)
      throw new ReaderException("Invalid sized list read in for SLICE: expected 2 elements but found " + npair);
    final int a = unpacker.readInt();
    final int b = unpacker.readInt();
    Slice slice = (Slice) field.get(ann);
    if (slice == null) {
      slice = new Slice();
      field.set(ann, slice);
    }
    slice.start = store.get(a);
    slice.stop = store.get(a + b);
    unpacker.readArrayEnd();
  }


  private static void readPointers(final Field field, final Ann ann, final Store<? extends Ann> store, final Unpacker unpacker) throws IOException, IllegalAccessException {
    final int nitems = unpacker.readArrayBegin();
    List<Ann> list = new ArrayList<Ann>(nitems);
    for (int i = 0; i != nitems; i++) {
      final int index = unpacker.readInt();
      list.add(store.get(index));
    }
    unpacker.readArrayEnd();
    field.set(ann, list);
  }


  private static void readPrimitive(final Field field, final Ann ann, final Unpacker unpacker) throws IOException, IllegalAccessException {
    final Class<?> type = field.getType();
    if (type.equals(String.class)) {
      final String value = unpacker.readString();
      field.set(ann, value);
    }
    else if (type.equals(byte.class) || type.equals(Byte.class)) {
      final byte value = unpacker.readByte();
      field.set(ann, value);
    }
    else if (type.equals(char.class) || type.equals(Character.class)) {
      final char value = (char) unpacker.readInt();
      field.set(ann, value);
    }
    else if (type.equals(short.class) || type.equals(Short.class)) {
      final short value = unpacker.readShort();
      field.set(ann, value);
    }
    else if (type.equals(int.class) || type.equals(Integer.class)) {
      final int value = unpacker.readInt();
      field.set(ann, value);
    }
    else if (type.equals(long.class) || type.equals(Long.class)) {
      final long value = unpacker.readLong();
      field.set(ann, value);
    }
    else if (type.equals(float.class) || type.equals(Float.class)) {
      final float value = unpacker.readFloat();
      field.set(ann, value);
    }
    else if (type.equals(double.class) || type.equals(Double.class)) {
      final double value = unpacker.readDouble();
      field.set(ann, value);
    }
    else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
      final boolean value = unpacker.readBoolean();
      field.set(ann, value);
    }
    else
      throw new ReaderException("Unknown type (" + type + ") of field '" + field + "'");
  }
}
