package eu.clarin.mockups.vcr.crud.form.editors;

import eu.clarin.mockups.vcr.crud.form.pojo.Author;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class AuthorEditor extends Panel {//implements FieldComposition {

    private final static Logger logger = LoggerFactory.getLogger(AuthorEditor.class);

    private final List<Editable<Author>> authors = new ArrayList<>();

    /*
    private final IModel<String> mdlName = new Model<>();
    private final IModel<String> mdlEmail = new Model<>();
    private final IModel<String> mdlAffiliation = new Model<>();

    private Component focusResetComponent;

    private List<Field> fields = new ArrayList<>();
*/
    public class Editable<T> implements Serializable {
        private final T data;
        
        private boolean editing = false;
        
        public Editable(T data) {
            this.data = data;
        }
        
        public void setEditing(boolean editing) {
            this.editing = editing;
        }
        
        public boolean isEditing() {
            return editing;
        }
        
        public T getData() {
            return data;
        }
    }
    
    public class AuthorPanel extends Panel {

        public AuthorPanel(String id, final Editable<Author> editableAuthor, final Component componentToUpdate) {
            super(id);
            setOutputMarkupId(true);
            //final Component _this = this;

            final Author a = editableAuthor.getData();
            add(new Label("name", a.getName()));
            add(new Label("email", a.getEmail()));
            add(new Label("affiliation", a.getAffiliation()));

            AjaxFallbackLink editButton = new AjaxFallbackLink("btn_edit") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    for(int i = 0; i < authors.size(); i++) {
                        if(authors.get(i).getData().getId().equalsIgnoreCase(a.getId())) {
                            authors.get(i).setEditing(true);
                        }
                    }
                    if (target != null) {
                        target.add(componentToUpdate);
                    }
                }
            };
            add(editButton);

            AjaxFallbackLink removeButton = new AjaxFallbackLink("btn_remove") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    //TODO: add confirmation step
                    for(int i = 0; i < authors.size(); i++) {
                        if(authors.get(i).getData().getId().equalsIgnoreCase(a.getId())) {
                            authors.remove(i);
                        }
                    }
                    if (target != null) {
                        target.add(componentToUpdate);
                    }
                }
            };
            add(removeButton);
        }

    }

    public class AuthorEditPanel extends Panel implements FieldComposition {

        private final IModel<String> mdlName = new Model<>();
        private final IModel<String> mdlEmail = new Model<>();
        private final IModel<String> mdlAffiliation = new Model<>();
    
        private final List<Field> fields = new ArrayList<>();
        
        private final Editable<Author> editableAuthor;
        private final Component componentToUpdate;
        
        /**
         * Editor to create a new author
         * 
         * @param id
         * @param componentToUpdate 
         */
        public AuthorEditPanel(String id, final Component componentToUpdate) {
            this(id, null, componentToUpdate);
        }
        
        /**
         * If editableAuthor is not null edit the values, otherwise create
         * 
         * @param id
         * @param editableAuthor
         * @param componentToUpdate 
         */
        public AuthorEditPanel(String id, final Editable<Author> editableAuthor, final Component componentToUpdate) {
            super(id);
            this.editableAuthor = editableAuthor;
            this.componentToUpdate = componentToUpdate;
            
            setOutputMarkupId(true);
            
            if(editableAuthor != null) {
                Author a = editableAuthor.getData();
                mdlName.setObject(a.getName());
                mdlEmail.setObject(a.getEmail());
                mdlAffiliation.setObject(a.getAffiliation());
            }
            
            Field f1 = new Field("author_name", "Name:", null, mdlName, this);
            f1.setRequired(true);
            //this.focusResetComponent = f1.getComponentToTakeFocus();
            Field f2 = new Field("author_email", "Email:", null, mdlEmail, this);
            f2.setRequired(true);
            Field f3 = new Field("author_affiliation", "Affiliation:", null, mdlAffiliation, this);
            f3.setTriggerComplete(true);

            fields.add(f1);
            fields.add(f2);
            fields.add(f3);

            add(f1);
            add(f2);
            add(f3);
        }

        @Override
        public boolean completeSubmit(AjaxRequestTarget target) {
            logger.info("Completing author submit: name=" + mdlName.getObject() + ", email=" + mdlEmail.getObject() + ", affiliation=" + mdlAffiliation.getObject());

            boolean valid = true;
            for (Field f : fields) {
                if (!f.validate()) {
                    valid = false;
                }
            }

            if (valid) {
                String name = mdlName.getObject();
                String email = mdlEmail.getObject();
                String affiliation = mdlAffiliation.getObject() == null ? null : mdlAffiliation.getObject();

                //Create or edit
                if(editableAuthor == null) {
                    authors.add(new Editable(new Author(name, email, affiliation)));
                } else {
                    editableAuthor.getData().setName(name);
                    editableAuthor.getData().setEmail(email);
                    editableAuthor.getData().setAffiliation(affiliation);
                    editableAuthor.setEditing(false);
                }
                
                mdlName.setObject("");
                mdlEmail.setObject("");
                mdlAffiliation.setObject("");

                if (target != null) {
                    target.add(componentToUpdate);
                }
            }
            return false;
        }

    }
    
    public AuthorEditor(String id) {
        super(id);
        setOutputMarkupId(true);

        final WebMarkupContainer ajaxWrapper = new WebMarkupContainer("ajaxwrapper");
        ajaxWrapper.setOutputMarkupId(true);
        
        ListView listview = new ListView("listview", authors) {
            @Override
            protected void populateItem(ListItem item) {
                Editable<Author> object = (Editable<Author>) item.getModel().getObject();
                if(object.isEditing()) {
                    item.add(new AuthorEditPanel("pnl_author_details", object, ajaxWrapper));
                } else {
                    item.add(new AuthorPanel("pnl_author_details", object, ajaxWrapper));
                }
            }
        };
        ajaxWrapper.add(listview);
        add(ajaxWrapper);
        
        add(new AuthorEditPanel("pnl_create_author", null, ajaxWrapper));
        
        /*
        Field f1 = new Field("author_name", "Name:", null, mdlName, this);
        f1.setRequired(true);
        this.focusResetComponent = f1.getComponentToTakeFocus();
        Field f2 = new Field("author_email", "Email:", null, mdlEmail, this);
        f2.setRequired(true);
        Field f3 = new Field("author_affiliation", "Affiliation:", null, mdlAffiliation, this);
        f3.setTriggerComplete(true);

        fields.add(f1);
        fields.add(f2);
        fields.add(f3);

        add(f1);
        add(f2);
        add(f3);
*/
    }
/*
    @Override
    public boolean completeSubmit(AjaxRequestTarget target) {
        logger.info("Completing author submit: name=" + mdlName.getObject() + ", email=" + mdlEmail.getObject() + ", affiliation=" + mdlAffiliation.getObject());

        boolean valid = true;
        for (Field f : fields) {
            if (!f.validate()) {
                valid = false;
            }
        }

        if (valid) {
            String name = mdlName.getObject();
            String email = mdlEmail.getObject();
            String affiliation = mdlAffiliation.getObject() == null ? null : mdlAffiliation.getObject();

            authors.add(new Editable(new Author(name, email, affiliation)));

            mdlName.setObject("");
            mdlEmail.setObject("");
            mdlAffiliation.setObject("");

            if (target != null) {
                target.add(this);
                if (this.focusResetComponent != null) {
                    target.focusComponent(this.focusResetComponent);
                }
            }
        }
        return false;
    }
*/
}
