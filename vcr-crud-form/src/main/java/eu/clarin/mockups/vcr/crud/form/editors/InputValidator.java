package eu.clarin.mockups.vcr.crud.form.editors;

/**
 *
 * @author wilelb
 */
public interface InputValidator {
    public boolean validate(String input);
    public String getErrorMessage();
}
