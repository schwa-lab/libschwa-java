package org.schwa.dr;


public class ReaderException extends DocrepException {
  public ReaderException(String msg) {
    super(msg);
  }

  public ReaderException(Throwable e) {
    super(e);
  }
}
