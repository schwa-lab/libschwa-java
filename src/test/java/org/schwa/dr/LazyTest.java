package org.schwa.dr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;


public class LazyTest {
  @dr.Ann
  public static class A extends AbstractAnn {
    @dr.Field public String v_str;
    @dr.Field public byte v_uint8;
    @dr.Field public boolean v_bool;
  }

  @dr.Ann
  public static class B extends AbstractAnn {
    @dr.Field(mode=FieldMode.READ_ONLY)
    public String word;
    @dr.Field
    public String upper;
    @dr.Field(mode=FieldMode.READ_ONLY)
    public boolean is_first;
  }

  @dr.Doc
  public static class DocA extends AbstractDoc {
    @dr.Store public Store<A> as = new Store<A>();
  }

  @dr.Doc
  public static class DocB extends AbstractDoc {
    @dr.Store public Store<B> bs = new Store<B>();
  }

  @dr.Doc
  public static class DocAB extends AbstractDoc {
    @dr.Store(mode=FieldMode.READ_ONLY)
    public Store<A> as = new Store<A>();
    @dr.Store(mode=FieldMode.READ_ONLY)
    public Store<B> bs = new Store<B>();
  }

  @Test
  public void lazy_test0() throws IOException {
    final String[] WORDS = {"The", "quick", "brown", "fox", "jumped"};
    final int NWORDS = WORDS.length;
    final byte[] stream0Expected = {
      (byte)0x03,
      (byte)0x92,
        (byte)0x92, (byte)0xA8, (byte)'_', (byte)'_', (byte)'m', (byte)'e', (byte)'t', (byte)'a', (byte)'_', (byte)'_', (byte)0x90,
        (byte)0x92, (byte)0xA1, (byte)'A', (byte)0x93, (byte)0x81, (byte)0x00, (byte)0xA5, (byte)'v', (byte)'_', (byte)'s', (byte)'t', (byte)'r', (byte)0x81, (byte)0x00, (byte)0xA7, (byte)'v', (byte)'_', (byte)'u', (byte)'i', (byte)'n', (byte)'t', (byte)'8', (byte)0x81, (byte)0x00, (byte)0xA6, (byte)'v', (byte)'_', (byte)'b', (byte)'o', (byte)'o', (byte)'l',
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
    final byte[] stream1Expected = {
      (byte)0x03,
      (byte)0x92,
        (byte)0x92, (byte)0xA8, (byte)'_', (byte)'_', (byte)'m', (byte)'e', (byte)'t', (byte)'a', (byte)'_', (byte)'_', (byte)0x90,
        (byte)0x92, (byte)0xA1, (byte)'B', (byte)0x94, (byte)0x81, (byte)0x00, (byte)0xA4, (byte)'w', (byte)'o', (byte)'r', (byte)'d', (byte)0x81, (byte)0x00, (byte)0xA7, (byte)'v', (byte)'_', (byte)'u', (byte)'i', (byte)'n', (byte)'t', (byte)'8', (byte)0x81, (byte)0x00, (byte)0xA8, (byte)'i', (byte)'s', (byte)'_', (byte)'f', (byte)'i', (byte)'r', (byte)'s', (byte)'t', (byte)0x81, (byte)0x00, (byte)0xA5, (byte)'u', (byte)'p', (byte)'p', (byte)'e', (byte)'r',
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

    DocA doc0 = new DocA();
    DocSchema schema0 = DocSchema.create(DocA.class);
    Writer writer0 = new Writer(stream0, schema0);

    doc0.as.create(A.class, NWORDS);
    for (int i = 0; i != NWORDS; ++i) {
      final A a = doc0.as.get(i);
      a.v_str = WORDS[i];
      a.v_uint8 = (byte)i;
      a.v_bool = i == 0;
    }
    writer0.write(doc0);

    Utils.assertArrayEquals(stream0Expected, stream0.toByteArray());

    DocSchema schema1In = DocSchema.create(DocB.class);
    DocSchema schema1Out = DocSchema.create(DocB.class);
    schema1In.getStore("bs").setSerial("as");
    schema1In.getSchema(B.class).setSerial("A");
    schema1In.getSchema(B.class).getField("word").setSerial("v_str");
    schema1In.getSchema(B.class).getField("is_first").setSerial("v_bool");
    Reader<DocB> reader1 = new Reader(new ByteArrayInputStream(stream0.toByteArray()), schema1In);
    Writer writer1 = new Writer(stream1, schema1Out);

    int nDocsRead1 = 0;
    for (DocB doc1 : reader1) {
      nDocsRead1++;

      // Check annotation values.
      Assert.assertEquals(NWORDS, doc1.bs.size());
      for (int i = 0; i != NWORDS; ++i) {
        final B b = doc1.bs.get(i);
        Assert.assertEquals(WORDS[i], b.word);
        Assert.assertNull(b.upper);
        Assert.assertEquals(i == 0, b.is_first);
      }

      // Check document lazy.
      Assert.assertNull(doc1.getDRLazy());
      Assert.assertEquals(0, doc1.getDRLazyNElem());

      // Check annotation lazy.
      for (int i = 0; i != NWORDS; ++i) {
        final B b = doc1.bs.get(i);
        Assert.assertNotNull(b.getDRLazy());
        Assert.assertEquals(3, b.getDRLazyNElem());
        Assert.assertEquals(3 + (b.word.length() + 1) + 1 + 1, b.getDRLazy().length);
      }

      // Modify some attributes, including lazy ones.
      for (int i = 0; i != NWORDS; ++i) {
        final B b = doc1.bs.get(i);
        b.upper = "";
        for (int j = 0; j != b.word.length(); ++j)
          b.upper += Character.toUpperCase(b.word.charAt(j));
        b.is_first = true;
      }

      writer1.write(doc1);
    }
    Assert.assertEquals(1, nDocsRead1);

    Utils.assertArrayEquals(stream1Expected, stream1.toByteArray());

    DocSchema schema2 = DocSchema.create(DocA.class);
    schema2.getStore("as").setSerial("bs");
    schema2.getSchema(A.class).setSerial("B");
    schema2.getSchema(A.class).getField("v_str").setSerial("word");
    schema2.getSchema(A.class).getField("v_bool").setSerial("is_first");
    Reader<DocA> reader2 = new Reader(new ByteArrayInputStream(stream1.toByteArray()), schema2);

    int nDocsRead2 = 0;
    for (DocA doc2 : reader2) {
      nDocsRead2++;

      // Check annotation values.
      Assert.assertEquals(NWORDS, doc2.as.size());
      for (int i = 0; i != NWORDS; ++i) {
        final A a = doc2.as.get(i);
        Assert.assertEquals(WORDS[i], a.v_str);
        Assert.assertEquals((byte)i, a.v_uint8);
        Assert.assertEquals(i == 0, a.v_bool);
      }

      // Check document lazy.
      Assert.assertNull(doc2.getDRLazy());
      Assert.assertEquals(0, doc2.getDRLazyNElem());

      // Check annotation lazy.
      for (int i = 0; i != NWORDS; ++i) {
        final A a = doc2.as.get(i);
        Assert.assertNotNull(a.getDRLazy());
        Assert.assertEquals(1, a.getDRLazyNElem());
        Assert.assertEquals(1 + (a.v_str.length() + 1), a.getDRLazy().length);
      }
    }
    Assert.assertEquals(1, nDocsRead2);
  }
}
