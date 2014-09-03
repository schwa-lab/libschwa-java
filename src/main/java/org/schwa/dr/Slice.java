package org.schwa.dr;


/**
 * <p>
 * A slice over other {@link Ann} instances in the docrep Java API.
 * </p>
 * <p>
 * Slices in the Java API are <code>[inclusive, inclusive]</code> unlike the C++ and Python APIs,
 * which are <code>[inclusive, exclusive)</code>.
 * </p>
 *
 * @author Tim Dawborn
 **/
public class Slice<T extends Ann> {
  /** The inclusive starting point of the slice. **/
  public T start;
  /** The inclusive stopping point of the slice. **/
  public T stop;

  public Slice() { }

  public Slice(T start, T stop) {
    this.start = start;
    this.stop = stop;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    else if (o == null)
      return false;
    else if (!(o instanceof Slice))
      return false;
    final Slice<T> s = (Slice<T>) o;
    return s.start.equals(start) && s.stop.equals(stop);
  }

  @Override
  public int hashCode() {
    return 31*start.hashCode() + stop.hashCode();
  }

  public T getStart() {
    return start;
  }

  public T getStop() {
    return stop;
  }

  public void setStart(final T start) {
    this.start = start;
  }

  public void setStop(final T stop) {
    this.stop = stop;
  }
}
