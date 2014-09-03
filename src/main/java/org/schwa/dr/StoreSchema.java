package org.schwa.dr;

import java.lang.reflect.Field;


/**
 * Schema class for docrep stores.
 *
 * @author Tim Dawborn
 **/
public final class StoreSchema {
  private final Field field;
  private final String name;
  private final Class<? extends Ann> storedKlass;
  private final FieldMode mode;
  private String serial;

  private StoreSchema(Field field, Class<? extends Ann> storedKlass, String name, FieldMode mode, String serial) {
    this.field = field;
    this.name = name;
    this.storedKlass = storedKlass;
    this.mode = mode;
    serial = serial.trim();
    this.serial = serial.isEmpty() ? name : serial;
  }

  public Field getField() {
    return field;
  }

  public FieldMode getMode() {
    return mode;
  }

  public String getName() {
    return name;
  }

  public String getSerial() {
    return serial;
  }

  public Store<? extends Ann> getStore(final Doc doc) {
    try {
      return (Store<? extends Ann>) field.get(doc);
    }
    catch (IllegalAccessException e) {
      throw new DocrepException(e);
    }
  }

  public Class<? extends Ann> getStoredKlass() {
    return storedKlass;
  }

  public void resize(final int size, final Doc doc) {
    try {
      Store<Ann> store = (Store<Ann>) field.get(doc);
      for (int i = 0; i != size; i++) {
        Ann ann = (Ann) storedKlass.newInstance();
        store.add(ann);
      }
    }
    catch (IllegalAccessException e) {
      throw new DocrepException(e);
    }
    catch (InstantiationException e) {
      throw new DocrepException(e);
    }
  }

  public void setSerial(String serial) {
    this.serial = serial;
  }

  public int size(final Doc doc) {
    try {
      return ((Store<?>) field.get(doc)).size();
    }
    catch (IllegalAccessException e) {
      throw new DocrepException(e);
    }
  }

  public static StoreSchema create(Field field, Class<? extends Ann> storedKlass, dr.Store drStore) {
    return new StoreSchema(field, storedKlass, field.getName(), drStore.mode(), drStore.serial());
  }
}
