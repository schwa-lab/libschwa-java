package org.schwa.dr;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


import org.junit.Test;

/**
 * Simple docrep write/read test
 */
public class ReadWriteTest {

	@dr.Ann
	public static class Token extends AbstractAnn {

		@dr.Field
		public int start;

		@dr.Field
		public int end;

		@dr.Field
		public String test;

		public Token() {
			super();
		}

	}

	@dr.Doc
	public static class Document extends AbstractDoc {

		@dr.Field
		public String text;

		@dr.Store
		public Store<Token> tokens;

		public Document() {
			super();
		}

	}

	@Test
	public void test() throws IOException {
		DocSchema docSchema = DocSchema.create(Document.class);
		Document doc = new Document();
		doc.text = "This is a test.";
		doc.tokens = new Store<Token>();
		Token tok = new Token();
		tok.start = 0;
		tok.end = 4;
		tok.test = "test";
		doc.tokens.add(tok);
		tok = new Token();
		tok.start = 5;
		tok.end = 7;
		doc.tokens.add(tok);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Writer writer = new Writer(os, docSchema);
		writer.write(doc);
		os.close();

		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		Reader<Document> reader = new Reader<Document>(is, docSchema);
		Document in = reader.next();
		assertEquals(in.tokens.size(), 2);
	}

}
