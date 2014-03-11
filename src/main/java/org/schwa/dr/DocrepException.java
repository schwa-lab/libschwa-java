package org.schwa.dr;


public class DocrepException extends RuntimeException {
  public DocrepException(String msg) {
    super(msg);
  }

  public DocrepException(Throwable e) {
    super(e);
  }
}
