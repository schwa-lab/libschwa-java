package org.schwa.dr;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;


public class StoreSchemaTest {

  @Test
  public void testStores() {
    @dr.Ann
    class A extends AbstractAnn {
    }

    @dr.Doc
    class D extends AbstractDoc {
      @dr.Store               public Store<A> storeOfAs;
      @dr.Store(serial="foo") public Store<A> anotherStoreOfAs;
    }

    final DocSchema d = DocSchema.create(D.class);
    Assert.assertEquals(0, d.getFields().size());
    Assert.assertEquals(2, d.getSchemas().size());
    Assert.assertEquals(2, d.getStores().size());

    final List<StoreSchema> schemas = d.getStores();
    StoreSchema s;

    s = schemas.get(0);
    Assert.assertEquals("storeOfAs", s.getName());
    Assert.assertEquals("storeOfAs", s.getSerial());
    Assert.assertEquals(A.class, s.getStoredKlass());

    s = schemas.get(1);
    Assert.assertEquals("anotherStoreOfAs", s.getName());
    Assert.assertEquals("foo", s.getSerial());
    Assert.assertEquals(A.class, s.getStoredKlass());
  }
}
