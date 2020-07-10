package eu.clarin.mockups.vcr.crud.form;

import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 *
 * @author wilelb
 */
public interface Event<T> {
    public EventType getType();
    public T getData();
    public AjaxRequestTarget getAjaxRequestTarget();
}
