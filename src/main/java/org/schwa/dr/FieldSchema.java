package org.schwa.dr;

import java.lang.reflect.Field;


public final class FieldSchema {
  private final Field field;
  private final String name;
  private final Class<? extends Ann> pointedToKlass;
  private final String storeName;
  private final FieldMode mode;

  private final boolean isPointer;
  private final boolean isSelfPointer;
  private final boolean isSlice;
  private final boolean isCollection;

  private String serial;

  private FieldSchema(Field field, String name, String serial, Class<? extends Ann> pointedToKlass, String storeName, FieldMode mode, boolean isPointer, boolean isSelfPointer, boolean isSlice, boolean isCollection) {
    this.field = field;
    this.name = name;
    this.pointedToKlass = pointedToKlass;
    this.storeName = storeName;
    this.mode = mode;

    this.isPointer = isPointer;
    this.isSelfPointer = isSelfPointer;
    this.isSlice = isSlice;
    this.isCollection = isCollection;

    serial = serial.trim();
    this.serial = serial.isEmpty() ? name : serial;
  }

  public Field getField() {
    return field;
  }

  public Object getFieldValue(Ann ann) {
    try {
      return field.get(ann);
    }
    catch (IllegalAccessException e) {
      throw new DocrepException(e);
    }
  }

  public String getName() {
    return name;
  }

  public FieldMode getMode() {
    return mode;
  }

  public Class<? extends Ann> getPointedToKlass() {
    return pointedToKlass;
  }

  public String getSerial() {
    return serial;
  }

  public String getStoreName() {
    return storeName;
  }

  public boolean isCollection() {
    return isCollection;
  }

  public boolean isPointer() {
    return isPointer;
  }

  public boolean isSelfPointer() {
    return isSelfPointer;
  }

  public boolean isSlice() {
    return isSlice;
  }

  public void setSerial(String serial) {
    this.serial = serial;
  }

  public static FieldSchema createByteSlice(Field field, dr.Field drField) {
    return new FieldSchema(field, field.getName(), drField.serial(), null, null, drField.mode(), false, false, true, false);
  }

  public static FieldSchema createPointer(Field field, dr.Pointer drPointer, Class<? extends Ann> pointedToKlass) {
    return new FieldSchema(field, field.getName(), drPointer.serial(), pointedToKlass, drPointer.store(), drPointer.mode(), true, false, false, false);
  }

  public static FieldSchema createPointerSlice(Field field, dr.Pointer drPointer, Class<? extends Ann> pointedToKlass) {
    return new FieldSchema(field, field.getName(), drPointer.serial(), pointedToKlass, drPointer.store(), drPointer.mode(), true, false, true, false);
  }

  public static FieldSchema createPointers(Field field, dr.Pointer drPointer, Class<? extends Ann> pointedToKlass) {
    return new FieldSchema(field, field.getName(), drPointer.serial(), pointedToKlass, drPointer.store(), drPointer.mode(), true, false, false, true);
  }

  public static FieldSchema createPrimitive(Field field, dr.Field drField) {
    return new FieldSchema(field, field.getName(), drField.serial(), null, null, drField.mode(), false, false, false, false);
  }

  public static FieldSchema createSelfPointer(Field field, dr.SelfPointer drSelfPointer, Class<? extends Ann> pointedToKlass) {
    return new FieldSchema(field, field.getName(), drSelfPointer.serial(), pointedToKlass, "", drSelfPointer.mode(), false, true, false, false);
  }

  public static FieldSchema createSelfPointerSlice(Field field, dr.SelfPointer drSelfPointer, Class<? extends Ann> pointedToKlass) {
    return new FieldSchema(field, field.getName(), drSelfPointer.serial(), pointedToKlass, "", drSelfPointer.mode(), false, true, true, false);
  }

  public static FieldSchema createSelfPointers(Field field, dr.SelfPointer drSelfPointer, Class<? extends Ann> pointedToKlass) {
    return new FieldSchema(field, field.getName(), drSelfPointer.serial(), pointedToKlass, "", drSelfPointer.mode(), false, true, false, true);
  }
}
