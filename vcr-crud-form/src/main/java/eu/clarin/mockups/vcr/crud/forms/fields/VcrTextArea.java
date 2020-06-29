package eu.clarin.mockups.vcr.crud.forms.fields;

import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;

/**
 *
 * @author wilelb
 */
public class VcrTextArea extends Field {   
    public VcrTextArea(String id, String label, String defaultValue, final IModel dataModel) {
        this(id, label, defaultValue, dataModel, null);
    }

    public VcrTextArea(String id, String label, String defaultValue, final IModel dataModel, final FieldComposition parent) {
        super(id, label, defaultValue, dataModel, parent, new TextArea("input", dataModel));
    }
}
