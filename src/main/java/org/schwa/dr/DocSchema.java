package org.schwa.dr;

import java.lang.IllegalArgumentException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Schema class for docrep annotation {@link Doc} implementations.
 *
 * @author Tim Dawborn
 **/
public final class DocSchema extends AnnSchema {
  public static final Class<?>[] ALLOWED_FIELD_KLASSES = {byte.class, char.class, short.class, int.class, long.class, float.class, double.class, boolean.class, String.class, ByteSlice.class};
  public static final Class<? extends Annotation>[] ANNOTATION_KLASSES = (Class<? extends Annotation>[]) new Class<?>[]{dr.Field.class, dr.Pointer.class, dr.SelfPointer.class, dr.Store.class};

  protected List<AnnSchema> annSchemas;
  protected List<StoreSchema> storeSchemas;

  private DocSchema(Class<? extends Ann> klass, String name) {
    super(klass, name, "__meta__");
    annSchemas = new ArrayList<AnnSchema>();
    storeSchemas = new ArrayList<StoreSchema>();
    traverseDocKlass();
  }

  /**
   * Adds an {@link AnnSchema} schema that this document class has annotation instances for.
   **/
  public void addSchema(final AnnSchema annSchema) {
    annSchemas.add(annSchema);
  }

  /**
   * Adds a new {@link StoreSchema} to the list of fields on this document class.
   **/
  public void addStore(final StoreSchema storeSchema) {
    storeSchemas.add(storeSchema);
  }

  /**
   * Returns the schema for the annotation on this document class whose {@link Class} matches the
   * provided class. This method returns null if no annotation class matches.
   **/
  public AnnSchema getSchema(final Class<? extends Ann> klass) {
    for (AnnSchema ann : annSchemas)
      if (ann.getKlass().equals(klass))
        return ann;
    return null;
  }

  /**
   * Returns a list of schema objects for all annotation types that this document contains.
   **/
  public List<AnnSchema> getSchemas() {
    return annSchemas;
  }

  /**
   * Returns the schema for the stores on this annotation class whose name matches the provided
   * name. This method returns null if no field matches.
   **/
  public StoreSchema getStore(final String name) {
    for (StoreSchema store : storeSchemas)
      if (store.getName().equals(name))
        return store;
    return null;
  }

  /**
   * Returns a list of schema objects for all of the stores on this annotation class.
   **/
  public List<StoreSchema> getStores() {
    return storeSchemas;
  }

  /**
   * Returns whether or not this document has a store with the provided name.
   * @see DocSchema#getStore
   **/
  public boolean hasStore(final String name) {
    return getStore(name) != null;
  }

  /**
   * Returns whether or not this document class has any stores.
   **/
  public boolean hasStores() {
    return !storeSchemas.isEmpty();
  }

  private void traverseDocKlass() {
    // Identify the Store instances annotated with dr.Store that are on the Doc class.
    for (Field field : klass.getFields()) {
      final Class<?> fieldKlass = field.getType();
      final dr.Store drStore = field.getAnnotation(dr.Store.class);
      if (fieldKlass != Store.class || drStore == null)
        continue;

      ensureNoOtherAnnotations(field, dr.Store.class);

      // Get the generic argument to the store.
      final Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
      assert types.length == 1;
      final Class<? extends Ann> storedKlass = (Class<? extends Ann>) types[0];

      // Ensure the generic argument is annotated with dr.Ann.
      final dr.Ann drAnn = storedKlass.getAnnotation(dr.Ann.class);
      if (drAnn == null)
        throw new IllegalAnnotationException("The stored class '" + storedKlass + "' in field '" + field + "' is not annotated with dr.Ann");

      // Create the StoreSchema object for this store.
      final StoreSchema storeSchema = StoreSchema.create(field, storedKlass, drStore);
      addStore(storeSchema);

      // Create the AnnSchema object for tyhe stored class.
      AnnSchema annSchema;  // FIXME this will create duplicates if the klass is already seen in another store.
      if (drAnn.serial().isEmpty())
        annSchema = AnnSchema.create(storedKlass, storedKlass.getSimpleName());
      else
        annSchema = AnnSchema.create(storedKlass, storedKlass.getSimpleName(), drAnn.serial());
      addSchema(annSchema);
    }

    // Discover all the dr.Field, dr.Pointer, and dr.SelfPointer instances on all of the classes.
    traverseAnnSchema(this);
    for (AnnSchema s : annSchemas)
      traverseAnnSchema(s);
  }

  private void traverseAnnSchema(final AnnSchema annSchema) {
    for (Field field : annSchema.getKlass().getFields()) {
      final Class<?> fieldKlass = field.getType();
      final dr.Field drField = field.getAnnotation(dr.Field.class);
      final dr.Pointer drPointer = field.getAnnotation(dr.Pointer.class);
      final dr.SelfPointer drSelfPointer = field.getAnnotation(dr.SelfPointer.class);

      if (drField != null) {
        ensureNoOtherAnnotations(field, dr.Field.class);
        validateFieldField(field, drField, annSchema);
      }
      else if (drPointer != null) {
        ensureNoOtherAnnotations(field, dr.Pointer.class);
        if (!hasStore(drPointer.store()))
          throw new IllegalAnnotationException("Store name '" + drPointer.store() + "' on field '" + field + "' is unknown");
        validatePointerField(field, drPointer, annSchema);
      }
      else if (drSelfPointer != null) {
        ensureNoOtherAnnotations(field, dr.SelfPointer.class);
        validateSelfPointerField(field, drSelfPointer, annSchema);
      }
    }
  }

