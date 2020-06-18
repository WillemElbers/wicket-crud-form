
package eu.clarin.mockups.vcr.crud.form.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wilelb
 */
public class VirtualCollection implements Serializable {
    private String name;
    private String type;
    private List<Author> authors = new ArrayList<>();
    private List<Reference> references = new ArrayList<>();

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the references
     */
    public List<Reference> getReferences() {
        return references;
    }

    /**
     * @param references the references to set
     */
    public void setReferences(List<Reference> references) {
        this.references = references;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the authors
     */
    public List<Author> getAuthors() {
        return authors;
    }

    /**
     * @param authors the authors to set
     */
    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }
}
