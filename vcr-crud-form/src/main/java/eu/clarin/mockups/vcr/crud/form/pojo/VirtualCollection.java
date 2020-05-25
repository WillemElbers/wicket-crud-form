
package eu.clarin.mockups.vcr.crud.form.pojo;

import eu.clarin.mockups.vcr.crud.form.pojo.reference.Reference;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wilelb
 */
public class VirtualCollection {
    private String name;
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
}
