package org.schwa.dr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;


public class LazyTest {
  @dr.Ann
  public static class A0 extends AbstractAnn {
    @dr.Field public String v_str;
    @dr.Field public byte v_uint8;
    @dr.Field public boolean v_bool;
  }

  @dr.Ann
  public static class B0 extends AbstractAnn {
    @dr.Field(mode=FieldMode.READ_ONLY)
    public String word;
    @dr.Field
    public String upper;
    @dr.Field(mode=FieldMode.READ_ONLY)
    public boolean is_first;
  }

  @dr.Doc
  public static class DocA0 extends AbstractDoc {
    @dr.Store public Store<A0> as = new Store<A0>();
  }

  @dr.Doc
  public static class DocB0 extends AbstractDoc {
    @dr.Store public Store<B0> bs = new Store<B0>();
  }


  @Test
  public void lazyTest0() throws IOException {
    final String[] WORDS = {"The", "quick", "brown", "fox", "jumped"};
    final int NWORDS = WORDS.length;
    final byte[] STREAM0_EXPECTED = {
      (byte)0x03,
      (byte)0x92,
        (byte)0x92, (byte)0xA8, (byte)'_', (byte)'_', (byte)'m', (byte)'e', (byte)'t', (byte)'a', (byte)'_', (byte)'_', (byte)0x90,
        (byte)0x92, (byte)0xA2, (byte)'A', (byte)'0', (byte)0x93, (byte)0x81, (byte)0x00, (byte)0xA5, (byte)'v', (byte)'_', (byte)'s', (byte)'t', (byte)'r', (byte)0x81, (byte)0x00, (byte)0xA7, (byte)'v', (byte)'_', (byte)'u', (byte)'i', (byte)'n', (byte)'t', (byte)'8', (byte)0x81, (byte)0x00, (byte)0xA6, (byte)'v', (byte)'_', (byte)'b', (byte)'o', (byte)'o', (byte)'l',
      (byte)0x91,
        (byte)0x93, (byte)0xA2, (byte)'a', (byte)'s', (byte)0x01, (byte)0x05,
      (byte)0x01,
        (byte)0x80,
      (byte)0x3A, (byte)0x95,
        (byte)0x83, (byte)0x00, (byte)0xA3, (byte)'T', (byte)'h', (byte)'e', (byte)0x01, (byte)0x00, (byte)0x02, (byte)0xC3,
        (byte)0x83, (byte)0x00, (byte)0xA5, (byte)'q', (byte)'u', (byte)'i', (byte)'c', (byte)'k', (byte)0x01, (byte)0x01, (byte)0x02, (byte)0xC2,
        (byte)0x83, (byte)0x00, (byte)0xA5, (byte)'b', (byte)'r', (byte)'o', (byte)'w', (byte)'n', (byte)0x01, (byte)0x02, (byte)0x02, (byte)0xC2,
        (byte)0x83, (byte)0x00, (byte)0xA3, (byte)'f', (byte)'o', (byte)'x', (byte)0x01, (byte)0x03, (byte)0x02, (byte)0xC2,
        (byte)0x83, (byte)0x00, (byte)0xA6, (byte)'j', (byte)'u', (byte)'m', (byte)'p', (byte)'e', (byte)'d', (byte)0x01, (byte)0x04, (byte)0x02, (byte)0xC2,
    };
    final byte[] STREAM1_EXPECTED = {
      (byte)0x03,
      (byte)0x92,
        (byte)0x92, (byte)0xA8, (byte)'_', (byte)'_', (byte)'m', (byte)'e', (byte)'t', (byte)'a', (byte)'_', (byte)'_', (byte)0x90,
        (byte)0x92, (byte)0xA2, (byte)'B', (byte)'0', (byte)0x94, (byte)0x81, (byte)0x00, (byte)0xA4, (byte)'w', (byte)'o', (byte)'r', (byte)'d', (byte)0x81, (byte)0x00, (byte)0xA7, (byte)'v', (byte)'_', (byte)'u', (byte)'i', (byte)'n', (byte)'t', (byte)'8', (byte)0x81, (byte)0x00, (byte)0xA8, (byte)'i', (byte)'s', (byte)'_', (byte)'f', (byte)'i', (byte)'r', (byte)'s', (byte)'t', (byte)0x81, (byte)0x00, (byte)0xA5, (byte)'u', (byte)'p', (byte)'p', (byte)'e', (byte)'r',
      (byte)0x91,
        (byte)0x93, (byte)0xA2, (byte)'b', (byte)'s', (byte)0x01, (byte)0x05,
      (byte)0x01,
        (byte)0x80,
      (byte)0x5A, (byte)0x95,
        (byte)0x84, (byte)0x00, (byte)0xA3, (byte)'T', (byte)'h', (byte)'e', (byte)0x01, (byte)0x00, (byte)0x02, (byte)0xC3, (byte)0x03, (byte)0xA3, (byte)'T', (byte)'H', (byte)'E',
        (byte)0x84, (byte)0x00, (byte)0xA5, (byte)'q', (byte)'u', (byte)'i', (byte)'c', (byte)'k', (byte)0x01, (byte)0x01, (byte)0x02, (byte)0xC2, (byte)0x03, (byte)0xA5, (byte)'Q', (byte)'U', (byte)'I', (byte)'C', (byte)'K',
        (byte)0x84, (byte)0x00, (byte)0xA5, (byte)'b', (byte)'r', (byte)'o', (byte)'w', (byte)'n', (byte)0x01, (byte)0x02, (byte)0x02, (byte)0xC2, (byte)0x03, (byte)0xA5, (byte)'B', (byte)'R', (byte)'O', (byte)'W', (byte)'N',
        (byte)0x84, (byte)0x00, (byte)0xA3, (byte)'f', (byte)'o', (byte)'x', (byte)0x01, (byte)0x03, (byte)0x02, (byte)0xC2, (byte)0x03, (byte)0xA3, (byte)'F', (byte)'O', (byte)'X',
        (byte)0x84, (byte)0x00, (byte)0xA6, (byte)'j', (byte)'u', (byte)'m', (byte)'p', (byte)'e', (byte)'d', (byte)0x01, (byte)0x04, (byte)0x02, (byte)0xC2, (byte)0x03, (byte)0xA6, (byte)'J', (byte)'U', (byte)'M', (byte)'P', (byte)'E', (byte)'D',
    };

    ByteArrayOutputStream stream0 = new ByteArrayOutputStream();
    ByteArrayOutputStream stream1 = new ByteArrayOutputStream();

    DocA0 doc0 = new DocA0();
    DocSchema schema0 = DocSchema.create(DocA0.class);
    Writer writer0 = new Writer(stream0, schema0);

    doc0.as.create(A0.class, NWORDS);
    for (int i = 0; i != NWORDS; ++i) {
      final A0 a = doc0.as.get(i);
      a.v_str = WORDS[i];
      a.v_uint8 = (byte)i;
      a.v_bool = i == 0;
    }
    writer0.write(doc0);

    Utils.assertArrayEquals(STREAM0_EXPECTED, stream0.toByteArray());

    DocSchema schema1In = DocSchema.create(DocB0.class);
    DocSchema schema1Out = DocSchema.create(DocB0.class);
    schema1In.getStore("bs").setSerial("as");
    schema1In.getSchema(B0.class).setSerial("A0");
    schema1In.getSchema(B0.class).getField("word").setSerial("v_str");
    schema1In.getSchema(B0.class).getField("is_first").setSerial("v_bool");
    Reader<DocB0> reader1 = new Reader(new ByteArrayInputStream(stream0.toByteArray()), schema1In);
    Writer writer1 = new Writer(stream1, schema1Out);

    int nDocsRead1 = 0;
    for (DocB0 doc1 : reader1) {
      nDocsRead1++;

      // Check annotation values.
      Assert.assertEquals(NWORDS, doc1.bs.size());
      for (int i = 0; i != NWORDS; ++i) {
        final B0 b = doc1.bs.get(i);
        Assert.assertEquals(WORDS[i], b.word);
        Assert.assertNull(b.upper);
        Assert.assertEquals(i == 0, b.is_first);
      }

      // Check document lazy.
      Assert.assertNull(doc1.getDRLazy());
      Assert.assertEquals(0, doc1.getDRLazyNElem());

      // Check annotation lazy.
      for (int i = 0; i != NWORDS; ++i) {
        final B0 b = doc1.bs.get(i);
        Assert.assertNotNull(b.getDRLazy());
        Assert.assertEquals(3, b.getDRLazyNElem());
        Assert.assertEquals(3 + (b.word.length() + 1) + 1 + 1, b.getDRLazy().length);
      }

      // Modify some attributes, including lazy ones.
      for (int i = 0; i != NWORDS; ++i) {
        final B0 b = doc1.bs.get(i);
        b.upper = "";
        for (int j = 0; j != b.word.length(); ++j)
          b.upper += Character.toUpperCase(b.word.charAt(j));
        b.is_first = true;
      }

      writer1.write(doc1);
    }
    Assert.assertEquals(1, nDocsRead1);

    Utils.assertArrayEquals(STREAM1_EXPECTED, stream1.toByteArray());

    DocSchema schema2 = DocSchema.create(DocA0.class);
    schema2.getStore("as").setSerial("bs");
    schema2.getSchema(A0.class).setSerial("B0");
    schema2.getSchema(A0.class).getField("v_str").setSerial("word");
    schema2.getSchema(A0.class).getField("v_bool").setSerial("is_first");
    Reader<DocA0> reader2 = new Reader(new ByteArrayInputStream(stream1.toByteArray()), schema2);

    int nDocsRead2 = 0;
    for (DocA0 doc2 : reader2) {
      nDocsRead2++;

      // Check annotation values.
      Assert.assertEquals(NWORDS, doc2.as.size());
      for (int i = 0; i != NWORDS; ++i) {
        final A0 a = doc2.as.get(i);
        Assert.assertEquals(WORDS[i], a.v_str);
        Assert.assertEquals((byte)i, a.v_uint8);
        Assert.assertEquals(i == 0, a.v_bool);
      }

      // Check document lazy.
      Assert.assertNull(doc2.getDRLazy());
      Assert.assertEquals(0, doc2.getDRLazyNElem());

      // Check annotation lazy.
      for (int i = 0; i != NWORDS; ++i) {
        final A0 a = doc2.as.get(i);
        Assert.assertNotNull(a.getDRLazy());
        Assert.assertEquals(1, a.getDRLazyNElem());
        Assert.assertEquals(1 + (a.v_str.length() + 1), a.getDRLazy().length);
      }
    }
    Assert.assertEquals(1, nDocsRead2);
  }


