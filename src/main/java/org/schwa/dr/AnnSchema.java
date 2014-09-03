package org.schwa.dr;

import java.util.ArrayList;
import java.util.List;


/**
 * Schema class for docrep annotation {@link Ann} implementations.
 *
 * @author Tim Dawborn
 **/
public class AnnSchema {
  protected final Class<? extends Ann> klass;
  protected final String name;
  protected String serial;
  protected List<FieldSchema> fieldSchemas;

  protected AnnSchema(Class<? extends Ann> klass, String name, String serial) {
    this.klass = klass;
    this.name = name;
    this.serial = serial;
    this.fieldSchemas = new ArrayList<FieldSchema>();
  }

  /**
   * Adds a new {@link FieldSchema} to the list of fields on this annotation class.
   **/
  public void addField(FieldSchema fieldSchema) {
    fieldSchemas.add(fieldSchema);
  }

  /**
   * Returns the {@link Class} object for the underlying annotation class.
   **/
  public Class<? extends Ann> getKlass() {
    return klass;
  }

  /**
   * Returns the schema for the index'th field on this annotation class.
   **/
  public FieldSchema getField(int index) {
    return fieldSchemas.get(index);
  }

  /**
   * Returns the schema for the field on this annotation class whose name matches the provided
   * name. This method returns null if no field matches.
   **/
  public FieldSchema getField(String name) {
    for (FieldSchema field : fieldSchemas)
      if (field.getName().equals(name))
        return field;
    return null;
  }

  /**
   * Returns a list of schema objects for all of the fields on this annotation class.
   **/
  public List<FieldSchema> getFields() {
    return fieldSchemas;
  }

  /**
   * Returns the name of this annotation class.
   **/
  public String getName() {
    return name;
  }

  /**
   * Returns the serial name of this annotation class.
   **/
  public String getSerial() {
    return serial;
  }

  /**
   * Returns whether or not this annotation class has any fields.
   **/
  public boolean hasFields() {
    return !fieldSchemas.isEmpty();
  }

  /**
   * Sets the serial name of this annotation class.
   **/
  public void setSerial(String serial) {
    this.serial = serial;
  }

  public static AnnSchema create(Class<? extends Ann> klass, String name) {
    return new AnnSchema(klass, name, name);
  }

  public static AnnSchema create(Class<? extends Ann> klass, String name, String serial) {
    return new AnnSchema(klass, name, serial);
  }
}
