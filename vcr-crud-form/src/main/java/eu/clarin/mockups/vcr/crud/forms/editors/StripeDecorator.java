package eu.clarin.mockups.vcr.crud.forms.editors;

import java.io.Serializable;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;

/**
 *
 * @author wilelb
 */
public class StripeDecorator implements Decorator, Serializable {

    private final String CLASS_ODD = "odd";
    private final String CLASS_EVEN = "even";
    
    private int count = 0;
    
    @Override
    public void decorate(Component c) {
        c.add(new AttributeModifier("class", count % 2 == 0 ? CLASS_EVEN : CLASS_ODD));
        count++;
    }
}
