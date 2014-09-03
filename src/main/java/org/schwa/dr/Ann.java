package org.schwa.dr;


/**
 * Base interface for all annotation types. The methods provided here are mainly usedful within
 * this package, providing internal methods for implementing laziness and other docrep things.
 *
 * @author Tim Dawborn
 */
public interface Ann {
  /**
   * If this annotation instance is stored within a {@link Store}, this method returns the index of
   * this annotation within the store. If this annotation does not belong to any store, this method
   * returns null.
   **/
  public Integer getDRIndex();

  /**
   * If this annotation has lazily stored attribute data, returns the lazy data as an array of
   * bytes ready for serialisation. If not, this method returns null.
   **/
  public byte[] getDRLazy();

  /**
   * If this annotaiton has lazily stored attribute data, returns the number of lazily stored
   * fields. If not, this method returns zero.
   **/
  public int getDRLazyNElem();

  /**
   * Sets the index of this annotation instance within its containing store.
   **/
  public void setDRIndex(Integer index);

  /**
   * Sets the lazily stored attribute data for this annotation instance.
   **/
  public void setDRLazy(byte[] lazy);

  /**
   * Sets the number of lazily stored fields for this annotation instance.
   **/
  public void setDRLazyNElem(int lazyNElem);
}
