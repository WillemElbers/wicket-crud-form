package eu.clarin.mockups.vcr.crud.forms.fields;

/**
 *
 * @author wilelb
 */
public interface InputValidator {
    public boolean validate(String input);
    public String getErrorMessage();
}
