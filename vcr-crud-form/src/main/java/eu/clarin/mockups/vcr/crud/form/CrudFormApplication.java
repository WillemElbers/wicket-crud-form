package eu.clarin.mockups.vcr.crud.form;

import org.apache.wicket.protocol.http.WebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class CrudFormApplication extends WebApplication {
    
    private static Logger logger = LoggerFactory.getLogger(CrudFormApplication.class);
    
    public CrudFormApplication() {
    }
    /**
     * @return 
     * @see org.apache.wicket.Application#getHomePage()
     */
    @Override
    public Class getHomePage() {
        return HomePage.class;
    }
    
}
