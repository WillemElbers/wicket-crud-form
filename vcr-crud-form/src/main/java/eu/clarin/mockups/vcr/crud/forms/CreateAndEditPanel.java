package eu.clarin.mockups.vcr.crud.forms;

import eu.clarin.mockups.vcr.crud.form.ActionablePanel;
import eu.clarin.mockups.vcr.crud.form.Event;
import eu.clarin.mockups.vcr.crud.form.EventType;
import eu.clarin.mockups.vcr.crud.forms.editors.authors.AuthorsEditor;
import eu.clarin.mockups.vcr.crud.forms.editors.references.ReferencesEditor;
import eu.clarin.mockups.vcr.crud.forms.fields.VcrTextField;
import eu.clarin.mockups.vcr.crud.forms.fields.VcrChoiceField;
import eu.clarin.mockups.vcr.crud.form.pojo.VirtualCollection;
import java.util.Arrays;
import java.util.UUID;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Form to create or edit a virtual collection
 * 
 * @author wilelb
 */
public class CreateAndEditPanel extends ActionablePanel {
    
    private final static Logger logger = LoggerFactory.getLogger(CreateAndEditPanel.class);

    //Keep track of the original collection, used to detect changes and reset the
    //form
    private VirtualCollection originalCollection;
    
    private final IModel<String> nameModel = Model.of("");
    private final IModel<String> typeModel = Model.of("");
    private final AuthorsEditor authorsEditor;
    private final ReferencesEditor referencesEditor;
         
    /**
     * Create a new virtual collection
     * @param id 
     */
    public CreateAndEditPanel(String id) {
        this(id, null);
    }
    
    /**
     * Edit the supplied virtual collection or create a new virtual collection if
     * the supplied collection is null
     * 
     * @param id
     * @param collection 
     */
    public CreateAndEditPanel(String id, VirtualCollection collection) {
        super(id);
        this.setOutputMarkupId(true);
        
        final Component ajax_update_component = this;
        
        add(new VcrTextField("name", "Name:", "", nameModel));
        add(new VcrChoiceField("type", "Type:", Arrays.asList(new String[]{"A", "B", "C"}), "", typeModel, null));
        this.authorsEditor = new AuthorsEditor("authors");
        add(authorsEditor);
        this.referencesEditor = new ReferencesEditor("references"); 
        add(referencesEditor);
        add(new AjaxFallbackLink("btn_save") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                if(validate()) {
                    persist(target);
                    reset();
                } else {
                    logger.info("Failed to validate");
                }
                
                if (target != null) {
                    target.add(ajax_update_component);
                }
            }
        });
        add(new AjaxFallbackLink("btn_cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                logger.info("Cancel button clicked");
                reset();
                if (target != null) {
                    target.add(ajax_update_component);
                }
            }
        });
    }
    
    public void editCollection(VirtualCollection c) {
        this.originalCollection = c; //TODO: deep clone?
        nameModel.setObject(c.getName());
        typeModel.setObject(c.getType());
        authorsEditor.setData(c.getAuthors());
        referencesEditor.setData(c.getReferences());
    }
    
    private boolean validate() {
        return true;
    }
    
    private void persist(final AjaxRequestTarget target) {
        final VirtualCollection newCollection = new VirtualCollection();
        if(this.originalCollection != null && this.originalCollection.getId() != null && !this.originalCollection.getId().isEmpty()) {
            newCollection.setId(this.originalCollection.getId());
        } else {
            newCollection.setId(UUID.randomUUID().toString());
        }
        newCollection.setName(nameModel.getObject());
        newCollection.setType(typeModel.getObject());
        newCollection.setAuthors(authorsEditor.getData());
        newCollection.setReferences(referencesEditor.getData());
        
        fireEvent(new Event<VirtualCollection>() {
            @Override
            public EventType getType() {
                return EventType.SAVE;
            }
            @Override
            public VirtualCollection getData() {
                return newCollection;
            }
            @Override
            public AjaxRequestTarget getAjaxRequestTarget() {
                return target;
            }
        });
    }
    
    private void reset() {
        nameModel.setObject("");
        typeModel.setObject("");
        authorsEditor.reset();
        referencesEditor.reset();
    }
    
}
