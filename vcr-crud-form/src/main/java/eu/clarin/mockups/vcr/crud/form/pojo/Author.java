package eu.clarin.mockups.vcr.crud.form.pojo;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author wilelb
 */
public class Author implements Serializable {
    
    private String id;
    private String name;
    private String email;
    private String affiliation;

    public Author() {}
    
    public Author(String name, String email, String affiliation) {
        this(UUID.randomUUID().toString(), name, email, affiliation);
    }
    
    public Author(String id, String name, String email, String affiliation) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.affiliation = affiliation;
    }
    
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
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the affiliation
     */
    public String getAffiliation() {
        return affiliation;
    }

    /**
     * @param affiliation the affiliation to set
     */
    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }
    
    @Override
    public String toString() {
        String a = affiliation == null ? "" : " / " + affiliation;
        return name + " / " + email + a;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
}
