package org.schwa.dr;


public class WriterException extends DocrepException {
  public WriterException(String msg) {
    super(msg);
  }

  public WriterException(Throwable e) {
    super(e);
  }
}