  private static void ensureNoOtherAnnotations(final Field field, final Class<?> knownAnnotationKlass) {
    for (Class<? extends Annotation> klass : ANNOTATION_KLASSES)
      if (klass != knownAnnotationKlass && field.getAnnotation(klass) != null)
        throw new IllegalAnnotationException("Field '" + field + "' cannot have more than one docrep annotation.");
  }

  private static void validateFieldField(final Field field, final dr.Field drField, final AnnSchema annSchema) {
    // dr.Field can annotate:
    // * {byte,char,short,int,long,float,double,boolean,String}
    // * org.schwa.dr.ByteSlice
    final Class<?> fieldKlass = field.getType();
    Class<?> validKlass = null;
    for (Class<?> k : ALLOWED_FIELD_KLASSES) {
      if (k == fieldKlass) {
        validKlass = k;
        break;
      }
    }
    if (validKlass == null)
      throw new IllegalAnnotationException("Field '" + field + "' which is annotated with dr.Field is of an invalid type");

    FieldSchema fieldSchema;
    if (fieldKlass == ByteSlice.class)
      fieldSchema = FieldSchema.createByteSlice(field, drField);
    else
      fieldSchema = FieldSchema.createPrimitive(field, drField);
    annSchema.addField(fieldSchema);
  }

  private static void validatePointerField(final Field field, final dr.Pointer drPointer, final AnnSchema annSchema) {
    // dr.Pointer can annotate:
    // * org.schwa.dr.Slice
    // * T, for T extends org.schwa.dr.Ann
    // * java.util.List<T>, for T extends org.schwa.dr.Ann
    FieldSchema fieldSchema;
    final Class<?> fieldKlass = field.getType();
    if (fieldKlass == Slice.class) {
      final Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
      final Class<? extends Ann> pointedToKlass = (Class<? extends Ann>) types[0];
      fieldSchema = FieldSchema.createPointerSlice(field, drPointer, pointedToKlass);
    }
    else if (Ann.class.isAssignableFrom(fieldKlass)) {
      final Class<? extends Ann> pointedToKlass = (Class<? extends Ann>) fieldKlass;
      fieldSchema = FieldSchema.createPointer(field, drPointer, pointedToKlass);
    }
    else if (List.class.isAssignableFrom(fieldKlass)) {
      // Ensure the generic of the List is a Ann subclass.
      final Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
      final Class<?> listKlass = (Class<?>) types[0];
      if (!Ann.class.isAssignableFrom(listKlass))
        throw new IllegalAnnotationException("Field '" + field + "' cannot be annotated with dr.Pointer when the generic type T of List<T> is not an Ann subclass");
      final Class<? extends Ann> pointedToKlass = (Class<? extends Ann>) listKlass;
      fieldSchema = FieldSchema.createPointers(field, drPointer, pointedToKlass);
    }
    else
      throw new IllegalAnnotationException("Field '" + field + "' which is annotated with dr.Pointer is of an invalid type");
    annSchema.addField(fieldSchema);
  }

  private static void validateSelfPointerField(final Field field, final dr.SelfPointer drSelfPointer, final AnnSchema annSchema) {
    // dr.SelfPointer can annotate:
    // * org.schwa.dr.Slice
    // * T, for T extends org.schwa.dr.Ann
    // * java.util.List<T>, for T extends org.schwa.dr.Ann
    FieldSchema fieldSchema;
    final Class<?> fieldKlass = field.getType();
    if (fieldKlass == Slice.class) {
      final Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
      final Class<? extends Ann> pointedToKlass = (Class<? extends Ann>) types[0];
      fieldSchema = FieldSchema.createSelfPointerSlice(field, drSelfPointer, pointedToKlass);
    }
    else if (Ann.class.isAssignableFrom(fieldKlass)) {
      final Class<? extends Ann> pointedToKlass = (Class<? extends Ann>) fieldKlass;
      fieldSchema = FieldSchema.createSelfPointer(field, drSelfPointer, pointedToKlass);
    }
    else if (List.class.isAssignableFrom(fieldKlass)) {
      // Ensure the generic of the List is a Ann subclass.
      final Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
      final Class<?> listKlass = (Class<?>) types[0];
      if (!Ann.class.isAssignableFrom(listKlass))
        throw new IllegalAnnotationException("Field '" + field + "' cannot be annotated with dr.Pointer when the generic type T of List<T> is not an Ann subclass");
      final Class<? extends Ann> pointedToKlass = (Class<? extends Ann>) listKlass;
      fieldSchema = FieldSchema.createSelfPointers(field, drSelfPointer, pointedToKlass);
    }
    else
      throw new IllegalAnnotationException("Field '" + field + "' which is annotated with dr.Pointer is of an invalid type");
    annSchema.addField(fieldSchema);
  }

  public static <T extends Doc> DocSchema create(final Class<T> klass) {
    if (!klass.isAnnotationPresent(dr.Doc.class))
      throw new IllegalArgumentException("The provided class is not annotated with dr.Doc");
    return new DocSchema(klass, klass.getName());
  }
}
