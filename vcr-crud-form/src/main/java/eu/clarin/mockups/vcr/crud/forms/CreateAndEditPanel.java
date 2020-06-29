package eu.clarin.mockups.vcr.crud.forms;

import eu.clarin.mockups.vcr.crud.form.pojo.Author;
import eu.clarin.mockups.vcr.crud.form.pojo.Reference;
import eu.clarin.mockups.vcr.crud.forms.editors.AuthorsEditor;
import eu.clarin.mockups.vcr.crud.forms.editors.ReferencesEditor;
import eu.clarin.mockups.vcr.crud.forms.fields.VcrTextField;
import eu.clarin.mockups.vcr.crud.forms.fields.VcrChoiceField;
import eu.clarin.mockups.vcr.crud.form.pojo.VirtualCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
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
        
    private final List<VirtualCollection> collections = new ArrayList<>();
    
    public class CollectionPanel extends Panel {
        public CollectionPanel(String id, VirtualCollection collection) {
            super(id);
            
            add(new Label("title", collection.getName()));
            add(new Label("type", collection.getType()));
            
             

            ListView authorsListview = new ListView("authors_list", collection.getAuthors()) {
                @Override
                protected void populateItem(ListItem item) {
                    Author a = (Author)item.getModel().getObject();
                    WebMarkupContainer wrapper = new WebMarkupContainer("wrapper1");
                    wrapper.add(new Label("name", a.getName()));
                    wrapper.add(new Label("email", a.getEmail()));
                    wrapper.add(new Label("affiliation", a.getAffiliation()));                
                    item.add(wrapper);
                }
            };

            add(authorsListview);
         
             ListView referencesListview = new ListView("references_list", collection.getReferences()) {
                @Override
                protected void populateItem(ListItem item) {
                    Reference r = (Reference)item.getModel().getObject();
                    WebMarkupContainer wrapper = new WebMarkupContainer("wrapper2");
                    ExternalLink link = new ExternalLink("ref_link", r.getValue());
                    link.add(new Label("title", r.getTitle()));
                    wrapper.add(link);
                    wrapper.add(new Label("description", r.getDescription()));
                    item.add(wrapper);
                }
            };

            add(referencesListview);
        }
    }
    
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
        
        
        WebMarkupContainer ajaxWrapper = new WebMarkupContainer("ajaxwrapper");
        ajaxWrapper.setOutputMarkupId(true);
        ListView listview = new ListView("listview", collections) {
            @Override
            protected void populateItem(ListItem item) {
                item.add(new CollectionPanel("pnl_collection", (VirtualCollection)item.getModel().getObject()));
            }
        };
        ajaxWrapper.add(listview);
        add(ajaxWrapper);
    }
    
    private boolean validate() {
        return true;
    }
    
    private void persist() {
        logger.info("Save button clicked");
        VirtualCollection newCollection = new VirtualCollection();
        newCollection.setName(nameModel.getObject());
        newCollection.setType(typeModel.getObject());
        newCollection.setAuthors(authorsEditor.getData());
        newCollection.setReferences(referencesEditor.getData());
        collections.add(newCollection);
    }
    
    private void reset() {
        nameModel.setObject("");
        typeModel.setObject("");
        authorsEditor.reset();
        referencesEditor.reset();
    }
    
}