  @dr.Ann
  public static class A1 extends AbstractAnn {
    @dr.Field public String v_str;
    @dr.Field public byte v_uint8;
    @dr.Field public boolean v_bool;
  }

  @dr.Ann
  public static class B1 extends AbstractAnn {
    @dr.Field public String word;
    @dr.Field public String upper;
    @dr.Field public boolean is_first;
  }

  @dr.Doc
  public static class DocA1 extends AbstractDoc {
    @dr.Store public Store<A1> as = new Store<A1>();
  }

  @dr.Doc
  public static class DocB1 extends AbstractDoc {
    @dr.Store public Store<B1> bs = new Store<B1>();
  }

  @dr.Doc
  public static class DocAB1 extends AbstractDoc {
    @dr.Store(mode=FieldMode.READ_ONLY)
    public Store<A1> as = new Store<A1>();
    @dr.Store(mode=FieldMode.READ_ONLY)
    public Store<B1> bs = new Store<B1>();
  }


  @Test
  public void lazyTest1() throws IOException {
    final String[] WORDS = {"How", "now", "brown", "cow"};
    final int NWORDS = WORDS.length;
    final byte[] STREAM0_EXPECTED = {
      (byte)0x03,
      (byte)0x92,
        (byte)0x92, (byte)0xA8, (byte)'_', (byte)'_', (byte)'m', (byte)'e', (byte)'t', (byte)'a', (byte)'_', (byte)'_', (byte)0x90,
        (byte)0x92, (byte)0xA2, (byte)'A', (byte)'1', (byte)0x93, (byte)0x81, (byte)0x00, (byte)0xA5, (byte)'v', (byte)'_', (byte)'s', (byte)'t', (byte)'r', (byte)0x81, (byte)0x00, (byte)0xA7, (byte)'v', (byte)'_', (byte)'u', (byte)'i', (byte)'n', (byte)'t', (byte)'8', (byte)0x81, (byte)0x00, (byte)0xA6, (byte)'v', (byte)'_', (byte)'b', (byte)'o', (byte)'o', (byte)'l',
      (byte)0x91,
        (byte)0x93, (byte)0xA2, (byte)'a', (byte)'s', (byte)0x01, (byte)0x04,
      (byte)0x01,
        (byte)0x80,
      (byte)0x2B, (byte)0x94,
        (byte)0x83, (byte)0x00, (byte)0xA3, (byte)'H', (byte)'o', (byte)'w', (byte)0x01, (byte)0x00, (byte)0x02, (byte)0xC3,
        (byte)0x83, (byte)0x00, (byte)0xA3, (byte)'n', (byte)'o', (byte)'w', (byte)0x01, (byte)0x01, (byte)0x02, (byte)0xC2,
        (byte)0x83, (byte)0x00, (byte)0xA5, (byte)'b', (byte)'r', (byte)'o', (byte)'w', (byte)'n', (byte)0x01, (byte)0x02, (byte)0x02, (byte)0xC3,
        (byte)0x83, (byte)0x00, (byte)0xA3, (byte)'c', (byte)'o', (byte)'w', (byte)0x01, (byte)0x03, (byte)0x02, (byte)0xC2,
    };
    final byte[] STREAM1_EXPECTED = {
      (byte)0x03,
      (byte)0x93,
        (byte)0x92, (byte)0xA8, (byte)'_', (byte)'_', (byte)'m', (byte)'e', (byte)'t', (byte)'a', (byte)'_', (byte)'_', (byte)0x90,
        (byte)0x92, (byte)0xA2, (byte)'A', (byte)'1', (byte)0x93, (byte)0x81, (byte)0x00, (byte)0xA5, (byte)'v', (byte)'_', (byte)'s', (byte)'t', (byte)'r', (byte)0x81, (byte)0x00, (byte)0xA7, (byte)'v', (byte)'_', (byte)'u', (byte)'i', (byte)'n', (byte)'t', (byte)'8', (byte)0x81, (byte)0x00, (byte)0xA6, (byte)'v', (byte)'_', (byte)'b', (byte)'o', (byte)'o', (byte)'l',
        (byte)0x92, (byte)0xA2, (byte)'B', (byte)'1', (byte)0x93, (byte)0x81, (byte)0x00, (byte)0xA4, (byte)'w', (byte)'o', (byte)'r', (byte)'d', (byte)0x81, (byte)0x00, (byte)0xA5, (byte)'u', (byte)'p', (byte)'p', (byte)'e', (byte)'r', (byte)0x81, (byte)0x00, (byte)0xA8, (byte)'i', (byte)'s', (byte)'_', (byte)'f', (byte)'i', (byte)'r', (byte)'s', (byte)'t',
      (byte)0x92,
        (byte)0x93, (byte)0xA2, (byte)'a', (byte)'s', (byte)0x01, (byte)0x04,
        (byte)0x93, (byte)0xA2, (byte)'b', (byte)'s', (byte)0x02, (byte)0x04,
      (byte)0x01,
        (byte)0x80,
      (byte)0x2B, (byte)0x94,
        (byte)0x83, (byte)0x00, (byte)0xA3, (byte)'H', (byte)'o', (byte)'w', (byte)0x01, (byte)0x00, (byte)0x02, (byte)0xC3,
        (byte)0x83, (byte)0x00, (byte)0xA3, (byte)'n', (byte)'o', (byte)'w', (byte)0x01, (byte)0x01, (byte)0x02, (byte)0xC2,
        (byte)0x83, (byte)0x00, (byte)0xA5, (byte)'b', (byte)'r', (byte)'o', (byte)'w', (byte)'n', (byte)0x01, (byte)0x02, (byte)0x02, (byte)0xC3,
        (byte)0x83, (byte)0x00, (byte)0xA3, (byte)'c', (byte)'o', (byte)'w', (byte)0x01, (byte)0x03, (byte)0x02, (byte)0xC2,
      (byte)0x39, (byte)0x94,
        (byte)0x83, (byte)0x00, (byte)0xA3, (byte)'H', (byte)'o', (byte)'w', (byte)0x01, (byte)0xA3, (byte)'H', (byte)'O', (byte)'W', (byte)0x02, (byte)0xC3,
        (byte)0x83, (byte)0x00, (byte)0xA3, (byte)'n', (byte)'o', (byte)'w', (byte)0x01, (byte)0xA3, (byte)'N', (byte)'O', (byte)'W', (byte)0x02, (byte)0xC2,
        (byte)0x83, (byte)0x00, (byte)0xA5, (byte)'b', (byte)'r', (byte)'o', (byte)'w', (byte)'n', (byte)0x01, (byte)0xA5, (byte)'B', (byte)'R', (byte)'O', (byte)'W', (byte)'N', (byte)0x02, (byte)0xC2,
        (byte)0x83, (byte)0x00, (byte)0xA3, (byte)'c', (byte)'o', (byte)'w', (byte)0x01, (byte)0xA3, (byte)'C', (byte)'O', (byte)'W', (byte)0x02, (byte)0xC2,
    };

    ByteArrayOutputStream stream0 = new ByteArrayOutputStream();
    ByteArrayOutputStream stream1 = new ByteArrayOutputStream();

    DocA1 doc0 = new DocA1();
    DocSchema schema0 = DocSchema.create(DocA1.class);
    Writer writer0 = new Writer(stream0, schema0);

    doc0.as.create(A1.class, NWORDS);
    for (int i = 0; i != NWORDS; ++i) {
      final A1 a = doc0.as.get(i);
      a.v_str = WORDS[i];
      a.v_uint8 = (byte)i;
      a.v_bool = i % 2 == 0;
    }
    writer0.write(doc0);

    Utils.assertArrayEquals(STREAM0_EXPECTED, stream0.toByteArray());

    DocSchema schema1 = DocSchema.create(DocB1.class);
    Reader<DocB1> reader1 = new Reader(new ByteArrayInputStream(stream0.toByteArray()), schema1);
    Writer writer1 = new Writer(stream1, schema1);

    int nDocsRead1 = 0;
    for (DocB1 doc1 : reader1) {
      nDocsRead1++;

      // Check annotation values.
      Assert.assertEquals(0, doc1.bs.size());

      // Modify some attributes, including lazy ones.
      doc1.bs.create(B1.class, NWORDS);
      for (int i = 0; i != NWORDS; ++i) {
        final B1 b = doc1.bs.get(i);
        b.word = WORDS[i];
        b.is_first = i == 0;
        b.upper = "";
        for (int j = 0; j != b.word.length(); ++j)
          b.upper += Character.toUpperCase(b.word.charAt(j));
      }

      writer1.write(doc1);
    }
    Assert.assertEquals(1, nDocsRead1);

    Utils.assertArrayEquals(STREAM1_EXPECTED, stream1.toByteArray());

    DocSchema schema2 = DocSchema.create(DocAB1.class);
    Reader<DocAB1> reader2 = new Reader(new ByteArrayInputStream(stream1.toByteArray()), schema2);

    int nDocsRead2 = 0;
    for (DocAB1 doc2 : reader2) {
      nDocsRead2++;

      // Check annotation values.
      Assert.assertEquals(NWORDS, doc2.as.size());
      Assert.assertEquals(NWORDS, doc2.bs.size());
      for (int i = 0; i != NWORDS; ++i) {
        final A1 a = doc2.as.get(i);
        Assert.assertEquals(WORDS[i], a.v_str);
        Assert.assertEquals((byte)i, a.v_uint8);
        Assert.assertEquals(i % 2 == 0, a.v_bool);

        final B1 b = doc2.bs.get(i);
        Assert.assertEquals(WORDS[i], b.word);
        Assert.assertEquals(WORDS[i].toUpperCase(), b.upper);
        Assert.assertEquals(i == 0, b.is_first);
      }
    }
    Assert.assertEquals(1, nDocsRead2);
  }


