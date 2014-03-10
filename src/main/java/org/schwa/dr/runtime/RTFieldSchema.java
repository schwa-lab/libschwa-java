package org.schwa.dr.runtime;

import org.schwa.dr.FieldSchema;


public final class RTFieldSchema {
  private RTStoreSchema containingStore;
  private FieldSchema def;
  private String serial;
  private final int fieldId;
  private final boolean isCollection;
  private final boolean isSelfPointer;
  private final boolean isSlice;

  public RTFieldSchema(int fieldId, String serial, RTStoreSchema containingStore, boolean isCollection, boolean isSelfPointer, boolean isSlice) {
    this(fieldId, serial, containingStore, isCollection, isSelfPointer, isSlice, null);
  }

  public RTFieldSchema(int fieldId, String serial, RTStoreSchema containingStore, boolean isCollection, boolean isSelfPointer, boolean isSlice, FieldSchema def) {
    this.containingStore = containingStore;
    this.def = def;
    this.serial = serial;
    this.fieldId = fieldId;
    this.isCollection = isCollection;
    this.isSelfPointer = isSelfPointer;
    this.isSlice = isSlice;
  }

  public RTStoreSchema getContainingStore() {
    return containingStore;
  }

  public FieldSchema getDef() {
    return def;
  }

  public int getFieldId() {
    return fieldId;
  }

  public String getSerial() {
    return serial;
  }

  public boolean isCollection() {
    return isCollection;
  }

  public boolean isLazy() {
    return def == null;
  }

  public boolean isPointer() {
    return containingStore != null;
  }

  public boolean isSelfPointer() {
    return isSelfPointer;
  }

  public boolean isSlice() {
    return isSlice;
  }

  public void setContainingStore(RTStoreSchema containingStore) {
    this.containingStore = containingStore;
  }

  public void setDef(FieldSchema def) {
    this.def = def;
  }
}
