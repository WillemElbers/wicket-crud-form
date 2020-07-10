package eu.clarin.mockups.vcr.crud.form;

import java.io.Serializable;

/**
 *
 * @author wilelb
 */
public interface Listener<T> extends Serializable {
    public void handleEvent(Event<T> event);
}
