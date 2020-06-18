package eu.clarin.mockups.vcr.crud.forms;

import eu.clarin.mockups.vcr.crud.form.pojo.Author;
import eu.clarin.mockups.vcr.crud.form.pojo.Reference;
import eu.clarin.mockups.vcr.crud.forms.editors.AuthorsEditor;
import eu.clarin.mockups.vcr.crud.forms.editors.ReferencesEditor;
import eu.clarin.mockups.vcr.crud.forms.fields.VcrTextField;
import eu.clarin.mockups.vcr.crud.forms.fields.VcrChoiceField;
import eu.clarin.mockups.vcr.crud.form.pojo.VirtualCollection;
import java.util.Arrays;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Form to create or edit a virtual collection
 * 
 * @author wilelb
 */
public class CreateAndEditPanel extends Panel {
    
    private final static Logger logger = LoggerFactory.getLogger(AuthorsEditor.class);

    //Keep track of the original collection, used to detect changes and reset the
    //form
    private final VirtualCollection originalCollection;
    
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
        this.originalCollection = collection; //TODO: deep clone?
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
                    persist();
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
    
    private boolean validate() {
        return true;
    }
    
    private void persist() {
        logger.info("Save button clicked");
        logger.info("Name: {}", nameModel.getObject());
        logger.info("Type: {}", typeModel.getObject());
        logger.info("Authors:");
        for(Author a : authorsEditor.getData()) {
            logger.info("\t{}, {}, {}", a.getName(), a.getEmail(), a.getAffiliation());
        }
        for(Reference r : referencesEditor.getData()) {
            logger.info("\t{}, {}, {}, {}", r.getUrl(), r.getType(), r.getTitle(), r.getDescription());
        }
    }
    
    private void reset() {
        nameModel.setObject("");
        typeModel.setObject("");
        authorsEditor.reset();
        referencesEditor.reset();
    }
    
}
