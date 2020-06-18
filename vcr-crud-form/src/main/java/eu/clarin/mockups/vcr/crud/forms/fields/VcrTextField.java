package eu.clarin.mockups.vcr.crud.forms.fields;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

/**
 *
 * @author wilelb
 */
public class VcrTextField extends Field {
       
        public VcrTextField(String id, String label, String defaultValue, final IModel dataModel) {
            this(id, label, defaultValue, dataModel, null);
        }
        
        public VcrTextField(String id, String label, String defaultValue, final IModel dataModel, final FieldComposition parent) {
            super(id, label, defaultValue, dataModel, parent, new TextField("input_textfield", dataModel));
            if(defaultValue != null) {
                editComponent.add(new AttributeModifier("placeholder", defaultValue));
            }
        }
}
