package org.schwa.dr;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;

import org.schwa.dr.runtime.RTAnnSchema;
import org.schwa.dr.runtime.RTFactory;
import org.schwa.dr.runtime.RTFieldSchema;
import org.schwa.dr.runtime.RTManager;
import org.schwa.dr.runtime.RTStoreSchema;


public final class Writer {
  public static final byte WIRE_VERSION = 2;

  private final OutputStream out;
  private final DocSchema docSchema;
  private final MessagePack msgpack;
  private final Packer packer;

  public Writer(OutputStream out, DocSchema docSchema) {
    this.out = out;
    this.docSchema = docSchema;
    this.msgpack = new MessagePack();
    this.packer = msgpack.createPacker(out);
  }

  public void write(final Doc doc) throws IOException {
    // Ger or construct the RTManager for the document.
    final RTManager rt = RTFactory.buildOrMerge(doc.getRT(), docSchema);
    final RTAnnSchema rtDocSchema = rt.getDocSchema();

    // <wire_version>
    packer.write(WIRE_VERSION);

    // <klasses>
    writeKlassesHeader(rt.getSchemas(), rtDocSchema);

    // <stores>
    writeStoresHeader(rtDocSchema.getStores(), doc);

    // <doc_instance> ::= <instances_nbytes> <instance>
    {
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      final DataOutputStream dos = new DataOutputStream(bos);
      writeInstance(doc, rtDocSchema, doc, dos);
      packer.write(bos.size());
      bos.writeTo(out);
    }

    // <instances_groups> ::= <instances_group>*
    for (RTStoreSchema rtStoreSchema : rtDocSchema.getStores()) {
      // <instances_group> ::= <instances_nbytes> <instances>
      if (rtStoreSchema.isLazy()) {
        final byte[] lazy = rtStoreSchema.getLazyData();
        packer.write(lazy.length);
        packer.flush();
        out.write(lazy, 0, lazy.length);
      }
      else {
        final RTAnnSchema storedKlass = rtStoreSchema.getStoredKlass();
        final Store<? extends Ann> store = rtStoreSchema.getDef().getStore(doc);

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(bos);
        writeArrayBegin(dos, store.size());
        for (Ann ann : store)
          writeInstance(ann, storedKlass, doc, dos);
        packer.write(bos.size());
        bos.writeTo(out);
      }
    }

    // flush since we've written a whole document
    out.flush();
  }

  private void writeKlassesHeader(final List<RTAnnSchema> schemas, final RTAnnSchema rtDocSchema) throws IOException {
    // <klasses> ::= [ <klass> ]
    packer.writeArrayBegin(schemas.size());
    int i = 0;
    for (RTAnnSchema schema : schemas) {
      // <klass> ::= ( <klass_name>, <fields> )
      packer.writeArrayBegin(2);
      if (schema.getKlassId() != i)
        throw new AssertionError();

      // <klass_name>
      if (schema == rtDocSchema)
        packer.write("__meta__");
      else if (schema.isLazy())
        packer.write(schema.getSerial());
      else
        packer.write(schema.getDef().getSerial());

      // <fields> ::= [ <field> ]
      packer.writeArrayBegin(schema.getFields().size());
      for (RTFieldSchema field : schema.getFields()) {
        // <field> ::= { <field_type> : <field_val> }
        final int nfields = 1 + (field.isPointer() ? 1 : 0) + (field.isSlice() ? 1 : 0) + (field.isCollection() ? 0 : 1) + (field.isSelfPointer() ? 1 : 0);
        packer.writeMapBegin(nfields);

        // <field_type> ::= 0 # NAME => the name of the field
        packer.write((byte) 0);
        packer.write(field.isLazy() ? field.getSerial() : field.getDef().getSerial());

        // <field_type> ::= 1 # POINTER_TO => the <store_id> that this field points into
        if (field.isPointer()) {
          packer.write((byte) 1);
          packer.write(field.getContainingStore().getStoreId());
        }

        // <field_type> ::= 2 # IS_SLICE => whether or not this field is a "Slice" field
        if (field.isSlice()) {
          packer.write((byte) 2);
          packer.writeNil();
        }

        // <field_type>  ::= 3 # IS_SELF_POINTER => whether or not this field is a self-pointer. POINTER_TO and IS_SELF_POINTER are mutually exclusive.
        if (field.isSelfPointer()) {
          packer.write((byte) 3);
          packer.writeNil();
        }

        // <field_type>  ::= 4 # IS_COLLECTION => whether or not this field is a collection. IS_COLLECTION and IS_SLICE are mutually exclusive.
        if (field.isCollection()) {
          packer.write((byte) 4);
          packer.writeNil();
        }

        packer.writeMapEnd(); // <field>
      }  // for each field.
      packer.writeArrayEnd(); // <fields>

      packer.writeArrayEnd(); // <klass>
      i++;
    }  // for each klass.
    packer.writeArrayEnd(); // <klasses>
  }

  private void writeStoresHeader(final List<RTStoreSchema> stores, final Doc doc) throws IOException {
    // <stores> ::= [ <store> ]
    packer.writeArrayBegin(stores.size());
    for (RTStoreSchema store : stores) {
      // <store> ::= ( <store_name>, <type_id>, <store_nelem> )
      packer.writeArrayBegin(3);
      if (store.isLazy()) {
        packer.write(store.getSerial());
        packer.write(store.getStoredKlass().getKlassId());
        packer.write(store.getLazyNElem());
      }
      else {
        packer.write(store.getDef().getSerial());
        packer.write(store.getStoredKlass().getKlassId());
        packer.write(store.getDef().size(doc));
      }
      packer.writeArrayEnd(); // <store>
    }  // for each store.
    packer.writeArrayEnd(); // <stores>
  }

  private void writeInstance(final Ann ann, final RTAnnSchema schema, final Doc doc, final DataOutputStream out) throws IOException {
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    final Packer p = msgpack.createPacker(bos);

    int nNewElem = 0;
    for (RTFieldSchema field : schema.getFields())
      if (!field.isLazy() && WriterHelper.write(p, field, ann))
        nNewElem++;
    p.flush();

    final int nElem = nNewElem + ann.getDRLazyNElem();
    writeMapBegin(out, nElem);
    if (nElem != 0) {
      if (ann.getDRLazyNElem() != 0) {
        final byte[] lazy = ann.getDRLazy();
        out.write(lazy, 0, lazy.length);
      }
      bos.writeTo(out);
    }
  }

  private static void writeArrayBegin(final DataOutputStream out, final int size) throws IOException {
    if (size < 16)
      out.write((byte) (0x90 | size));
    else if (size < 65536) {
      out.write((byte) 0xdc);
      out.writeShort((short) size);
    }
    else {
      out.write((byte) 0xdd);
      out.writeInt(size);
    }
  }

  private static void writeMapBegin(final DataOutputStream out, final int size) throws IOException {
    if (size < 16)
      out.write((byte) (0x80 | size));
    else if (size < 65536) {
      out.write((byte) 0xde);
      out.writeShort((short) size);
    }
    else {
      out.write((byte) 0xdf);
      out.writeInt(size);
    }
  }
}
