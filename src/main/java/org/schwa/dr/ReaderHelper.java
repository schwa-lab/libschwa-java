package org.schwa.dr;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.msgpack.core.MessageUnpacker;

import org.schwa.dr.runtime.RTFieldSchema;
import org.schwa.dr.runtime.RTStoreSchema;


/**
 * Internal helper class for {@link Reader}.
 *
 * @author Tim Dawborn
 * @see Reader
 **/
final class ReaderHelper {
  private ReaderHelper() { }

  public static void read(final RTFieldSchema rtFieldSchema, final Ann ann, final Doc doc, final Store<? extends Ann> currentStore, final MessageUnpacker unpacker) throws IOException, IllegalAccessException {
    final FieldSchema fieldSchema = rtFieldSchema.getDef();
    final Field field = fieldSchema.getField();
    final RTStoreSchema rtStoreSchema = rtFieldSchema.getContainingStore();
    final StoreSchema storeSchema = (rtStoreSchema == null) ? null : rtStoreSchema.getDef();
    if (rtFieldSchema.isPointer() && rtStoreSchema.isLazy())
      throw new ReaderException("Pointer field '" + field + "' cannot point into a lazy store");

    if (rtFieldSchema.isPointer() || rtFieldSchema.isSelfPointer()) {
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
    else {
      final Class<?> klass = fieldSchema.getField().getType();
      if (klass == ByteSlice.class)
        readByteSlice(field, ann, unpacker);
      else
        readPrimitive(field, ann, klass, unpacker);
    }
  }


  private static void readByteSlice(final Field field, final Ann ann, final MessageUnpacker unpacker) throws IOException, IllegalAccessException {
    final int npair = unpacker.unpackArrayHeader();
    if (npair != 2)
      throw new ReaderException("Invalid sized list read in for SLICE: expected 2 elements but found " + npair);
    final long a = unpacker.unpackLong();
    final long b = unpacker.unpackLong();
    ByteSlice slice = (ByteSlice) field.get(ann);
    if (slice == null) {
      slice = new ByteSlice();
      field.set(ann, slice);
    }
    slice.start = a;
    slice.stop = a + b;
  }


  private static void readPointer(final Field field, final Ann ann, final Store<? extends Ann> store, final MessageUnpacker unpacker) throws IOException, IllegalAccessException {
    final int index = unpacker.unpackInt();
    field.set(ann, store.get(index));
  }


  private static void readPointerSlice(final Field field, final Ann ann, final Store<? extends Ann> store, final MessageUnpacker unpacker) throws IOException, IllegalAccessException {
    final int npair = unpacker.unpackArrayHeader();
    if (npair != 2)
      throw new ReaderException("Invalid sized list read in for SLICE: expected 2 elements but found " + npair);
    final int a = unpacker.unpackInt();
    final int b = unpacker.unpackInt();
    Slice slice = (Slice) field.get(ann);
    if (slice == null) {
      slice = new Slice();
      field.set(ann, slice);
    }
    slice.start = store.get(a);
    slice.stop = store.get(a + b - 1);  // Pointer slices in Java have to be [inclusive, inclusive].
  }


  private static void readPointers(final Field field, final Ann ann, final Store<? extends Ann> store, final MessageUnpacker unpacker) throws IOException, IllegalAccessException {
    final int nitems = unpacker.unpackArrayHeader();
    List<Ann> list = new ArrayList<Ann>(nitems);
    for (int i = 0; i != nitems; i++) {
      final int index = unpacker.unpackInt();
      list.add(store.get(index));
    }
    field.set(ann, list);
  }


  private static void readPrimitive(final Field field, final Ann ann, final Class<?> klass, final MessageUnpacker unpacker) throws IOException, IllegalAccessException {
    if (klass == String.class) {
      final String value = unpacker.unpackString();
      field.set(ann, value);
    }
    else if (klass == byte.class || klass == Byte.class) {
      final byte value = unpacker.unpackByte();
      field.set(ann, value);
    }
    else if (klass == char.class || klass == Character.class) {
      final char value = (char) unpacker.unpackInt();
      field.set(ann, value);
    }
    else if (klass == short.class || klass == Short.class) {
      final short value = unpacker.unpackShort();
      field.set(ann, value);
    }
    else if (klass == int.class || klass == Integer.class) {
      final int value = unpacker.unpackInt();
      field.set(ann, value);
    }
    else if (klass == long.class || klass == Long.class) {
      final long value = unpacker.unpackLong();
      field.set(ann, value);
    }
    else if (klass == float.class || klass == Float.class) {
      final float value = unpacker.unpackFloat();
      field.set(ann, value);
    }
    else if (klass == double.class || klass == Double.class) {
      final double value = unpacker.unpackDouble();
      field.set(ann, value);
    }
    else if (klass == boolean.class || klass == Boolean.class) {
      final boolean value = unpacker.unpackBoolean();
      field.set(ann, value);
    }
    else
      throw new ReaderException("Unknown type (" + klass + ") of field '" + field + "'");
  }
}
