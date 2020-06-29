package eu.clarin.mockups.vcr.crud.form;

import eu.clarin.mockups.vcr.crud.forms.CreateAndEditPanel;
import org.apache.wicket.markup.html.WebPage;

/**
 *
 * @author wilelb
 */
public class HomePage extends WebPage {

    final static String KEYPRESS_PARAM = "keycode";

    public HomePage() {
        add(new CreateAndEditPanel("create_and_edit_panel"));
    }

}
