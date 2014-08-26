package org.schwa.dr;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.msgpack.core.MessagePackFactory;
import org.msgpack.core.MessagePacker;

import org.schwa.dr.runtime.RTAnnSchema;
import org.schwa.dr.runtime.RTFactory;
import org.schwa.dr.runtime.RTFieldSchema;
import org.schwa.dr.runtime.RTManager;
import org.schwa.dr.runtime.RTStoreSchema;


public final class Writer {
  public static final byte WIRE_VERSION = 2;

  private final OutputStream out;
  private final DocSchema docSchema;
  private final MessagePacker packer;

  public Writer(OutputStream out, DocSchema docSchema) {
    this.out = out;
    this.docSchema = docSchema;
    this.packer = MessagePackFactory.newDefaultPacker(out);
  }

  public void write(final Doc doc) throws IOException {
    // Ger or construct the RTManager for the document.
    final RTManager rt = RTFactory.buildOrMerge(doc.getRT(), docSchema);
    final RTAnnSchema rtDocSchema = rt.getDocSchema();

    // <wire_version>
    packer.packByte(WIRE_VERSION);

    // <klasses>
    writeKlassesHeader(rt.getSchemas(), rtDocSchema);

    // <stores>
    writeStoresHeader(rtDocSchema.getStores(), doc);

    // <doc_instance> ::= <instances_nbytes> <instance>
    {
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      final DataOutputStream dos = new DataOutputStream(bos);
      writeInstance(doc, rtDocSchema, doc, dos);
      packer.packInt(bos.size());
      bos.writeTo(out);
    }

    // <instances_groups> ::= <instances_group>*
    for (RTStoreSchema rtStoreSchema : rtDocSchema.getStores()) {
      // <instances_group> ::= <instances_nbytes> <instances>
      if (rtStoreSchema.isLazy()) {
        final byte[] lazy = rtStoreSchema.getLazyData();
        packer.packInt(lazy.length);
        packer.flush();
        out.write(lazy, 0, lazy.length);
      }
      else {
        final RTAnnSchema storedKlass = rtStoreSchema.getStoredKlass();
        final Store<? extends Ann> store = rtStoreSchema.getDef().getStore(doc);

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(bos);
        packArrayHeader(dos, store.size());
        for (Ann ann : store)
          writeInstance(ann, storedKlass, doc, dos);
        packer.packInt(bos.size());
        bos.writeTo(out);
      }
    }

    // flush since we've written a whole document
    out.flush();
  }

  private void writeKlassesHeader(final List<RTAnnSchema> schemas, final RTAnnSchema rtDocSchema) throws IOException {
    // <klasses> ::= [ <klass> ]
    packer.packArrayHeader(schemas.size());
    int i = 0;
    for (RTAnnSchema schema : schemas) {
      // <klass> ::= ( <klass_name>, <fields> )
      packer.packArrayHeader(2);
      if (schema.getKlassId() != i)
        throw new AssertionError();

      // <klass_name>
      if (schema == rtDocSchema)
        packer.packString("__meta__");
      else if (schema.isLazy())
        packer.packString(schema.getSerial());
      else
        packer.packString(schema.getDef().getSerial());

      // <fields> ::= [ <field> ]
      packer.packArrayHeader(schema.getFields().size());
      for (RTFieldSchema field : schema.getFields()) {
        // <field> ::= { <field_type> : <field_val> }
        final int nfields = 1 + (field.isPointer() ? 1 : 0) + (field.isSlice() ? 1 : 0) + (field.isCollection() ? 1 : 0) + (field.isSelfPointer() ? 1 : 0);
        packer.packMapHeader(nfields);

        // <field_type> ::= 0 # NAME => the name of the field
        packer.packByte((byte) 0);
        packer.packString(field.isLazy() ? field.getSerial() : field.getDef().getSerial());

        // <field_type> ::= 1 # POINTER_TO => the <store_id> that this field points into
        if (field.isPointer()) {
          packer.packByte((byte) 1);
          packer.packInt(field.getContainingStore().getStoreId());
        }

        // <field_type> ::= 2 # IS_SLICE => whether or not this field is a "Slice" field
        if (field.isSlice()) {
          packer.packByte((byte) 2);
          packer.packNil();
        }

        // <field_type>  ::= 3 # IS_SELF_POINTER => whether or not this field is a self-pointer. POINTER_TO and IS_SELF_POINTER are mutually exclusive.
        if (field.isSelfPointer()) {
          packer.packByte((byte) 3);
          packer.packNil();
        }

        // <field_type>  ::= 4 # IS_COLLECTION => whether or not this field is a collection. IS_COLLECTION and IS_SLICE are mutually exclusive.
        if (field.isCollection()) {
          packer.packByte((byte) 4);
          packer.packNil();
        }
      }  // for each field.
      i++;
    }  // for each klass.
  }

  private void writeStoresHeader(final List<RTStoreSchema> stores, final Doc doc) throws IOException {
    // <stores> ::= [ <store> ]
    packer.packArrayHeader(stores.size());
    for (RTStoreSchema store : stores) {
      // <store> ::= ( <store_name>, <type_id>, <store_nelem> )
      packer.packArrayHeader(3);
      if (store.isLazy()) {
        packer.packString(store.getSerial());
        packer.packInt(store.getStoredKlass().getKlassId());
        packer.packInt(store.getLazyNElem());
      }
      else {
        packer.packString(store.getDef().getSerial());
        packer.packInt(store.getStoredKlass().getKlassId());
        packer.packInt(store.getDef().size(doc));
      }
    }  // for each store.
  }

  private void writeInstance(final Ann ann, final RTAnnSchema schema, final Doc doc, final DataOutputStream out) throws IOException {
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    final MessagePacker p = MessagePackFactory.newDefaultPacker(bos);

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

  private static void packArrayHeader(final DataOutputStream out, final int size) throws IOException {
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
