package org.schwa.dr;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Unpacker;

import org.schwa.dr.runtime.RTAnnSchema;
import org.schwa.dr.runtime.RTFactory;
import org.schwa.dr.runtime.RTFieldSchema;
import org.schwa.dr.runtime.RTManager;
import org.schwa.dr.runtime.RTStoreSchema;


public final class Reader <T extends Doc> implements Iterable<T>, Iterator<T> {
  public static final byte WIRE_VERSION = 2;

  private final InputStream in;
  private final DocSchema docSchema;
  private final MessagePack msgpack;
  private final Unpacker unpacker;
  private T doc;

  public Reader(InputStream in, DocSchema docSchema) {
    this.in = in;
    this.docSchema = docSchema;
    this.msgpack = new MessagePack();
    this.unpacker = msgpack.createUnpacker(in);
    readNext();
  }

  @Override  // Iterable<T>
  public Iterator<T> iterator() {
    return this;
  }

  @Override  // Iterator<T>
  public boolean hasNext() {
    return doc != null;
  }

  @Override  // Iterator<T>
  public T next() {
    final T doc = this.doc;
    readNext();
    return doc;
  }

  @Override  // Iterator<T>
  public void remove() {
    throw new UnsupportedOperationException();
  }

  private void readNext() {
    try {
      _readNext();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    catch (InstantiationException e) {
      throw new RuntimeException(e);
    }
    catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private void _readNext() throws IOException, InstantiationException, IllegalAccessException {
    // <doc>  ::= <wire_version> <klasses> <stores> <doc_instance> <instances_groups>

    // Read the wire format version before, and ensure we know how to read that version.
    // <wire_version> ::= UINT
    byte wireVersion;
    try {
      wireVersion = unpacker.readByte();
    }
    catch (EOFException e) {
      doc = null;
      return;
    }
    if (wireVersion != WIRE_VERSION)
      throw new ReaderException("Invalid wire format version. Stream has version " + wireVersion + " but I can only read version " + WIRE_VERSION + ". Ensure the input is not plain text.");

    // Construct the lazy runtime manager for the document.
    final RTManager rt = RTFactory.create();
    doc = (T) docSchema.getKlass().newInstance();
    doc.setRT(rt);

    // Map of each of the registered types.
    Map<String, AnnSchema> klassNameMap = new HashMap<String, AnnSchema>();
    klassNameMap.put("__meta__", docSchema);
    for (AnnSchema ann : docSchema.getSchemas())
      klassNameMap.put(ann.getSerial(), ann);

    // Keep track of the temporary mapping of pointer field to the store_id that they point to.
    Map<RTFieldSchema, Integer> rtFieldSchemaToStoreIds = new HashMap<RTFieldSchema, Integer>();

    // Keep track of the klass_id of __meta__.
    Integer klassIdMeta = null;

    // Read the klasses header.
    // <klasses> ::= [ <klass> ]
    final int nklasses = unpacker.readArrayBegin();
    for (int k = 0; k != nklasses; k++) {
      // <klass> ::= ( <klass_name>, <fields> )
      final int npair = unpacker.readArrayBegin();
      if (npair != 2)
        throw new ReaderException("Invalid sized tuple read in: expected 2 elements but found " + npair);

      // Read in the class name and check that we have a registered class with this name.
      RTAnnSchema rtAnnSchema;
      final String klassName = unpacker.readString();
      final AnnSchema schema = klassNameMap.get(klassName);
      if (schema == null)
        rtAnnSchema = new RTAnnSchema(k, klassName);
      else
        rtAnnSchema = new RTAnnSchema(k, klassName, schema);
      rt.addAnn(rtAnnSchema);

      // Keep track of the klass_id of __meta__.
      if (klassName.equals("__meta__"))
        klassIdMeta = k;

      // <fields> ::= [ <field> ]
      final int nfields = unpacker.readArrayBegin();
      for (int f = 0; f != nfields; f++) {
        String fieldName = null;
        int storeId = 0;
        boolean isPointer = false, isSelfPointer = false, isSlice = false, isCollection = false;

        // <field> ::= { <field_type> : <field_val> }
        final int nitems = unpacker.readMapBegin();
        for (int i = 0; i != nitems; ++i) {
          final byte key = unpacker.readByte();
          switch (key) {
          case 0:  // NAME
            fieldName = unpacker.readString();
            break;
          case 1:  // POINTER_TO
            storeId = unpacker.readInt() + 1;
            isPointer = true;
            break;
          case 2:  // IS_SLICE
            unpacker.readNil();
            isSlice = true;
            break;
          case 3:  // IS_SELF_POINTER
            unpacker.readNil();
            isSelfPointer = true;
            break;
          case 4:  // IS_COLLECTION
            unpacker.readNil();
            isCollection = true;
          default:
            throw new ReaderException("Unknown value " + ((int) key) +  " as key in <field> map");
          }
        }
        unpacker.readMapEnd();
        if (fieldName == null)
          throw new ReaderException("Field number " + (f + 1) + " did not contain a NAME key");

        // See if the read in field exists on the registered class's schema.
        RTFieldSchema rtFieldSchema;
        if (rtAnnSchema.isLazy())
          rtFieldSchema = new RTFieldSchema(f, fieldName, null, isSlice);
        else {
          // Try and find the field on the registered class.
          FieldSchema fieldDef = null;
          for (FieldSchema field : rtAnnSchema.getDef().getFields()) {
            if (field.getSerial().equals(fieldName)) {
              fieldDef = field;
              break;
            }
          }
          rtFieldSchema = new RTFieldSchema(f, fieldName, null, isSlice, fieldDef);

          // Perform some sanity checks that the type of data on the stream is what we're expecting.
          if (fieldDef != null) {
            if (isPointer != fieldDef.isPointer())
              throw new ReaderException("Field '" + fieldName + "' of class '" + klassName + "' has IS_POINTER as " + isPointer + " on the stream, but " + fieldDef.isPointer() + " on the class's field");
            if (isSlice != fieldDef.isSlice())
              throw new ReaderException("Field '" + fieldName + "' of class '" + klassName + "' has IS_SLICE as " + isSlice + " on the stream, but " + fieldDef.isSlice() + " on the class's field");
            if (isSelfPointer != fieldDef.isSelfPointer())
              throw new ReaderException("Field '" + fieldName + "' of class '" + klassName + "' has IS_SELF_POINTER as " + isSelfPointer + " on the stream, but " + fieldDef.isSelfPointer() + " on the class's field");
            if (isCollection != fieldDef.isCollection())
              throw new ReaderException("Field '" + fieldName + "' of class '" + klassName + "' has IS_COLLECTION as " + isCollection + " on the stream, but " + fieldDef.isCollection() + " on the class's field");
          }
        }

        rtAnnSchema.addField(rtFieldSchema);
        if (isPointer)
          rtFieldSchemaToStoreIds.put(rtFieldSchema, storeId);
      } // for each field
      unpacker.readArrayEnd();
      unpacker.readArrayEnd();
    } // for each klass
    unpacker.readArrayEnd();

    if (klassIdMeta == null)
      throw new ReaderException("Did not read in a __meta__ class");
    final RTAnnSchema rtDocSchema = rt.getSchema(klassIdMeta);
    rt.setDocSchema(rtDocSchema);

    // Read the stores header.
    // <stores> ::= [ <store> ]
    final int nstores = unpacker.readArrayBegin();
    for (int n = 0; n != nstores; n++) {
      // <store> ::= ( <store_name>, <klass_id>, <store_nelem> )
      final int ntriple = unpacker.readArrayBegin();
      if (ntriple != 3)
        throw new ReaderException("Invalid sized tuple read in: expected 3 elements but found " + ntriple);
      final String storeName = unpacker.readString();
      final int klassId = unpacker.readInt();
      final int nelem = unpacker.readInt();
      unpacker.readArrayEnd();

      // Sanity check on the value of the klassId.
      if (klassId >= rt.getSchemas().size())
        throw new ReaderException("klassId value " + klassId + " >= number of klasses (" + rt.getSchemas().size() + ")");

      // Lookup the store on the Doc class.
      StoreSchema def = null;
      for (StoreSchema store : docSchema.getStores()) {
        if (store.getSerial().equals(storeName)) {
          def = store;
          break;
        }
      }

      final RTAnnSchema klass = rt.getSchema(klassId);
      RTStoreSchema rtStoreSchema;
      if (def == null)
        rtStoreSchema = new RTStoreSchema(n, storeName, klass, null, nelem);
      else
        rtStoreSchema = new RTStoreSchema(n, storeName, klass, def);
      rtDocSchema.addStore(rtStoreSchema);

      // Ensure that the stream store and the static store agree on the klass they're storing.
      if (!rtStoreSchema.isLazy()) {
        final Class<? extends Ann> storeKlass = def.getStoredKlass();
        final Class<? extends Ann> klassKlass = klass.getDef().getKlass();
        if (!storeKlass.equals(klassKlass))
          throw new ReaderException("Store '" + storeName + "' points to " + storeKlass + " but the stream says it points to " + klassKlass);

        // Resize the store to house the correct number of instances.
        def.resize(nelem, doc);
      }
    }  // for each store.
    unpacker.readArrayEnd();


    // Back-fill each of the pointer fields to point to the actual RTStoreSchema instance.
    for (RTAnnSchema rtSchema : rt.getSchemas()) {
      for (RTFieldSchema rtField : rtSchema.getFields()) {
        if (!rtFieldSchemaToStoreIds.containsKey(rtField))
          continue;

        // Sanity check on the value of store_id.
        final int storeId = rtFieldSchemaToStoreIds.get(rtField);
        if (storeId >= rtDocSchema.getStores().size())
          throw new ReaderException("storeId value " + storeId + " >= number of stores (" + rtDocSchema.getStores().size()+ ")");
        final RTStoreSchema rtStore = rtDocSchema.getStore(storeId);

        // If the field isn't lazy, ensure the field and the store point to the same type.
        if (!rtField.isLazy()) {
          final Class<?> pointedToKlass = rtField.getDef().getPointedToKlass();
          final Class<?> storedKlass = rtStore.getDef().getStoredKlass();
          if (!pointedToKlass.equals(storedKlass))
            throw new ReaderException("Field points to " + pointedToKlass + " but the containing Store stores " + storedKlass);
        }

        // Update the field pointer value.
        rtField.setContainingStore(rtStore);
      }
    }


    // Read the document instance.
    // <doc_instance> ::= <instances_nbytes> <instance>
    do {
      final long _instancesNBytes = unpacker.readLong();
      if (_instancesNBytes > Integer.MAX_VALUE)
        throw new ReaderException("<instances_nbytes> is too large for Java (" + _instancesNBytes + ")");
      final int instancesNBytes = (int) _instancesNBytes;

      // Read all of the doc's fields lazily, if required.
      if (!docSchema.hasFields()) {
        byte[] lazyBytes = new byte[instancesNBytes];
        final int nbytesRead = in.read(lazyBytes);
        if (nbytesRead != instancesNBytes)
          throw new ReaderException("Failed to read in " + instancesNBytes + " from the input stream");

        // Attach the lazy fields to the doc.
        // TODO
        break;
      }

      final ByteArrayOutputStream lazyBOS = new ByteArrayOutputStream(instancesNBytes);
      final Packer lazyPacker = msgpack.createPacker(lazyBOS);
      int lazyNElem = 0;

      // <instance> ::= { <field_id> : <obj_val> }
      final int nitems = unpacker.readMapBegin();
      for (int i = 0; i != nitems; i++) {
        final int key = unpacker.readInt();
        final RTFieldSchema field = rtDocSchema.getField(key);

        // deserialize the field value if required
        if (field.isLazy()) {
          final Value lazyValue = unpacker.readValue();
          lazyPacker.write(key);
          lazyValue.writeTo(lazyPacker);
          lazyNElem++;
        }
        else
          ReaderHelper.read(field, doc, doc, unpacker);
      }  // for each field.
      unpacker.readMapEnd();

      // Store the lazy slab on the doc if it was used.
      if (lazyNElem != 0) {
        lazyPacker.flush();
        doc.setDRLazy(lazyBOS.toByteArray());
        doc.setDRLazyNElem(lazyNElem);
      }
    } while (false);


    // Read the store instances.
    // <instances_groups> ::= <instances_group>*
    for (RTStoreSchema rtStoreSchema : rtDocSchema.getStores()) {
      // <instances_group>  ::= <instances_nbytes> <instances>
      final long _instancesNBytes = unpacker.readLong();
      if (_instancesNBytes > Integer.MAX_VALUE)
        throw new ReaderException("<instances_nbytes> is too large for Java (" + _instancesNBytes + ")");
      final int instancesNBytes = (int) _instancesNBytes;

      // Read the store lazily, if required.
      if (rtStoreSchema.isLazy()) {
        byte[] lazyBytes = new byte[instancesNBytes];
        final int nbytesRead = in.read(lazyBytes);
        if (nbytesRead != instancesNBytes)
          throw new ReaderException("Failed to read in " + instancesNBytes + " from the input stream");

        rtStoreSchema.setLazy(lazyBytes);
        continue;
      }

      final RTAnnSchema storedKlass = rtStoreSchema.getStoredKlass();
      final Store<? extends Ann> store = rtStoreSchema.getDef().getStore(doc);

      // <instances> ::= [ <instance> ]
      final int ninstances = unpacker.readArrayBegin();
      for (int o = 0; o != ninstances; o++) {
        final Ann ann = store.get(o);
        final ByteArrayOutputStream lazyBOS = new ByteArrayOutputStream();
        final Packer lazyPacker = msgpack.createPacker(lazyBOS);
        int lazyNElem = 0;

        // <instance> ::= { <field_id> : <obj_val> }
        final int nitems = unpacker.readMapBegin();
        for (int i = 0; i != nitems; i++) {
          final int key = unpacker.readInt();
          final RTFieldSchema field = storedKlass.getField(key);

          // Deserialize the field value, if required.
          if (field.isLazy()) {
            final Value lazyValue = unpacker.readValue();
            lazyPacker.write(key);
            lazyValue.writeTo(lazyPacker);
            lazyNElem++;
          }
          else
            ReaderHelper.read(field, ann, doc, unpacker);
        }  // for each field.
        unpacker.readMapEnd();

        // If there were any lazy fields on the Ann instance, store them on the instance.
        if (lazyNElem != 0) {
          lazyPacker.flush();
          ann.setDRLazy(lazyBOS.toByteArray());
          ann.setDRLazyNElem(lazyNElem);
        }
      }  // for each instance

      unpacker.readArrayEnd();
    }  // for each instance group
  }
}
