package org.schwa.dr;


/**
 * Base class for all docrep exceptions.
 *
 * @author Tim Dawborn
 **/
public class DocrepException extends RuntimeException {
  public DocrepException(String msg) {
    super(msg);
  }

  public DocrepException(Throwable e) {
    super(e);
  }
}
