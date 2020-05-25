package eu.clarin.mockups.vcr.crud.form.pojo.reference;

/**
 *
 * @author wilelb
 */
public interface Reference {
    public String getType();
    public void setType(String type);
    public String getUrl();
    public void setUrl(String url);
    public String getValue();
    public String getCheck();
    public void setCheck(String check);
    
    @Override
    public String toString();
}
