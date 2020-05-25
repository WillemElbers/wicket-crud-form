
package eu.clarin.mockups.vcr.crud.form.pojo.reference;

import java.io.Serializable;

/**
 *
 * @author wilelb
 */
public abstract class AbstractReference implements Reference, Serializable {
    protected String value;
    protected String url;
    private String check;
    protected String type;
    
    public AbstractReference() {}
    
    public AbstractReference(String value) {
        this.value = value;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    @Override
    public String getType() {
        return type;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    @Override
    public String getUrl() {
        return url;
    }
    
    /**
     * @return the value
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return value;
    }

    /**
     * @return the check
     */
    public String getCheck() {
        return check;
    }

    /**
     * @param check the check to set
     */
    public void setCheck(String check) {
        this.check = check;
    }
}
