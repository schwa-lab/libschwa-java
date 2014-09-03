package org.schwa.dr;


/**
 * Exception class for errors that occur during reading from a docrep stream.
 *
 * @author Tim Dawborn
 **/
public class ReaderException extends DocrepException {
  public ReaderException(String msg) {
    super(msg);
  }

  public ReaderException(Throwable e) {
    super(e);
  }
}
