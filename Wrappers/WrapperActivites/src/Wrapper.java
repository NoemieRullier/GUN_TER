import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;


public class Wrapper {

    private static final String ONTO_PREFIX = "onto";
    private static final String ONTO_URL = "http://example.org/";

    private static final String OUTPUT_XML = "activities.xml"; 
    private static final String OUTPUT_RDF = "activities.ttl";

    private static final String DATA_URL =
	    "http://data.nantes.fr/api/publication/"
		    + "22440002800011_CG44_TOU_04812/"
		    + "activites_tourisme_et_handicap_STBL/content"
		    + "?format=xml";

    private static final String MAPPING_FILE = "res/mapping.properties";

    // Only for convenience...
    private static HashMap<String, String> mapping = 
	    new HashMap<String, String>();
    
    // Useless ?
    private static enum Field {ID, NAME, ADDRESS1, ADDRESS2, POSTAL_CODE, TOWN,
	WEBSITE, MAIL, V_IMPAIRMENT, H_IMPAIRMENT};

	public Document getXml() throws IOException, JDOMException {
	    URL url = new URL(DATA_URL);
	    URLConnection connection = url.openConnection();

	    InputStreamReader stream =
		    new InputStreamReader(connection.getInputStream());

	    return new SAXBuilder().build(stream);
	}

	public void writeXml() throws JDOMException, IOException {
	    XMLOutputter sortie =
		    new XMLOutputter(Format.getPrettyFormat());
	    sortie.output(this.getXml(),
		    new FileOutputStream(new File(OUTPUT_XML)));
	}
	
	private void loadMapping() throws FileNotFoundException, IOException {
	    Properties prop = new Properties();
	    prop.load(new FileReader(MAPPING_FILE));
	    
	    for(Object key : prop.keySet()) {
		mapping.put(key.toString(), prop.getProperty(key.toString()));
	    }
	}
	
	public void toRdf() throws IOException, JDOMException {
	    Model model = ModelFactory.createDefaultModel();
	    model.setNsPrefix(ONTO_PREFIX, ONTO_URL);

	    Document xml = this.getXml();
	    Element root = xml.getRootElement();

	    List<Element> listeActi = root.getChild("data").getChildren("element");
	    
	    // Every <element> matches with an activity
	    for(Element activity : listeActi) {
		Resource res = null;
		
		// Every java property matches with an RDF property
		for(String key : mapping.keySet()) {
		    Element current = activity;

		    String value = mapping.get(key);
		    
		    // Several elements (paths) which fit an RDF property
		    // are separated by a coma
		    String[] paths = value.split(",");
		    
		    for(int p = 0 ; p < paths.length ; p++) {
			String path = paths[p];
			String[] elements = path.split("\\.");

			for(int e = 0 ; e < elements.length && current != null ; e++) {
			    current = current.getChild(elements[e]);
			}

			if(current != null) {
			    String text = current.getValue();

			    if(text.equals("null") == false) {
				if(key.equals("hasName")) {
				    String id = text.replace(' ', '_')
					    .replace('\'', '_');

				    res = model.createResource(ONTO_URL + id);
				}
				
				res.addProperty(
					model.createProperty(ONTO_URL, key),
					text);
			    }
			}
		    }
		}
	    }
	    
	    model.write(new FileOutputStream(OUTPUT_RDF));
	}

	public static void main(String[] args) {
	    Wrapper w = new Wrapper();

	    try {
		w.loadMapping();
		w.toRdf();
	    }

	    catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	    catch (JDOMException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
}
