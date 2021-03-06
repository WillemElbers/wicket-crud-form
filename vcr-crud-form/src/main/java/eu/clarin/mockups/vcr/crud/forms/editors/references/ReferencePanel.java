package eu.clarin.mockups.vcr.crud.forms.editors.references;

import eu.clarin.mockups.vcr.crud.forms.editors.Decorator;
import eu.clarin.mockups.vcr.crud.forms.editors.EventHandler;
import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 *
 * @author wilelb
 */
public class ReferencePanel extends Panel {
    private static Logger logger = LoggerFactory.getLogger(ReferencePanel.class);
    
    private transient List<EventHandler> eventHandlers = new ArrayList<>();
    
    /**
     * 
     * @param id    The wicket component id
     * @param ref 
     */
    public ReferencePanel(String id, final ReferencesEditor.ReferenceJob ref) {
        super(id);

        Model titleModel = Model.of("");
        if(ref.getReference().getTitle() != null) {
            titleModel.setObject(ref.getReference().getTitle());
        }
        Model descriptionModel = Model.of("");
        if(ref.getReference().getDescription() != null) {
            descriptionModel.setObject(ref.getReference().getDescription());
        }
        
        WebMarkupContainer editorWrapper = new WebMarkupContainer("wrapper");
        
        editorWrapper.add(new Label("state", ref.getState()));
        editorWrapper.add(new Label("value", ref.getReference().getValue()));
        editorWrapper.add(new Label("check", ref.getReference().getCheck()));
        editorWrapper.add(new Label("type", ref.getReference().getType()));
        editorWrapper.add(new Label("title", titleModel));
        editorWrapper.add(new Label("description", descriptionModel));
        AjaxFallbackLink btnEdit = new AjaxFallbackLink("btn_edit") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                for(EventHandler handler : eventHandlers) {
                    handler.handleEditEvent(ref.getReference(), target);
                }
            }
        };
        btnEdit.setEnabled(ref.getState() == ReferencesEditor.State.DONE);
        editorWrapper.add(btnEdit);
        
        AjaxFallbackLink btnRemove = new AjaxFallbackLink("btn_remove") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                for(EventHandler handler : eventHandlers) {
                    handler.handleRemoveEvent(ref.getReference(), target);
                }
            }
        };
        btnRemove.setEnabled(ref.getState() == ReferencesEditor.State.DONE);
        editorWrapper.add(btnRemove);
        
        add(editorWrapper);
    }
    
    public void addEventHandler(EventHandler handler) {
        this.eventHandlers.add(handler);
    }
}
