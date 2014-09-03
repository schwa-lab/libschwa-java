package org.schwa.dr;


/**
 * Exception class to indicate that a docrep annotation has been used illegally.
 *
 * @author Tim Dawborn
 **/
public class IllegalAnnotationException extends DocrepException {
  public IllegalAnnotationException(String msg) {
    super(msg);
  }
}
