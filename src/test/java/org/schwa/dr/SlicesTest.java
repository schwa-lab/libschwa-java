package org.schwa.dr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;


public class SlicesTest {
  @dr.Ann
  public static class Token extends AbstractAnn {
    @dr.Field public ByteSlice span;
    @dr.Field public String raw;
  }

  @dr.Ann
  public static class Sent extends AbstractAnn {
    @dr.Pointer(store="tokens") public Slice<Token> span;
    @dr.Field                   public int number;
  }

  @dr.Doc
  public static class Doc extends AbstractDoc {
    @dr.Store public Store<Token> tokens = new Store<Token>();
    @dr.Store public Store<Sent> sents = new Store<Sent>();
  }


  @Test
  public void test_slices() throws IOException {
    final Doc doc0 = new Doc();

    doc0.tokens.create(Token.class, 10);
    doc0.tokens.get(0).span = new ByteSlice(0, 3);
    doc0.tokens.get(0).raw = "The";
    doc0.tokens.get(1).span = new ByteSlice(4, 9);
    doc0.tokens.get(1).raw = "quick";
    doc0.tokens.get(2).span = new ByteSlice(11, 16);
    doc0.tokens.get(2).raw = "brown";
    doc0.tokens.get(3).span = new ByteSlice(17, 20);
    doc0.tokens.get(3).raw = "fox";
    doc0.tokens.get(4).span = new ByteSlice(20, 21);
    doc0.tokens.get(4).raw = ".";
    doc0.tokens.get(5).span = new ByteSlice(22, 25);
    doc0.tokens.get(5).raw = "The";
    doc0.tokens.get(6).span = new ByteSlice(26, 30);
    doc0.tokens.get(6).raw = "lazy";
    doc0.tokens.get(7).span = new ByteSlice(31, 34);
    doc0.tokens.get(7).raw = "cat";
    doc0.tokens.get(8).span = new ByteSlice(35, 38);
    doc0.tokens.get(8).raw = "too";
    doc0.tokens.get(9).span = new ByteSlice(38, 39);
    doc0.tokens.get(9).raw = ".";

    doc0.sents.create(Sent.class, 2);
    doc0.sents.get(0).span = new Slice(doc0.tokens.get(0), doc0.tokens.get(4));
    doc0.sents.get(1).span = new Slice(doc0.tokens.get(5), doc0.tokens.get(9));

    ByteArrayOutputStream actual = new ByteArrayOutputStream();
    DocSchema schema = DocSchema.create(Doc.class);
    Writer writer = new Writer(actual, schema);
    writer.write(doc0);

    final byte[] correct = {
      (byte)0x02,
      (byte)0x93,
        (byte)0x92,
          (byte)0xa8, '_', '_', 'm', 'e', 't', 'a', '_', '_',
          (byte)0x90,
        (byte)0x92,
          (byte)0xa5, 'T', 'o', 'k', 'e', 'n',
          (byte)0x92,
            (byte)0x82, (byte)0x00, (byte)0xa4, 's', 'p', 'a', 'n', (byte)0x02, (byte)0xc0,
            (byte)0x81, (byte)0x00, (byte)0xa3, 'r', 'a', 'w',
        (byte)0x92,
          (byte)0xa4, 'S', 'e', 'n', 't',
          (byte)0x92,
            (byte)0x83, (byte)0x00, (byte)0xa4, 's', 'p', 'a', 'n', (byte)0x01, (byte)0x00, (byte)0x02, (byte)0xc0,
            (byte)0x81, (byte)0x00, (byte)0xa6, 'n', 'u', 'm', 'b', 'e', 'r',
      (byte)0x92,
        (byte)0x93, (byte)0xa6, 't', 'o', 'k', 'e', 'n', 's', (byte)0x01, (byte)0x0a,
        (byte)0x93, (byte)0xa5, 's', 'e', 'n', 't', 's', (byte)0x02, (byte)0x02,
      (byte)0x01,
        (byte)0x80,
      (byte)0x66,
        (byte)0x9a,
          (byte)0x82, (byte)0x00, (byte)0x92, (byte)0x00, (byte)0x03, (byte)0x01, (byte)0xa3, 'T', 'h', 'e',
          (byte)0x82, (byte)0x00, (byte)0x92, (byte)0x04, (byte)0x05, (byte)0x01, (byte)0xa5, 'q', 'u', 'i', 'c', 'k',
          (byte)0x82, (byte)0x00, (byte)0x92, (byte)0x0b, (byte)0x05, (byte)0x01, (byte)0xa5, 'b', 'r', 'o', 'w', 'n',
          (byte)0x82, (byte)0x00, (byte)0x92, (byte)0x11, (byte)0x03, (byte)0x01, (byte)0xa3, 'f', 'o', 'x',
          (byte)0x82, (byte)0x00, (byte)0x92, (byte)0x14, (byte)0x01, (byte)0x01, (byte)0xa1,  '.',
          (byte)0x82, (byte)0x00, (byte)0x92, (byte)0x16, (byte)0x03, (byte)0x01, (byte)0xa3, 'T', 'h', 'e',
          (byte)0x82, (byte)0x00, (byte)0x92, (byte)0x1a, (byte)0x04, (byte)0x01, (byte)0xa4, 'l', 'a', 'z', 'y',
          (byte)0x82, (byte)0x00, (byte)0x92, (byte)0x1f, (byte)0x03, (byte)0x01, (byte)0xa3, 'c', 'a', 't',
          (byte)0x82, (byte)0x00, (byte)0x92, (byte)0x23, (byte)0x03, (byte)0x01, (byte)0xa3, 't', 'o', 'o',
          (byte)0x82, (byte)0x00, (byte)0x92, (byte)0x26, (byte)0x01, (byte)0x01, (byte)0xa1, '.',
      (byte)0x0f,
        (byte)0x92,
          (byte)0x82, (byte)0x00, (byte)0x92, (byte)0x00, (byte)0x05, (byte)0x01, (byte)0x00,
          (byte)0x82, (byte)0x00, (byte)0x92, (byte)0x05, (byte)0x05, (byte)0x01, (byte)0x00,
    };

    Utils.assertArrayEquals(correct, actual.toByteArray());

    Reader reader = new Reader(new ByteArrayInputStream(correct), schema);
    Iterator<Doc> iterator = reader.iterator();
    Assert.assertTrue(iterator.hasNext());
    final Doc doc1 = iterator.next();

    Assert.assertEquals(10, doc1.tokens.size());
    Assert.assertEquals(2, doc1.sents.size());

    Assert.assertEquals("The", doc1.tokens.get(0).raw);
    Assert.assertEquals(doc1.tokens.get(0).span, new ByteSlice(0, 3));
    Assert.assertEquals("quick", doc1.tokens.get(1).raw);
    Assert.assertEquals(doc1.tokens.get(1).span, new ByteSlice(4, 9));
    Assert.assertEquals("brown", doc1.tokens.get(2).raw);
    Assert.assertEquals(doc1.tokens.get(2).span, new ByteSlice(11, 16));
    Assert.assertEquals("fox", doc1.tokens.get(3).raw);
    Assert.assertEquals(doc1.tokens.get(3).span, new ByteSlice(17, 20));
    Assert.assertEquals(".", doc1.tokens.get(4).raw);
    Assert.assertEquals(doc1.tokens.get(4).span, new ByteSlice(20, 21));
    Assert.assertEquals("The", doc1.tokens.get(5).raw);
    Assert.assertEquals(doc1.tokens.get(5).span, new ByteSlice(22, 25));
    Assert.assertEquals("lazy", doc1.tokens.get(6).raw);
    Assert.assertEquals(doc1.tokens.get(6).span, new ByteSlice(26, 30));
    Assert.assertEquals("cat", doc1.tokens.get(7).raw);
    Assert.assertEquals(doc1.tokens.get(7).span, new ByteSlice(31, 34));
    Assert.assertEquals("too", doc1.tokens.get(8).raw);
    Assert.assertEquals(doc1.tokens.get(8).span, new ByteSlice(35, 38));
    Assert.assertEquals(".", doc1.tokens.get(9).raw);
    Assert.assertEquals(doc1.tokens.get(9).span, new ByteSlice(38, 39));
  }
}
