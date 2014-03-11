package org.schwa.dr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;


public class PointersTest {
  @dr.Ann
  public static class Token extends AbstractAnn {
    @dr.Field public ByteSlice span;
    @dr.Field public String raw;
  }

  @dr.Ann
  public static class X extends AbstractAnn {
    @dr.Pointer(store="tokens") public List<Token> tokens = new ArrayList<Token>();
    @dr.SelfPointer             public List<X> prior = new ArrayList<X>();
  }

  @dr.Doc
  public static class TestDoc extends AbstractDoc {
    @dr.Store public Store<Token> tokens = new Store<Token>();
    @dr.Store public Store<X> xs = new Store<X>();
  }


  @Test
  public void test_pointers() throws IOException {
    final TestDoc doc0 = new TestDoc();

    doc0.tokens.create(Token.class, 10);
    for (int i = 0; i != 10; i++) {
      doc0.tokens.get(i).span = new ByteSlice(i, i + 1);
      doc0.tokens.get(i).raw = Character.toString((char) (i + 'a'));
    }

    doc0.xs.create(X.class, 5);
    doc0.xs.get(0).tokens.add(doc0.tokens.get(0));
    doc0.xs.get(1).tokens.add(doc0.tokens.get(0));
    doc0.xs.get(1).tokens.add(doc0.tokens.get(1));
    doc0.xs.get(1).tokens.add(doc0.tokens.get(2));
    doc0.xs.get(2).tokens.add(doc0.tokens.get(0));
    doc0.xs.get(2).tokens.add(doc0.tokens.get(1));
    doc0.xs.get(2).tokens.add(doc0.tokens.get(2));
    doc0.xs.get(2).tokens.add(doc0.tokens.get(3));
    doc0.xs.get(2).tokens.add(doc0.tokens.get(4));
    doc0.xs.get(4).tokens.add(doc0.tokens.get(9));
    for (int i = 0; i != 5; ++i)
      for (int j = 0; j <= i; ++j)
        doc0.xs.get(i).prior.add(doc0.xs.get(j));

    final byte[] correct = {
      (byte)0x02,
      (byte)0x93,  // <klasses>: 3-element array

      (byte)0x92,  // <klass>: 2-element array
      (byte)0xa8, '_', '_', 'm', 'e', 't', 'a', '_', '_',  // <klass_name>: 8-bytes of utf-8 encoded "__meta__"
      (byte)0x90,  // <fields>: 0-element array

      (byte)0x92,  // <klass>: 2-element array
      (byte)0xa5, 'T', 'o', 'k', 'e', 'n',  // <klass_name>
      (byte)0x92,  // <fields>: 2-element array
        (byte)0x82,  // <field>: 2-element map
          (byte)0x00, (byte)0xa4, 's', 'p', 'a', 'n',
          (byte)0x02, (byte)0xc0,
        (byte)0x81,  // <field>: 2-element map
          (byte)0x00, (byte)0xa3, 'r', 'a', 'w',

      (byte)0x92,  // <klass>: 2-element array
      (byte)0xa1, 'X',  // <klass_name>
      (byte)0x92,  // <fields>: 1-element array
        (byte)0x83,  // <field>: 3-element map
          (byte)0x00, (byte)0xa6, 't', 'o', 'k', 'e', 'n', 's',
          (byte)0x01, (byte)0x00,
          (byte)0x04, (byte)0xc0,
        (byte)0x83,  // <field>: 3-element map
          (byte)0x00, (byte)0xa5, 'p', 'r', 'i', 'o', 'r',
          (byte)0x03, (byte)0xc0,
          (byte)0x04, (byte)0xc0,

      (byte)0x92,  // <stores>: 2-element array
        (byte)0x93, (byte)0xa6, 't', 'o', 'k', 'e', 'n', 's', (byte)0x01, (byte)0x0a,
        (byte)0x93, (byte)0xa2, 'x', 's', (byte)0x02, (byte)0x05,

      (byte)0x01,  // <instance_nbytes>
      (byte)0x80,  // <instance>

      (byte)0x51,  // <instances_nbytes>
      (byte)0x9a,  // <instances>
      (byte)0x82, (byte)0x00, (byte)0x92, (byte)0x00, (byte)0x01, (byte)0x01, (byte)0xa1, 'a',
      (byte)0x82, (byte)0x00, (byte)0x92, (byte)0x01, (byte)0x01, (byte)0x01, (byte)0xa1, 'b',
      (byte)0x82, (byte)0x00, (byte)0x92, (byte)0x02, (byte)0x01, (byte)0x01, (byte)0xa1, 'c',
      (byte)0x82, (byte)0x00, (byte)0x92, (byte)0x03, (byte)0x01, (byte)0x01, (byte)0xa1, 'd',
      (byte)0x82, (byte)0x00, (byte)0x92, (byte)0x04, (byte)0x01, (byte)0x01, (byte)0xa1, 'e',
      (byte)0x82, (byte)0x00, (byte)0x92, (byte)0x05, (byte)0x01, (byte)0x01, (byte)0xa1, 'f',
      (byte)0x82, (byte)0x00, (byte)0x92, (byte)0x06, (byte)0x01, (byte)0x01, (byte)0xa1, 'g',
      (byte)0x82, (byte)0x00, (byte)0x92, (byte)0x07, (byte)0x01, (byte)0x01, (byte)0xa1, 'h',
      (byte)0x82, (byte)0x00, (byte)0x92, (byte)0x08, (byte)0x01, (byte)0x01, (byte)0xa1, 'i',
      (byte)0x82, (byte)0x00, (byte)0x92, (byte)0x09, (byte)0x01, (byte)0x01, (byte)0xa1, 'j',

      (byte)0x31,  // <instances_nbytes>
      (byte)0x95,  // <instances>
      (byte)0x82,
        (byte)0x00, (byte)0x91, (byte)0x00,
        (byte)0x01, (byte)0x91, (byte)0x00,
      (byte)0x82,
        (byte)0x00, (byte)0x93, (byte)0x00, (byte)0x01, (byte)0x02,
        (byte)0x01, (byte)0x92, (byte)0x00, (byte)0x01,
      (byte)0x82,
        (byte)0x00, (byte)0x95, (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04,
        (byte)0x01, (byte)0x93, (byte)0x00, (byte)0x01, (byte)0x02,
      (byte)0x81,
        (byte)0x01, (byte)0x94, (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03,
      (byte)0x82,
        (byte)0x00, (byte)0x91, (byte)0x09,
        (byte)0x01, (byte)0x95, (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04,
    };

    ByteArrayOutputStream actual = new ByteArrayOutputStream();
    DocSchema schema = DocSchema.create(TestDoc.class);
    Writer writer = new Writer(actual, schema);
    writer.write(doc0);

    Utils.assertArrayEquals(correct, actual.toByteArray());

    Reader reader = new Reader(new ByteArrayInputStream(correct), schema);
    Iterator<TestDoc> iterator = reader.iterator();
    Assert.assertTrue(iterator.hasNext());
    final TestDoc doc1 = iterator.next();

    Assert.assertEquals(10, doc1.tokens.size());
    Assert.assertEquals(5, doc1.xs.size());

    Assert.assertEquals("a", doc1.tokens.get(0).raw);
    Assert.assertEquals(new ByteSlice(0, 1), doc1.tokens.get(0).span);
    Assert.assertEquals("b", doc1.tokens.get(1).raw);
    Assert.assertEquals(new ByteSlice(1, 2), doc1.tokens.get(1).span);
    Assert.assertEquals("c", doc1.tokens.get(2).raw);
    Assert.assertEquals(new ByteSlice(2, 3), doc1.tokens.get(2).span);
    Assert.assertEquals("d", doc1.tokens.get(3).raw);
    Assert.assertEquals(new ByteSlice(3, 4), doc1.tokens.get(3).span);
    Assert.assertEquals("e", doc1.tokens.get(4).raw);
    Assert.assertEquals(new ByteSlice(4, 5), doc1.tokens.get(4).span);
    Assert.assertEquals("f", doc1.tokens.get(5).raw);
    Assert.assertEquals(new ByteSlice(5, 6), doc1.tokens.get(5).span);
    Assert.assertEquals("g", doc1.tokens.get(6).raw);
    Assert.assertEquals(new ByteSlice(6, 7), doc1.tokens.get(6).span);
    Assert.assertEquals("h", doc1.tokens.get(7).raw);
    Assert.assertEquals(new ByteSlice(7, 8), doc1.tokens.get(7).span);
    Assert.assertEquals("i", doc1.tokens.get(8).raw);
    Assert.assertEquals(new ByteSlice(8, 9), doc1.tokens.get(8).span);
    Assert.assertEquals("j", doc1.tokens.get(9).raw);
    Assert.assertEquals(new ByteSlice(9, 10), doc1.tokens.get(9).span);

    Assert.assertEquals(1, doc1.xs.get(0).tokens.size());
    Assert.assertEquals(3, doc1.xs.get(1).tokens.size());
    Assert.assertEquals(5, doc1.xs.get(2).tokens.size());
    Assert.assertEquals(0, doc1.xs.get(3).tokens.size());
    Assert.assertEquals(1, doc1.xs.get(4).tokens.size());

    Assert.assertEquals(doc1.tokens.get(0), doc1.xs.get(0).tokens.get(0));
    Assert.assertEquals(doc1.tokens.get(0), doc1.xs.get(1).tokens.get(0));
    Assert.assertEquals(doc1.tokens.get(1), doc1.xs.get(1).tokens.get(1));
    Assert.assertEquals(doc1.tokens.get(2), doc1.xs.get(1).tokens.get(2));
    Assert.assertEquals(doc1.tokens.get(0), doc1.xs.get(2).tokens.get(0));
    Assert.assertEquals(doc1.tokens.get(1), doc1.xs.get(2).tokens.get(1));
    Assert.assertEquals(doc1.tokens.get(2), doc1.xs.get(2).tokens.get(2));
    Assert.assertEquals(doc1.tokens.get(3), doc1.xs.get(2).tokens.get(3));
    Assert.assertEquals(doc1.tokens.get(4), doc1.xs.get(2).tokens.get(4));
    Assert.assertEquals(doc1.tokens.get(9), doc1.xs.get(4).tokens.get(0));

    Assert.assertEquals(1, doc1.xs.get(0).prior.size());
    Assert.assertEquals(2, doc1.xs.get(1).prior.size());
    Assert.assertEquals(3, doc1.xs.get(2).prior.size());
    Assert.assertEquals(4, doc1.xs.get(3).prior.size());
    Assert.assertEquals(5, doc1.xs.get(4).prior.size());

    Assert.assertEquals(doc1.xs.get(0), doc1.xs.get(0).prior.get(0));
    Assert.assertEquals(doc1.xs.get(0), doc1.xs.get(1).prior.get(0));
    Assert.assertEquals(doc1.xs.get(1), doc1.xs.get(1).prior.get(1));
    Assert.assertEquals(doc1.xs.get(0), doc1.xs.get(2).prior.get(0));
    Assert.assertEquals(doc1.xs.get(1), doc1.xs.get(2).prior.get(1));
    Assert.assertEquals(doc1.xs.get(2), doc1.xs.get(2).prior.get(2));
    Assert.assertEquals(doc1.xs.get(0), doc1.xs.get(3).prior.get(0));
    Assert.assertEquals(doc1.xs.get(1), doc1.xs.get(3).prior.get(1));
    Assert.assertEquals(doc1.xs.get(2), doc1.xs.get(3).prior.get(2));
    Assert.assertEquals(doc1.xs.get(3), doc1.xs.get(3).prior.get(3));
    Assert.assertEquals(doc1.xs.get(0), doc1.xs.get(4).prior.get(0));
    Assert.assertEquals(doc1.xs.get(1), doc1.xs.get(4).prior.get(1));
    Assert.assertEquals(doc1.xs.get(2), doc1.xs.get(4).prior.get(2));
    Assert.assertEquals(doc1.xs.get(3), doc1.xs.get(4).prior.get(3));
    Assert.assertEquals(doc1.xs.get(4), doc1.xs.get(4).prior.get(4));
  }
}
