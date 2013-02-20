import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.util.FileManager;


public class Wrapper {

    private static final String ONTO_PREFIX = "onto";
    private static final String ONTO_URL = "http://example.org/";

    private static final String LOCAL_DATASET = "res/activities.xml";
    private static final String RDF_FILE = "res/activities.ttl";

    private static final String DATA_URL =
	    "http://data.nantes.fr/api/publication/"
		    + "22440002800011_CG44_TOU_04812/"
		    + "activites_tourisme_et_handicap_STBL/content"
		    + "?format=xml";

    private static final String MAPPING_FILE = "res/mapping.properties";

    // Only for convenience...
    private static HashMap<String, String> mapping = 
	    new HashMap<String, String>();

    /*
    // Useless ?
    private static enum Field {ID, NAME, ADDRESS1, ADDRESS2, POSTAL_CODE, TOWN, 
	WEBSITE, MAIL, V_IMPAIRMENT, H_IMPAIRMENT};
     */

    private Model model_ = ModelFactory.createDefaultModel();

    /**
     * 
     * 
     * @return
     * @throws IOException
     * @throws JDOMException
     */
    public Document getXml() throws IOException, JDOMException {
	InputStream stream = null;

	try {
	    URL url = new URL(DATA_URL);
	    URLConnection connection = url.openConnection();

	    stream = connection.getInputStream();
	}

	catch(IOException e) {
	    File local = new File(LOCAL_DATASET);

	    if(local.exists()) {
		stream = new FileInputStream(local);
	    }

	    else {
		throw new RuntimeException("Récupération des données échouée.");
	    }
	}

	return new SAXBuilder().build(stream);
    }

    /*
    public void writeXml() throws JDOMException, IOException {
	XMLOutputter sortie =
		new XMLOutputter(Format.getPrettyFormat());
	sortie.output(this.getXml(),
		new FileOutputStream(new File(OUTPUT_XML)));
    }
     */

    private void loadMapping() throws FileNotFoundException, IOException {
	Properties prop = new Properties();
	prop.load(new FileReader(MAPPING_FILE));

	for(Object key : prop.keySet()) {
	    mapping.put(key.toString(), prop.getProperty(key.toString()));
	}
    }

    public void toRdf() throws IOException, JDOMException {
	File rdfFile = new File(RDF_FILE);

	if(rdfFile.exists() == false) {
	    model_.setNsPrefix(ONTO_PREFIX, ONTO_URL);

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

				    res = model_.createResource(ONTO_URL + id);
				}

				res.addProperty(
					model_.createProperty(ONTO_URL, key),
					text);
			    }
			}
		    }
		}
	    }

	    model_.write(new FileOutputStream(RDF_FILE));
	}

	else {
	    InputStream input = FileManager.get().open(RDF_FILE);
	    model_.read(input, "TURTLE");
	}
    }

    public Model query1(String queryString) throws IOException, JDOMException {
	Model resModel = ModelFactory.createDefaultModel();
	
	toRdf();

	Query query = QueryFactory.create(queryString);
	QueryExecution qexec = QueryExecutionFactory.create(query, model_);
	ResultSet results = qexec.execSelect();
	
	PrefixMapping pm = query.getPrefixMapping();
	Map<String, String> truc = pm.getNsPrefixMap();
	
	for(String key : truc.keySet()) {
	    resModel.setNsPrefix(key, truc.get(key));
	}
	
	ElementGroup eltGroup = (ElementGroup) query.getQueryPattern();
	ElementPathBlock pathBlock =
		(ElementPathBlock) eltGroup.getElements().get(0);
	
	while(results.hasNext())
	{
	    QuerySolution sol = results.next();
	    
	    Iterator<TriplePath> it = pathBlock.patternElts();
	    
	    Node s;
	    Node o;
	    Resource res;
	    Property prop;
	    
	    while(it.hasNext()) {
		TriplePath queryTriple = it.next();
		
		s = queryTriple.getSubject();
		o = queryTriple.getObject();
		
		res = resModel.createResource(sol.getResource(s.getName()).getURI());
		prop = resModel.createProperty(queryTriple.getPredicate().getURI());
		
		res.addProperty(prop, o.getName());
	    }
	}
	
	return resModel;
    }
    
    public static void main(String[] args) {
	Wrapper w = new Wrapper();

	try {
	    w.loadMapping();
	    Model m1 = w.query1("prefix onto: <http://example.org/> SELECT ?x ?ad WHERE{ ?x onto:hasAddress ?ad}");
	    
	    m1.write(System.out);
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
