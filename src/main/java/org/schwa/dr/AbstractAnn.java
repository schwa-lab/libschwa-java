package org.schwa.dr;


/**
 * Abstract base class for the {@link Ann} interface.
 *
 * @author Tim Dawborn
 **/
public abstract class AbstractAnn implements Ann {
  /** The index of this annotation instance within a {@link Store}. **/
  protected Integer drIndex;
  /** The lazy serialised data for this annotation instance. **/
  protected byte[] drLazy;
  /** The number of fields that are lazily stored for this annotation instance. **/
  protected int drLazyNElem;


  protected AbstractAnn() { }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    else if (o == null)
      return false;
    else if (!(o instanceof AbstractAnn))
      return false;
    final AbstractAnn other = (AbstractAnn) o;
    if (this.drIndex == null || other.drIndex == null)
      return false;
    return this.drIndex == other.drIndex;
  }

  @Override
  public final Integer getDRIndex() {
    return drIndex;
  }

  @Override
  public final byte[] getDRLazy() {
    return drLazy;
  }

  @Override
  public final int getDRLazyNElem() {
    return drLazyNElem;
  }

  @Override
  public final void setDRIndex(Integer index) {
    drIndex = index;
  }

  @Override
  public final void setDRLazy(byte[] drLazy) {
    this.drLazy = drLazy;
  }

  @Override
  public final void setDRLazyNElem(int drLazyNElem) {
    this.drLazyNElem = drLazyNElem;
  }
}
