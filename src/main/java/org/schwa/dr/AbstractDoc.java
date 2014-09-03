package org.schwa.dr;

import org.schwa.dr.runtime.RTManager;


/**
 * Abstract base class for the {@link Doc} interface.
 *
 * @author Tim Dawborn
 **/
public abstract class AbstractDoc extends AbstractAnn implements Doc {
  /** The runtime manager for this document. **/
  protected RTManager drRT;

  @Override
  public final RTManager getDRRT() {
    return drRT;
  }

  @Override
  public final void setDRRT(RTManager drRT) {
    this.drRT = drRT;
  }
}
