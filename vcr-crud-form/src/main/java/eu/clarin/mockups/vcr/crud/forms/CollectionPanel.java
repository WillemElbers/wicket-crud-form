package eu.clarin.mockups.vcr.crud.forms;

import eu.clarin.mockups.vcr.crud.form.ActionablePanel;
import eu.clarin.mockups.vcr.crud.form.Event;
import eu.clarin.mockups.vcr.crud.form.EventType;
import eu.clarin.mockups.vcr.crud.form.pojo.Author;
import eu.clarin.mockups.vcr.crud.form.pojo.Reference;
import eu.clarin.mockups.vcr.crud.form.pojo.VirtualCollection;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

/**
 *
 * @author wilelb
 */
public class CollectionPanel extends ActionablePanel {
        
    public CollectionPanel(String id, final VirtualCollection collection) {
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
        
        add(new AjaxFallbackLink("btn_edit") {
            @Override
            public void onClick(final AjaxRequestTarget target) {
                fireEvent(new Event<VirtualCollection>() {
                    @Override
                    public EventType getType() {
                        return EventType.EDIT;
                    }
                    @Override
                    public VirtualCollection getData() {
                        return collection;
                    }
                    @Override
                    public AjaxRequestTarget getAjaxRequestTarget() {
                        return target;
                    }                    
                }); 
            }
        }); 
        add(new AjaxFallbackLink("btn_remove") {
            @Override
            public void onClick(final AjaxRequestTarget target) {
                fireEvent(new Event<VirtualCollection>() {
                    @Override
                    public EventType getType() {
                        return EventType.DELETE;
                    }
                    @Override
                    public VirtualCollection getData() {
                        return collection;
                    }
                    @Override
                    public AjaxRequestTarget getAjaxRequestTarget() {
                        return target;
                    }  
                }); 
            }
        }); 
    }
}
    