  @dr.Doc
  public static class FauxDoc extends AbstractDoc {
  }


  @Test
  public void lazyTestPointerTo_0() throws IOException {
    final byte[] STREAM = {
      (byte)0x03, // version
      (byte)0x92, // <klasses>: 2-element array
        (byte)0x92, // <klass>
          (byte)0xA8, (byte)'_', (byte)'_', (byte)'m', (byte)'e', (byte)'t', (byte)'a', (byte)'_', (byte)'_', // <klass_name>
          (byte)0x91, // <fields>
            (byte)0x82, // 2 attributes
            (byte)0x00, (byte)0xA6, (byte)'a', (byte)'s', (byte)'_', (byte)'p', (byte)'t', (byte)'r', // name
            (byte)0x01, (byte)0x00, // ptr to store 0
        (byte)0x92, // <klass>
          (byte)0xA1, (byte)'A', // <klass_name>
          (byte)0x90, // <fields>
      (byte)0x91, // <stores>: 1-element array
        (byte)0x93, (byte)0xA2, (byte)'a', (byte)'s', (byte)0x01, (byte)0x00, // 0-element store
      (byte)0x01, (byte)0x80, // Empty document
      (byte)0x01, (byte)0x90, // Empty store
    };

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    DocSchema schema = DocSchema.create(FauxDoc.class);
    Reader<FauxDoc> reader = new Reader(new ByteArrayInputStream(STREAM), schema);
    Assert.assertTrue(reader.hasNext());
    FauxDoc doc = reader.next();
    Assert.assertNotNull(doc);
    Writer writer = new Writer(output, schema);
    writer.write(doc);

    Utils.assertArrayEquals(STREAM, output.toByteArray());
  }
}
