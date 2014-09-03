package org.schwa.dr;


/**
 * <p>
 * A slice over bytes in the docrep Java API. This was chosen to not be a subclass of {@link Slice}
 * so that we can guarentee byte values will always fit into the wrapped data type (a long in the
 * case of Java).
 * </p>
 * <p>
 * Slices in the Java API are <code>[inclusive, inclusive]</code> unlike the C++ and Python APIs,
 * which are <code>[inclusive, exclusive)</code>.
 * </p>
 *
 * @author Tim Dawborn
 **/
public class ByteSlice {
  /** The inclusive starting point of the slice. **/
  public long start;
  /** The inclusive stopping point of the slice. **/
  public long stop;

  public ByteSlice() {
    this(-1, -1);
  }

  public ByteSlice(long start, long stop) {
    this.start = start;
    this.stop = stop;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    else if (o == null)
      return false;
    else if (!(o instanceof ByteSlice))
      return false;
    final ByteSlice s = (ByteSlice) o;
    return s.start == start && s.stop == stop;
  }

  @Override
  public int hashCode() {
    return (int)(31*start + stop);
  }

  public long getStart() {
    return start;
  }

  public long getStop() {
    return stop;
  }

  public void setStart(final long start) {
    this.start = start;
  }

  public void setStop(final long stop) {
    this.stop = stop;
  }
}
