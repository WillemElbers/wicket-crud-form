package eu.clarin.mockups.vcr.crud.forms.editors;

import eu.clarin.mockups.vcr.crud.form.pojo.Reference;
import eu.clarin.mockups.vcr.crud.forms.fields.VcrTextField;
import eu.clarin.mockups.vcr.crud.forms.fields.Field;
import eu.clarin.mockups.vcr.crud.forms.fields.FieldComposition;
import eu.clarin.mockups.vcr.crud.forms.fields.InputValidator;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author wilelb
 */
public class ReferencesEditor extends Panel implements FieldComposition {
    private static Logger logger = LoggerFactory.getLogger(ReferencesEditor.class);
    
    private final List<ReferenceJob> references = new CopyOnWriteArrayList<>();
    private IModel<String> data = new Model<>();
    
    final Label lblNoReferences;
    final ListView listview;
    
    private transient Worker worker = new Worker();
    
    public class Validator implements InputValidator, Serializable {
        private String message = "";
            
            @Override
            public boolean validate(String input) {
                try {
                    //URI.create(input);
                    new URL(input);
                } catch(MalformedURLException ex) {
                    message = ex.getMessage();
                    return false;
                }
                return true;
            }

            @Override
            public String getErrorMessage() {
                return message;
            }
    }
    
    public ReferencesEditor(String id) {
        super(id);
        setOutputMarkupId(true);

        
        final WebMarkupContainer ajaxWrapper = new WebMarkupContainer("ajaxwrapper");
        ajaxWrapper.setOutputMarkupId(true);
        
        lblNoReferences = new Label("lbl_no_references", "No references found.");
        
        final List<Component> comps = new ArrayList<>();
        listview = new ListView("listview", references) {
            @Override
            protected void populateItem(ListItem item) {
                ReferenceJob ref = (ReferenceJob)item.getModel().getObject();
                Component c = new ReferencePanel("pnl_reference", ref);
                c.setOutputMarkupId(true);
                comps.add(c);
                item.add(c);
            }
        };

        ajaxWrapper.add(new AbstractAjaxTimerBehavior(Duration.seconds(1)) {
            @Override
            protected void onTimer(AjaxRequestTarget target) {
                if(target != null) {
                    //target.add(ajaxWrapper);
                    for(Component c : comps) {
                        target.add(c);
                    }
                }
            }
        });
        ajaxWrapper.add(listview);
       
        lblNoReferences.setVisible(references.isEmpty());
        listview.setVisible(!references.isEmpty());
        
        ajaxWrapper.add(lblNoReferences);
        ajaxWrapper.add(listview);
        add(ajaxWrapper);
        
        //add(lblNoReferences);
        //add(listview);
        
        Field f1 = new VcrTextField("reference", "Reference:", "Add URL or PID", data, this);        
        f1.setCompleteSubmitOnUpdate(true);
        f1.addValidator(new Validator());
        add(f1);
    }
    
    @Override
    protected void onRemove() {
        logger.info("Removing Reference editor");
        worker.stop();
    }

    @Override
    public boolean completeSubmit(AjaxRequestTarget target) {
        logger.info("Completing reference submit: value="+data.getObject());
        
        String value = data.getObject();
        if(value != null && !value.isEmpty()) {
            if(handleUrl(value)) {
                references.add(new ReferenceJob(new Reference(value)));
                data.setObject("");
            } else if(handlePid(value)) {
                references.add(new ReferenceJob(new Reference(value)));
                data.setObject("");
            } else {
//                references.add(new ReferenceJob(new UnkownReference(value, "Not a valid URL or PID.")));
            }

            if(worker == null || !worker.isRunning()) {
                worker = new Worker();
                worker.start();
                new Thread(worker).start();
                logger.info("Worker thread started");
            }
            
            if(target != null) {
                lblNoReferences.setVisible(references.isEmpty());
                listview.setVisible(!references.isEmpty());
                target.add(this);
            }
        }
        return false;
    }
    
    private boolean handleUrl(String value) {
        boolean result = false;
        try {
            URL url = new URL(value);
            result = true;
        } catch(MalformedURLException ex) {
            logger.debug("Failed to parse value: "+value+" as url", ex);
        }
        return result;
    }
    
    private boolean handlePid(String value) {
        return false;
    }
    
    public enum State {
        INITIALIZED, ANALYZING, DONE, FAILED
    }
    
    public List<Reference> getData() {
        List<Reference> result = new ArrayList<>();
        for(ReferenceJob job : references) {
            result.add(job.getReference());
        }
        return result;
    }
    
    public void reset() {
        references.clear();
    }
    
    public class ReferenceJob implements Serializable {
        private Reference ref;
        private State state;
        
        public ReferenceJob(Reference ref) {
            this.ref = ref;
            this.state = State.INITIALIZED;
        }
        
        public State getState() {
            return this.state;
        }
        
        public synchronized void setState(State newState){
            this.state = newState;
        }
        
        public Reference getReference() {
            return this.ref;
        }
    }
    
    public class Worker implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(Worker.class);
        
        private boolean running = false;
        
        public Worker() {}
        
        public void start() {
            this.running = true;
        }
        
        public synchronized void stop() {
            this.running = false;
        }
        
        public boolean isRunning() {
            return this.running;
        }
        
