package eu.clarin.mockups.vcr.crud.form;

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.markup.html.panel.Panel;

/**
 *
 * @author wilelb
 */
public abstract class ActionablePanel extends Panel {
    private final List<Listener> actionListeners = new ArrayList<>();
    
    public ActionablePanel(String id) {
        super(id);
    }
    
    public void addListener(Listener l) {
        actionListeners.add(l);
    }
    
    public void removeListener(Listener l) {
        throw new RuntimeException("Not implemented");
    }
    
    public void fireEvent(Event evt) {
        for(Listener l : actionListeners) {
            l.handleEvent(evt);
        }
    }
}
