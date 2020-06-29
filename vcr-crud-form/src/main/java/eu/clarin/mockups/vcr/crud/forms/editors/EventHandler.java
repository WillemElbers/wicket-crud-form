package eu.clarin.mockups.vcr.crud.forms.editors;

import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 *
 * @author wilelb
 */
public interface EventHandler<T> {
    public void handleEditEvent(T object, AjaxRequestTarget target);
    public void handleRemoveEvent(T object, AjaxRequestTarget target);
}
