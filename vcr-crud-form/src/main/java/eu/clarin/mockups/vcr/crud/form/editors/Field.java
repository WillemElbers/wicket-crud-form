package eu.clarin.mockups.vcr.crud.form.editors;

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Editable TextField with label. 
 * Input is updated into the model onBlur and onPress (enter)
 * 
 * @author wilelb
 */
public class Field extends AbstractEditor {
    private static final Logger logger = LoggerFactory.getLogger(ReferencesEditor.class);
    
    private TextField tf;
    
    //Clear TextField on submit, defaults to false;
    private boolean resetOnSubmit = false;
    //If set to true, this component triggers a complete call on the parent when
    //losing focus (typically the last component of  form)
    private boolean triggerComplete = false;
    //The next component to receive focus after submitting this component (can be null)
    private Component nextComponentToFocus = null;
    
    private final List<InputValidator> inputValidators = new ArrayList<>();
    
    private boolean required = false;
    
    private final Model errorMessageModel = Model.of("");
    private Label lblErrorMessage;
    
    final IModel dataModel;
    
    public Field(String id, String label) {
        this(id, label, null, new Model<>(), null);
    }
    
    public Field(String id, String label, String defaultValue, final IModel dataModel, final FieldComposition parent) {
        super(id);
        this.dataModel = dataModel;
        setOutputMarkupId(true);
        
        final Component t = this;
        
        String idSuffix = "reference";
        add(new Label("lbl_"+idSuffix, Model.of(label)));

        tf = new TextField("input_"+idSuffix, dataModel);
        tf.setOutputMarkupId(true);
        if(defaultValue != null) {
            tf.add(new AttributeModifier("placeholder", defaultValue));
        }
        tf.add(new AjaxFormComponentUpdatingBehavior("blur") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                logger.trace("onUpdate: triggered via onBlur");
                if(validate()) {
                    handleUpdateData(target, dataModel, nextComponentToFocus);                    
                
                if(triggerComplete && parent != null) {
                        parent.completeSubmit(target);
                    }
                }
                //else {
                    target.add(t);
                //}
            }
        });
        tf.add(new AjaxFormComponentOnKeySubmitBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {               
                logger.trace("onUpdate: triggered via keyPress, jsKeycode=[" + getPressedKeyCode() + "]");
                if(pressedReturn()) {
                    logger.trace("onUpdate: pressedReturn() == true");
                    
                    if(validate()) {
                        handleUpdateData(target, dataModel, nextComponentToFocus);                        
                    
                    if(parent != null) {
                            parent.completeSubmit(target);
                        }
                    }
                        target.add(t);
                    
                }
            }
        });
        add(tf);
        
        lblErrorMessage = new Label("error_message", errorMessageModel);
        lblErrorMessage.setVisible(false);
        add(lblErrorMessage);
    }
    
    protected boolean validate() {
        String input = (String)dataModel.getObject(); 
        
        //Check for value if required == true
        if(required && input == null) {
            lblErrorMessage.setVisible(true);
            errorMessageModel.setObject("Required field.");
            return false;
        } 
        if(required && input.isEmpty()) {
            lblErrorMessage.setVisible(true);
            errorMessageModel.setObject("Required field.");
            return false;
        }
        
        //If any validator fails, set error message and return false
        if(input != null) {
            for(InputValidator v : this.inputValidators) {
                if(!v.validate(input)) {
                    lblErrorMessage.setVisible(true);
                    errorMessageModel.setObject(v.getErrorMessage());
                    return false;
                }
            }
        }
        
        //All validators passed, reset error message and return true
        lblErrorMessage.setVisible(false);
        errorMessageModel.setObject("");
        return true;
    }
    
    public void addValidator(InputValidator validator) {
        if(validator != null) {
            this.inputValidators.add(validator);
        }
    }
    
    public void setRequired(boolean required) {
        this.required = required;
    }
    
    public Component getComponentToTakeFocus() {
        return tf;
    }
    
    public void setNextComponentToFocus(Field f) {
        if(f != null) {
            this.nextComponentToFocus = f.getComponentToTakeFocus();
        }
    }
    
    public void setNextComponentToFocus(Component c) {
        this.nextComponentToFocus = c;
    }
    
    public void setResetOnSubmit(boolean resetOnSubmit) {
        this.resetOnSubmit = resetOnSubmit;
    }
    
    public void setTriggerComplete(boolean triggerComplete) {
        this.triggerComplete = triggerComplete;
    }
    
    @Override
    protected void handleUpdateData(AjaxRequestTarget target, IModel modelToUpdate, Component nextComponentToFocus) {        
        String value = (String) modelToUpdate.getObject();    
        if(resetOnSubmit) {
            modelToUpdate.setObject("");
        }
        logger.debug("Field: id="+getId()+", value=" + value);
        
        if(target != null) {
            target.add(this);
            if(nextComponentToFocus != null) {
                target.focusComponent(nextComponentToFocus);
            }
        }
    }
    
}
