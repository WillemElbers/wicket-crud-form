package eu.clarin.mockups.vcr.crud.form;

import eu.clarin.mockups.vcr.crud.forms.CollectionPanel;
import eu.clarin.mockups.vcr.crud.form.pojo.VirtualCollection;
import eu.clarin.mockups.vcr.crud.forms.CreateAndEditPanel;
import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class HomePage extends WebPage {

    private final static Logger logger = LoggerFactory.getLogger(HomePage.class);
    
    final static String KEYPRESS_PARAM = "keycode";

    private final List<VirtualCollection> collections = new ArrayList<>();
    
    public HomePage() {        
        final CreateAndEditPanel crud = new CreateAndEditPanel("create_and_edit_panel");
        final WebMarkupContainer ajaxWrapper = new WebMarkupContainer("ajaxwrapper");
        ajaxWrapper.setOutputMarkupId(true);
        
        crud.addListener(new Listener<VirtualCollection>() {
            @Override
            public void handleEvent(Event<VirtualCollection> event) {
                switch(event.getType()) {
                    case SAVE:   
                        //Search or exising collection
                        int idx = -1;
                        String id = event.getData().getId();
                        for(int i = 0; i < collections.size(); i++) {
                            String listId = collections.get(i).getId();
                            if(listId.equalsIgnoreCase(id)) {
                                idx = i;
                            }
                        }                         
                        //Update or insert
                        if(idx >= 0) {
                            //Update collection
                            logger.info("Updating existing collection (id={}) @ idx={}", id, idx);
                            collections.set(idx, event.getData());
                        } else {
                            //New collection
                            logger.info("Adding new collection (id={})", id);
                            collections.add(event.getData());
                        }
                        //Update ui
                        if(event.getAjaxRequestTarget() != null) {
                            event.getAjaxRequestTarget().add(ajaxWrapper);
                        }
                        break;
                    default: 
                        throw new RuntimeException("Unhandled event. type = "+event.getType().toString());
                }
            }
        });
        add(crud);
        
        ListView listview = new ListView("listview", collections) {
            @Override
            protected void populateItem(ListItem item) {
                CollectionPanel pnl = 
                    new CollectionPanel("pnl_collection", (VirtualCollection)item.getModel().getObject());
                pnl.addListener(new Listener<VirtualCollection>() {
                    @Override
                    public void handleEvent(Event<VirtualCollection> event) {
                        switch(event.getType()) {
                            case EDIT: 
                                crud.editCollection(event.getData());
                                if(event.getAjaxRequestTarget() != null) {
                                    event.getAjaxRequestTarget().add(crud);
                                }
                                break;                                
                            case DELETE: 
                                //Search index for collection to remove
                                int idxToRemove = -1;
                                String id = event.getData().getId();
                                for(int i = 0; i < collections.size(); i++) {
                                    String listId = collections.get(i).getId();
                                    if(listId.equalsIgnoreCase(id)) {
                                        idxToRemove = i;
                                    }
                                }
                                //Remove collection
                                if(idxToRemove >= 0) {
                                    collections.remove(idxToRemove);
                                } else {
                                    logger.warn("Tried to remove but could not find collection with id={}", id);
                                }
                                //Update ui
                                if(event.getAjaxRequestTarget() != null) {
                                    event.getAjaxRequestTarget().add(ajaxWrapper);
                                }
                                break;
                            default: 
                                throw new RuntimeException("Unhandled event. type = "+event.getType().toString());
                        }
                    }
                });
                item.add(pnl);
            }
        };
        ajaxWrapper.add(listview);
        add(ajaxWrapper);
    }

}
