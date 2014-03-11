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


public class SelfPointerTest {
  @dr.Ann
  public static class X extends AbstractAnn {
    @dr.SelfPointer          public X parent;
    @dr.Pointer(store="xs2") public X other;
  }

  @dr.Doc
  public static class TestDoc extends AbstractDoc {
    @dr.Store public Store<X> xs1 = new Store<X>();
    @dr.Store public Store<X> xs2 = new Store<X>();
  }


  @Test
  public void test_self_pointer() throws IOException {
    final TestDoc doc0 = new TestDoc();

    doc0.xs1.create(X.class, 3);
    doc0.xs2.create(X.class, 2);

    doc0.xs1.get(1).parent = doc0.xs1.get(0);
    doc0.xs1.get(2).parent = doc0.xs1.get(1);
    doc0.xs2.get(1).parent = doc0.xs2.get(0);

    doc0.xs1.get(0).other = doc0.xs2.get(0);
    doc0.xs1.get(1).other = doc0.xs2.get(0);
    doc0.xs1.get(2).other = doc0.xs2.get(0);
    doc0.xs2.get(0).other = doc0.xs2.get(0);
    doc0.xs2.get(1).other = doc0.xs2.get(0);

    final byte[] correct = {
      (byte)0x02,
      (byte)0x92,  // <klasses>: 2-element array
      (byte)0x92,  // <klass>: 2-element array
      (byte)0xa8, '_', '_', 'm', 'e', 't', 'a', '_', '_',  // <klass_name>: 8-bytes of utf-8 encoded "__meta__"
      (byte)0x90,  // <fields>: 0-element array
      (byte)0x92,  // <klass>: 2-element array
      (byte)0xa1, 'X',  // <klass_name>: 8-bytes of utf-8 encoded "X"
      (byte)0x92,  // <fields>: 2-element array
      (byte)0x82,  // <field>: 2-element map
      (byte)0x00, (byte)0xa6, 'p', 'a', 'r', 'e', 'n', 't',
      (byte)0x03, (byte)0xc0,
      (byte)0x82,  // <field>: 2-element map
      (byte)0x00, (byte)0xa5, 'o', 't', 'h', 'e', 'r',
      (byte)0x01, (byte)0x01,
      (byte)0x92,  // <stores>: 2-element array
      (byte)0x93, (byte)0xa3, 'x', 's', '1', (byte)0x01, (byte)0x03,
      (byte)0x93, (byte)0xa3, 'x', 's', '2', (byte)0x01, (byte)0x02,
      (byte)0x01,  // <instance_nbytes>: 1 byte after this
      (byte)0x80,  // <instance>: 0-element map
      (byte)0x0e,  // <instance_nbytes>: 1 byte after this
      (byte)0x93,  // <instance>: 3-element array
      (byte)0x81, (byte)0x01, (byte)0x00,
      (byte)0x82, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00,
      (byte)0x82, (byte)0x00, (byte)0x01, (byte)0x01, (byte)0x00,
      (byte)0x09,  // <instance_nbytes>: 1 byte after this
      (byte)0x92,  // <instance>: 2-element array
      (byte)0x81, (byte)0x01, (byte)0x00,
      (byte)0x82, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00,
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

    Assert.assertEquals(3, doc1.xs1.size());
    Assert.assertEquals(2, doc1.xs2.size());
    Assert.assertNull(doc1.xs1.get(0).parent);
    Assert.assertSame(doc1.xs2.get(0), doc1.xs1.get(0).other);
    Assert.assertSame(doc1.xs1.get(0), doc1.xs1.get(1).parent);
    Assert.assertSame(doc1.xs2.get(0), doc1.xs1.get(1).other);
    Assert.assertSame(doc1.xs1.get(1), doc1.xs1.get(2).parent);
    Assert.assertSame(doc1.xs2.get(0), doc1.xs1.get(2).other);
    Assert.assertNull(doc1.xs2.get(0).parent);
    Assert.assertSame(doc1.xs2.get(0), doc1.xs2.get(0).other);
    Assert.assertSame(doc1.xs2.get(0), doc1.xs2.get(1).parent);
    Assert.assertSame(doc1.xs2.get(0), doc1.xs2.get(1).other);
  }
}
