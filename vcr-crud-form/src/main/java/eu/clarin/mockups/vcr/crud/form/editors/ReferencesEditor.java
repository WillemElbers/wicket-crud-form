package eu.clarin.mockups.vcr.crud.form.editors;

import eu.clarin.mockups.vcr.crud.form.editors.references.ReferencePanel;
import eu.clarin.mockups.vcr.crud.form.pojo.reference.PidReference;
import eu.clarin.mockups.vcr.crud.form.pojo.reference.Reference;
import eu.clarin.mockups.vcr.crud.form.pojo.reference.UnkownReference;
import eu.clarin.mockups.vcr.crud.form.pojo.reference.UrlReference;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
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
    final Label lblNoReferences;
    final ListView listview;
    public ReferencesEditor(String id) {
        super(id);
        setOutputMarkupId(true);

        
        final WebMarkupContainer ajaxWrapper = new WebMarkupContainer("ajaxwrapper");
        ajaxWrapper.setOutputMarkupId(true);
        
        lblNoReferences = new Label("lbl_no_references", "No references found.");
        
        ajaxWrapper.add(lblNoReferences);
        listview = new ListView("listview", references) {
            @Override
            protected void populateItem(ListItem item) {
                ReferenceJob ref = (ReferenceJob)item.getModel().getObject();
                item.add(new ReferencePanel("pnl_reference", ref));
            }
        };

        ajaxWrapper.add(new AbstractAjaxTimerBehavior(Duration.seconds(1)) {
            @Override
            protected void onTimer(AjaxRequestTarget target) {
                target.add(ajaxWrapper);
            }
        });
        ajaxWrapper.add(listview);
        lblNoReferences.setVisible(references.isEmpty());
        listview.setVisible(!references.isEmpty());
        add(ajaxWrapper);
        
        Field f1 = new Field("reference", "Reference:", "Add URL or PID", data, this);        
        f1.setTriggerComplete(true);
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
                references.add(new ReferenceJob(new UrlReference(value)));
                data.setObject("");
            } else if(handlePid(value)) {
                references.add(new ReferenceJob(new PidReference(value)));
                data.setObject("");
            } else {
                references.add(new ReferenceJob(new UnkownReference(value, "Not a valid URL or PID.")));
            }

            if(!worker.isRunning()) {
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
                    Thread.sleep(5000);
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
                                        parseCmdi(body);
                                    } catch(IOException | ParserConfigurationException | XPathExpressionException | SAXException ex) {
                                        logger.error("Failed to parse CMDI", ex);
                                    }
                                } else if(job.getReference().getType().equalsIgnoreCase("text/xml") && 
                                    body.contains("xmlns=\"http://www.clarin.eu/cmd/\"")) {
                                    try {
                                        parseCmdi(body);
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
                //System.out.println("----------------------------------------");
                //System.out.println(responseBody);
            } finally {
                httpclient.close();
            }
        
            try {
                    Thread.sleep(5000);
                } catch(InterruptedException ex) {
                    logger.error("", ex);
                }
        }
    }

    private void parseCmdi(final String xml) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        logger.info("Parsing CMDI");
        /*
        String xml = "<urn:ResponseStatus version=\"1.0\" xmlns:urn=\"urn:camera-org\">\r\n" + //
        "\r\n" + //
        "<urn:requestURL>/CAMERA/Streaming/status</urn:requestURL>\r\n" + //
        "<urn:statusCode>4</urn:statusCode>\r\n" + //
        "<urn:statusString>Invalid Operation</urn:statusString>\r\n" + //
        "<urn:id>0</urn:id>\r\n" + //
        "\r\n" + //
        "</urn:ResponseStatus>";
        */
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new java.io.ByteArrayInputStream(xml.getBytes()));
        XPath xpath = XPathFactory.newInstance().newXPath();
        
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

        //XPathExpression expr = xpath.compile("//CMD/Header/MdCreator");
        XPathExpression expr = xpath.compile("//default:CMD");
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        logger.info("Nodelist.getLength()="+nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            Node currentItem = nodes.item(i);
            logger.info("found node -> " + currentItem.getLocalName() + " (namespace: " + currentItem.getNamespaceURI() + ")");
        }
    }
}
