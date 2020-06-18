package eu.clarin.mockups.vcr.crud.form;

import eu.clarin.mockups.vcr.crud.forms.CreateAndEditPanel;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 *
 * @author wilelb
 */
public class HomePage extends WebPage {

    final static String KEYPRESS_PARAM = "keycode";

    public HomePage() {
        String name = "";
        String newReference = "";

        add(new Label("message", "Hello World!"));

        final Form<Void> form1 = new Form<Void>("form1") {
            @Override
            protected void onSubmit() {
                super.onSubmit();
                System.out.println("Form 1 submitted.");
            }

            @Override
            protected boolean wantSubmitOnNestedFormSubmit() {
                return false;
            }
        };
        form1.setOutputMarkupId(true);

        form1.add(new Label("lbl_name", "Name:"));
        form1.add(new TextField("input_name", new Model(name)));

        Button submitButton = new Button("submit1", Model.of("Save outer form"));
        form1.add(submitButton);
        form1.setDefaultButton(submitButton);
        /*
        Button   = new Button("cancel1", Model.of("Cancel outer form")) {
            @Override
                public void onSubmit() {
                    System.out.println("Cancel button onSubmitl");
               }
        };
         */
        AjaxFallbackLink cancelButton = new AjaxFallbackLink("cancel1") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                System.out.println("Cancel button clicked");
                if (target != null) {
                    // target is only available in an Ajax request
                    target.add(form1);
                }
            }
        };

        form1.add(cancelButton);

        add(form1);

        Form<Void> form2 = new Form<Void>("form2") {
            @Override
            protected void onSubmit() {
                super.onSubmit();
                System.out.println("Form 2 submitted.");
            }
        };
        Button submitButton2 = new Button("submit2", Model.of("Save inner form"));
        form2.add(submitButton2);
        form1.add(form2);

        form2.add(new Label("lbl_reference", "Add new reference:"));

        final TextField tf = new TextField("input_reference", new Model(newReference));
        tf.add(new AjaxFormComponentUpdatingBehavior("blur") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                System.out.println("onUpdate triggered via onBlur");
                String name = (String) tf.getDefaultModel().getObject();
                System.out.println("Data=" + name);
                target.add(form1);
            }
        });
        tf.add(new AjaxFormComponentUpdatingBehavior("keypress") {
            //Reference on getting the pressed key
            //https://stackoverflow.com/a/14468972

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);

                final int enter_keycode = 13;
                final int escape_keycode = 27;
                
                IAjaxCallListener listener = new AjaxCallListener() {
                    @Override
                    public CharSequence getPrecondition(Component component) {
                        //this javascript code evaluates wether an ajaxcall is necessary.
                        //Here only by keyocdes for F9 and F10 
                        return "var keycode = Wicket.Event.keyCode(attrs.event);"
                                + "if ((keycode == "+enter_keycode+") || (keycode == "+escape_keycode+"))"
                                + "    return true;"
                                + "else"
                                + "    return false;";
                    }
                };
                attributes.getAjaxCallListeners().add(listener);

                //Append the pressed keycode to the ajaxrequest 
                attributes.getDynamicExtraParameters()
                        .add("var eventKeycode = Wicket.Event.keyCode(attrs.event);"
                                + "return {keycode: eventKeycode};");

                //whithout setting, no keyboard events will reach any inputfield
                attributes.setAllowDefault(true);
            }

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                final Request request = RequestCycle.get().getRequest();
                final String jsKeycode = request.getRequestParameters()
                        .getParameterValue(KEYPRESS_PARAM).toString("");

                
                
                final int keyCode = Integer.valueOf(jsKeycode);
                if(keyCode == 13) {
                    String name = (String) tf.getDefaultModel().getObject();
                    System.out.println("Submitting data=" + name);
                } else if(keyCode == 27) {
                    System.out.println("Cancel data");
                }
                
                target.add(form1);
            }
        });

        form2.add(tf);
        
        add(new CreateAndEditPanel("create_and_edit_panel"));
        /*
        final Component ajax_update_component = this;
        IModel<String> nameModel = Model.of("");
        add(new VcrTextField("name", "Name:", "", nameModel));
        add(new VcrChoiceField("type", "Type:", Arrays.asList(new String[]{"A", "B", "C"})));
        add(new AuthorEditor("authors"));
        add(new ReferencesEditor("references"));
        add(new AjaxFallbackLink("btn_save") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                System.out.println("Save button clicked");
                if (target != null) {
                    target.add(ajax_update_component);
                }
            }
        });
        add(new AjaxFallbackLink("btn_cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                System.out.println("Cancel button clicked");
                if (target != null) {
                    target.add(ajax_update_component);
                }
            }
        });
        */
    }

}
