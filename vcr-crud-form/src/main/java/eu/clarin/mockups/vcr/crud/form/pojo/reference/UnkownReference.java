/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.clarin.mockups.vcr.crud.form.pojo.reference;

/**
 *
 * @author wilelb
 */
public class UnkownReference extends AbstractReference {
    private final String error;
    
    public UnkownReference(String value, String error) {
        super(value);
        this.error = error;
    }
    
    @Override
    public String toString() {
        return value + " (Invalid reference: " + error;
    }
}
