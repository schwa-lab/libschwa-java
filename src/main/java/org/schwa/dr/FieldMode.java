package org.schwa.dr;


/**
 * A enum defining the types of modes that an {@link Ann} field can have.
 *
 * @author Tim Dawborn
 **/
public enum FieldMode {
  /**
   * {@link FieldMode#READ_WRITE} is the default value for all docrep fields. Fields of this type
   * do not have any laziness.
   **/
  READ_WRITE,
  /**
   * Fields which are set to be {@link FieldMode#READ_ONLY} will have their serialised form cached
   * with the annotation instance as well as being deserialised onto the object. The original
   * serialised form will be written back out when the instance is serialised.
   **/
  READ_ONLY,
  /**
   * {@link FieldMode#DELETE} states that this field will not be written out during serialisation.
   **/
  DELETE
}
