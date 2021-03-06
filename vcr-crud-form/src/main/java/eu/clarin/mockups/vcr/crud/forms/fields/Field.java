package eu.clarin.mockups.vcr.crud.forms.fields;

import eu.clarin.mockups.vcr.crud.forms.editors.AjaxFormComponentOnKeySubmitBehavior;
import eu.clarin.mockups.vcr.crud.forms.editors.references.ReferencesEditor;
import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
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
public abstract class Field extends Panel {
    private static final Logger logger = LoggerFactory.getLogger(ReferencesEditor.class);
    
    //Clear TextField on submit, defaults to false;
    private boolean resetOnSubmit = false;
    
    //If set to true, this component triggers a complete call on the parent when
    //losing focus (typically the last component of  form)
    //private boolean triggerComplete = false;
    
    //The next component to receive focus after submitting this component (can be null)
    protected Component nextComponentToFocus = null;
    
    protected Component componentToTakeFocus = null;
    
    private final List<InputValidator> inputValidators = new ArrayList<>();
    
    private boolean required = false;
    
    private final Model errorMessageModel = Model.of("");
    private Label lblErrorMessage;
    
    protected final IModel dataModel;
    
    protected final Component editComponent;
    
    private boolean completeSubmitOnUpdate = false;
    
    private final Label requiredLabel;
    
    public Field(String id, String label, Component editComponent) {
        this(id, label, null, new Model<>(), null, editComponent);
    }
    
    public Field(String id, String label, String defaultValue, final IModel dataModel, final FieldComposition parent, Component editComponent) {
        super(id);
        this.editComponent = editComponent;
        this.dataModel = dataModel;
        setOutputMarkupId(true);
        
        addUpdatingBehavior(editComponent, parent, this);
        add(editComponent);            
        componentToTakeFocus = editComponent;
        
        add(new Label("label", Model.of(label)));

        requiredLabel = new Label("required", "*");
        requiredLabel.setVisible(required);
        add(requiredLabel);
        
        lblErrorMessage = new Label("error_message", errorMessageModel);
        lblErrorMessage.setVisible(false);
        add(lblErrorMessage);
    }
    
    protected void addUpdatingBehavior(Component c, final FieldComposition parent, final Component t) {
        c.add(getOnFocusUpdatingBehavior(parent));
        c.add(getOnBlurUpdatingBehavior(parent, t));
        c.add(getOnKeySubmitBehavior(parent, t));
    }
    
    protected AjaxFormComponentUpdatingBehavior getOnFocusUpdatingBehavior(final FieldComposition parent) {
        return new AjaxFormComponentUpdatingBehavior("focus") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if(parent != null) {
                    parent.increaseFocusCount();
                }
            }
        };
    }
    
    protected AjaxFormComponentUpdatingBehavior getOnBlurUpdatingBehavior(final FieldComposition parent, final Component t) {
        return new AjaxFormComponentUpdatingBehavior("blur") {
            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                logger.trace("onUpdate: triggered via onBlur");
                if(validate()) {
                    handleUpdateData(target, dataModel, nextComponentToFocus);                    
                    if(parent != null && completeSubmitOnUpdate) {
                        /*
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                 try {
                                    Thread.sleep(500);
                                } catch(Exception ex) {

                                }
                                parent.decreaseFocusCount();
                                parent.completeSubmit(target);
                            }
                        }, "blur");
                        t.start();
                       */
                        parent.decreaseFocusCount();
                        parent.completeSubmit(target);
                    }
                }

                target.add(t);
            }
        };
    }
    
    protected AjaxFormComponentOnKeySubmitBehavior getOnKeySubmitBehavior(final FieldComposition parent, final Component t) {
        return new AjaxFormComponentOnKeySubmitBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {               
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
        };
    }
    
    public boolean validate() {
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
        this.requiredLabel.setVisible(required);
    }
    
    public Component getComponentToTakeFocus() {
        return componentToTakeFocus;
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
    
    //@Override
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

    /**
     * @param completeSubmitOnUpdate the completeSubmitOnUpdate to set
     */
    public void setCompleteSubmitOnUpdate(boolean completeSubmitOnUpdate) {
        this.completeSubmitOnUpdate = completeSubmitOnUpdate;
    }
    
}