        @Override
        public void run() {
            while(running) {
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException ex) {
                    logger.error("", ex);
                }
                
                synchronized(this){
                    for(ReferenceJob job : references) {
                        if(job.getState() == State.INITIALIZED) {
                            job.setState(State.ANALYZING);
                            try {
                                analyze(job);
                                job.setState(State.DONE);
                            } catch(Exception ex) {
                                job.setState(State.FAILED);
                            }   
                        }
                    }
                }
            }
            logger.info("Worker thread finished");
        }
        
        
        private void analyze(final ReferenceJob job) throws IOException {
            logger.info("Analyzing");
            
            CloseableHttpClient httpclient = HttpClients.createDefault();
            try {
                HttpGet httpget = new HttpGet(job.getReference().getValue());
                logger.info("Executing request " + httpget.getRequestLine());

                // Create a custom response handler
                ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                    @Override
                    public String handleResponse(
                            final HttpResponse response) throws ClientProtocolException, IOException {
                        for(Header h : response.getHeaders("Content-Type")) {
                            logger.info(h.getName() + " - " + h.getValue());
                            
                            String[] parts = h.getValue().split(";");
                            String mediaType = parts[0];
                            
                            logger.info("Media-Type="+mediaType);
                            if(parts.length > 1) {
                                String p = parts[1].trim();
                                if(p.startsWith("charset=")) {
                                    logger.info("Charset="+p.replaceAll("charset=", ""));
                                } else if(p.startsWith("boundary=")) {
                                    logger.info("Boundary="+p.replaceAll("boundary=", ""));
                                }
                            }
                            
                            job.getReference().setType(mediaType);
                                    
                        }
                        for(Header h : response.getHeaders("Content-Length")) {
                            logger.info(h.getName() + " - " + h.getValue());
                        }
                        int status = response.getStatusLine().getStatusCode();
                        if (status >= 200 && status < 300) {
                            HttpEntity entity = response.getEntity();
                            job.getReference().setCheck("HTTP "+status+"/"+response.getStatusLine().getReasonPhrase());
                            String body = entity != null ? EntityUtils.toString(entity) : null;
                            
                            if(body != null) {
                                String type = job.getReference().getType();
                                if(type.equalsIgnoreCase("application/x-cmdi+xml")) {
                                    try {
                                        parseCmdi(body, job);
                                    } catch(IOException | ParserConfigurationException | XPathExpressionException | SAXException ex) {
                                        logger.error("Failed to parse CMDI", ex);
                                    }
                                } else if(job.getReference().getType().equalsIgnoreCase("text/xml") && 
                                    body.contains("xmlns=\"http://www.clarin.eu/cmd/\"")) {
                                    try {
                                        parseCmdi(body, job);
                                    } catch(IOException | ParserConfigurationException | XPathExpressionException | SAXException ex) {
                                        logger.error("Failed to parse CMDI", ex);
                                    }
                                }
                            }
                            return body;
                        } else {
                            throw new ClientProtocolException("Unexpected response status: " + status);
                        }
                    }

                };
                String responseBody = httpclient.execute(httpget, responseHandler);
            } finally {
                httpclient.close();
            }
        
            try {
                    Thread.sleep(1000);
                } catch(InterruptedException ex) {
                    logger.error("", ex);
                }
        }
    }

    private void parseCmdi(final String xml, final ReferenceJob job) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        logger.info("Parsing CMDI");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new java.io.ByteArrayInputStream(xml.getBytes()));

        String profile = getValueForXPath(doc, "//default:CMD/default:Header/default:MdProfile/text()");
        logger.info("CMDI profile = " + profile);
        
        String name = getValueForXPath(doc, "//default:CMD/default:Components/default:lat-session/default:Name/text()");
        String description = getValueForXPath(doc, "//default:CMD/default:Components/default:lat-session/default:descriptions/default:Description[lang('eng')]/text()");
        logger.info("Name = " + name + ", description = " + description);
        
        if(name != null) {
            job.getReference().setTitle(name);
        }
        if(description != null) {
            job.getReference().setDescription(description);
        }
    }
    
    /**
     * Return the first value of the xpath query result, or null if the result is
     * empty
     * 
     * @param doc
     * @param xpathQuery
     * @return 
     */
    private String getValueForXPath(Document doc, String xpathQuery) {
        List<String> result = getValuesForXPath(doc, xpathQuery);
        if(result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }
    
    /**
     * Return all values for the xpath query
     * 
     * @param doc
     * @param xpathQuery
     * @return 
     */
    private List<String> getValuesForXPath(Document doc, String xpathQuery) {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                return prefix.equals("default") ? "http://www.clarin.eu/cmd/" : null;
            }

            @Override
            public Iterator<String> getPrefixes(String val) {
                return null;
            }

            @Override
            public String getPrefix(String uri) {
                return null;
            }
        });
        
        List<String> result = new ArrayList<>();
        
        try {
            XPathExpression expr = xpath.compile(xpathQuery);
            Object xpathResult = expr.evaluate(doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList) xpathResult;
            logger.trace("XPatch query = ["+xpathQuery+"], result nodelist.getLength() = "+nodes.getLength());
            for (int i = 0; i < nodes.getLength(); i++) {
                Node currentItem = nodes.item(i);
                logger.trace("found node -> " + currentItem.getLocalName() + " (namespace: " + currentItem.getNamespaceURI() + "), value = " + currentItem.getNodeValue());
                result.add(currentItem.getNodeValue());
            }
        } catch(XPathExpressionException ex) {
            logger.error("XPath query ["+xpathQuery+"] failed.", ex);
        }
        
        return result;
    }
}
