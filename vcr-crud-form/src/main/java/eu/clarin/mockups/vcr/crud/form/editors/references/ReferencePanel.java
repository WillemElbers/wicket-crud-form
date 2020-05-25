package eu.clarin.mockups.vcr.crud.form.editors.references;

import eu.clarin.mockups.vcr.crud.form.editors.ReferencesEditor;
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
        add(new TextField("title", Model.of("")));
        add(new TextField("description", Model.of("")));
    }
}
