package org.schwa.dr;


/**
 * Exception class for errors that occur during writing to a docrep stream.
 *
 * @author Tim Dawborn
 **/
public class WriterException extends DocrepException {
  public WriterException(String msg) {
    super(msg);
  }

  public WriterException(Throwable e) {
    super(e);
  }
}
