package org.schwa.dr;

import org.schwa.dr.runtime.RTManager;


/**
 * Base interface for all docrep document classes. The methods provided here are mainly useful
 * within this package, providing internal methods for implementing laziness and other docrep
 * things.
 *
 * @author Tim Dawborn
 */
public interface Doc extends Ann {
  /**
   * Returns the runtime manager for this document. If this document instance was newly constructed
   * instead of being read in via a {@link Reader}, this method will return null.
   **/
  public RTManager getDRRT();

  /**
   * Sets the runtime manager for this document.
   **/
  public void setDRRT(RTManager rt);
}
