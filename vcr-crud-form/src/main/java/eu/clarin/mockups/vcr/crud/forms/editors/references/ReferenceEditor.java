package eu.clarin.mockups.vcr.crud.forms.editors.references;

import eu.clarin.mockups.vcr.crud.form.pojo.Reference;
import eu.clarin.mockups.vcr.crud.forms.editors.CancelEventHandler;
import eu.clarin.mockups.vcr.crud.forms.editors.SaveEventHandler;
import eu.clarin.mockups.vcr.crud.forms.fields.VcrTextArea;
import eu.clarin.mockups.vcr.crud.forms.fields.VcrTextField;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class ReferenceEditor extends Panel {
    private final static Logger logger = LoggerFactory.getLogger(ReferenceEditor.class);
    
    private final IModel<String> urlModel = Model.of("");
    private final IModel<String> titleModel = Model.of("");
    private final IModel<String> descriptionModel = Model.of("");
    
    private final Component componentToUpdate;
    
    private Reference data;
    
    public ReferenceEditor(String id, final Component componentToUpdate, final SaveEventHandler saveEventHandler, final CancelEventHandler cancelEventHandler) {
        super(id); 
        final Component _this = this;
        this.componentToUpdate = componentToUpdate;
        
        add(new Label("url", urlModel));
        
        WebMarkupContainer wrapper = new WebMarkupContainer("wrapper");
        
        VcrTextField tf = new VcrTextField("title", "Title:", "", titleModel);//, this);
        tf.setCompleteSubmitOnUpdate(true);         
        wrapper.add(tf);
        
        VcrTextArea ta = new VcrTextArea("description", "Description:", "", descriptionModel);//, this);
        ta.setCompleteSubmitOnUpdate(true); 
        wrapper.add(ta);
        
        wrapper.add(new AjaxFallbackLink("save") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                data.setTitle(titleModel.getObject());
                data.setDescription( descriptionModel.getObject());
                reset();
                saveEventHandler.handleSaveEvent();
                if (target != null) {
                    target.add(componentToUpdate);
                }
            }
        });
        wrapper.add(new AjaxFallbackLink("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                reset();
                cancelEventHandler.handleCancelEvent();
                if (target != null) {
                    target.add(componentToUpdate);
                }
            }
        });
        
        add(wrapper);
    }
    
    public void setReference(Reference ref) {
        data = ref;
        urlModel.setObject(ref.getValue());
        titleModel.setObject(ref.getTitle());
        descriptionModel.setObject(ref.getDescription());
    }

    public void reset() {
        urlModel.setObject("");
        titleModel.setObject("");
        descriptionModel.setObject("");
    }
}
