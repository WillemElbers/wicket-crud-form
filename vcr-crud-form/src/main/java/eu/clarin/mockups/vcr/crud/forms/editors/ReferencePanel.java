package eu.clarin.mockups.vcr.crud.forms.editors;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

/** 
 *
 * @author wilelb
 */
public class ReferencePanel extends Panel {
    public ReferencePanel(String id, ReferencesEditor.ReferenceJob ref) {
        super(id);
        add(new Label("state", ref.getState()));
        add(new Label("value", ref.getReference().getValue()));
        add(new Label("check", ref.getReference().getCheck()));
        add(new Label("type", ref.getReference().getType()));
        
        Model titleModel = Model.of("");
        if(ref.getReference().getTitle() != null) {
            titleModel.setObject(ref.getReference().getTitle());
        }
        add(new TextField("title", titleModel));
        
        Model descriptionModel = Model.of("");
        if(ref.getReference().getDescription() != null) {
            descriptionModel.setObject(ref.getReference().getDescription());
        }
        add(new TextField("description", descriptionModel));
    }
}
