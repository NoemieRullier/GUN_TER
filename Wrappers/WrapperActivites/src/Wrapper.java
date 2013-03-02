import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.util.NodeFactory;
import com.hp.hpl.jena.util.FileManager;

public class Wrapper {

	private static final String ONTO_PREFIX = "onto";
	private static final String ONTO_URL = "http://example.org/";

	private static final String LOCAL_DATASET = "res/activities.xml";
	private static final String RDF_FILE = "res/activities.ttl";

	private static final String DATA_URL = "http://data.nantes.fr/api/publication/"
			+ "22440002800011_CG44_TOU_04812/"
			+ "activites_tourisme_et_handicap_STBL/content" + "?format=xml";

	private static final String MAPPING_FILE = "res/mapping.properties";
	private static final String MAPPING_API_FILE = "res/mapping_api.properties";
	
	private static HashMap<String, String> mapping = new HashMap<String, String>();
	
	private static HashMap<String, String> mappingAPI = new HashMap<String, String>();

	private Model dataModel_ = ModelFactory.createDefaultModel();
	
	private Model resultModel_ = ModelFactory.createDefaultModel();
	
	public Document getData() throws JDOMException, IOException {
		InputStream stream = null;
		
		try {
			URL url = new URL(DATA_URL);
			URLConnection connection = url.openConnection();
	
			stream = connection.getInputStream();
		}
		
		catch(UnknownHostException e) {
			stream = new FileInputStream(LOCAL_DATASET);
		}
		
		return new SAXBuilder().build(stream);
	}

	/*
	 * public void writeXml() throws JDOMException, IOException { XMLOutputter
	 * sortie = new XMLOutputter(Format.getPrettyFormat());
	 * sortie.output(this.getXml(), new FileOutputStream(new File(OUTPUT_XML)));
	 * }
	 */

	private void loadMappings() throws FileNotFoundException, IOException {
		Properties prop = new Properties();
		prop.load(new FileReader(MAPPING_FILE));

		for (Object key : prop.keySet()) {
			mapping.put(key.toString(), prop.getProperty(key.toString()));
		}
		
		prop = new Properties();
		prop.load(new FileReader(MAPPING_API_FILE));

		for (Object key : prop.keySet()) {
			mappingAPI.put(key.toString(), prop.getProperty(key.toString()));
		}
	}

	public void toRdf() throws JDOMException, IOException {
		File rdfFile = new File(RDF_FILE);

		if (rdfFile.exists() == false) {
			dataModel_.setNsPrefix(ONTO_PREFIX, ONTO_URL);

			Document xml = this.getData();
			Element root = xml.getRootElement();

			List<Element> listeActi = root.getChild("data").getChildren("element");

			// Every <element> matches with an activity
			for (Element activity : listeActi) {
				Resource res = null;

				// Every java property matches with an RDF property
				for (String key : mapping.keySet()) {
					Element current = activity;

					String value = mapping.get(key);

					// Several elements (paths) which fit an RDF property
					// are separated by a coma
					String[] paths = value.split(",");

					for (int p = 0; p < paths.length; p++) {
						String path = paths[p];
						String[] elements = path.split("\\.");

						for (int e = 0; e < elements.length && current != null; e++) {
							current = current.getChild(elements[e]);
						}

						if (current != null) {
							String text = current.getValue();

							if (text.equals("null") == false) {
								if (key.equals("hasName")) {
									String id = text.replace(' ', '_').replace(
											'\'', '_');

									res = dataModel_.createResource(ONTO_URL + id);
								}

								res.addProperty(
										dataModel_.createProperty(ONTO_URL, key),
										text);
							}
						}
					}
				}
			}

			try {
				dataModel_.write(new FileOutputStream(RDF_FILE));
			}
			
			catch (FileNotFoundException e) {
				System.err.println("Erreur : ecriture du graphe RDF des donnees echouee.");
				System.err.println(e.getMessage());
			}
		}

		else {
			InputStream input = FileManager.get().open(RDF_FILE);
			dataModel_.read(input, "TURTLE");
		}
	}

	public Model query1(String queryString) throws JDOMException, IOException {
		resultModel_.removeAll();

		this.toRdf();

		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, dataModel_);
		ResultSet results = qexec.execSelect();
		
		PrefixMapping mapping = query.getPrefixMapping();
		Map<String, String> prefixes = mapping.getNsPrefixMap();

		for (String prefix : prefixes.keySet()) {
			resultModel_.setNsPrefix(prefix, prefixes.get(prefix));
		}

		ElementGroup eltGroup = (ElementGroup) query.getQueryPattern();
		ElementPathBlock pathBlock = (ElementPathBlock) eltGroup.getElements()
				.get(0);

		while (results.hasNext()) {
			QuerySolution sol = results.next();

			Iterator<TriplePath> it = pathBlock.patternElts();

			Node s;
			Node o;
			Resource res;
			Property prop;

			while (it.hasNext()) {
				TriplePath queryTriple = it.next();

				s = queryTriple.getSubject();
				o = queryTriple.getObject();

				res = resultModel_.createResource(sol.getResource(s.getName())
						.getURI());
				prop = resultModel_.createProperty(queryTriple.getPredicate()
						.getURI());

				res.addProperty(prop, o.getName());
			}
		}

		return resultModel_;
	}
	
	// Evalue les triples de la requete un par un
	// et stocke les valeurs possibles de chaque variable rencontree.
	// Si des valeurs sont deja stockees pour une variable, alors on garde
	// l'intersection des 2 ensembles 
	public Model query2(String queryString) throws JDOMException, IOException {
		Map<String, Set<Node>> varValues = new Hashtable<String, Set<Node>>();
		
		resultModel_.removeAll();
		
		Query query = QueryFactory.create(queryString);

		PrefixMapping pm = query.getPrefixMapping();
		Map<String, String> truc = pm.getNsPrefixMap();

		for (String key : truc.keySet()) {
			resultModel_.setNsPrefix(key, truc.get(key));
		}

		ElementGroup eltGroup = (ElementGroup) query.getQueryPattern();
		ElementPathBlock pathBlock = (ElementPathBlock) eltGroup.getElements()
				.get(0);
		
		ArrayList<TriplePath> triples = new ArrayList<TriplePath>();
		Iterator<TriplePath> it = pathBlock.patternElts();
		
		while(it.hasNext()) {
			this.pouetV2(it.next(), varValues);
		}
		
		Set<Node> setSubject = varValues.get("x");
		
		if(setSubject != null) {
			for(Node s : setSubject) {
				System.out.println(s);
			}
		}
		
		return resultModel_;
	}
	
	// Evalue un triple sur les donnees et met à jour
	// les ensembles de valeurs des variables concernees
	private void pouet(TriplePath triple, Map<String, Set<Node>> varValues)
			throws JDOMException, IOException {
		
		Node s = triple.getSubject();
		Node o = triple.getObject();
		
		System.out.println(triple);
		
		Property prop = resultModel_.createProperty(
				triple.getPredicate().getURI());
		
		if(o.isVariable()) {
			Set<Node> setObject = new HashSet<Node>();
			
			// On recupère les activites qui ont la propriete recherchee
			// ResIterator resIt = dataModel_.listResourcesWithProperty(prop);
			ResIterator resIt = null;
			Set<Triple> results = methode1(prop);
			
			if(s.isVariable()) {
				Set<Node> setSuject = new HashSet<Node>();
				
				while(resIt.hasNext()) {
					Resource res = resIt.next();
					setSuject.add(res.asNode());
					
					Statement stmt = res.getProperty(prop);
					Node nodeObject = stmt.getObject().asNode();
					setObject.add(nodeObject);
				}
				
				if(varValues.containsKey(s.getName())) {
					Set<Node> oldValues = varValues.get(s.getName());
					Set<Node> newValues = new HashSet<Node>();
					
					for(Node value : oldValues) {
						if(setSuject.contains(value) == true) {
							newValues.add(value);
						}
					}
					
					varValues.put(s.getName(), newValues);
				}
				
				else {
					varValues.put(s.getName(), setSuject);
				}
			}
			
			if(varValues.containsKey(o.getName())) {
				Set<Node> oldValues = varValues.get(o.getName());
				Set<Node> newValues = new HashSet<Node>();
				
				for(Node value : oldValues) {
					if(setObject.contains(value) == true) {
						newValues.add(value);
					}
				}
				
				varValues.put(s.getName(), newValues);
			}
			
			else {
				varValues.put(o.getName(), setObject);
			}
		}
		
		else {
			// On recupère les activites qui ont la propriete recherchee à la bonne valeur
			ResIterator resIt = null;
			
			if(o.isLiteral()) {
				resIt = dataModel_.listResourcesWithProperty(prop, o.getLiteralValue());
			}
			
			else {
				resIt = dataModel_.listResourcesWithProperty(prop, o.getURI());
			}
			
			Set<Node> setSuject = new HashSet<Node>();
			
			while(resIt.hasNext()) {
				Resource res = resIt.next();
				setSuject.add(res.asNode());
			}
			
			if(varValues.containsKey(s.getName())) {
				Set<Node> oldValues = varValues.get(s.getName());
				Set<Node> newValues = new HashSet<Node>();
				
				for(Node value : oldValues) {
					if(setSuject.contains(value) == true) {
						newValues.add(value);
					}
				}
				
				varValues.put(s.getName(), newValues);
			}
			
			else {
				varValues.put(s.getName(), setSuject);
			}
		}
	}
	
	private void pouetV2(TriplePath triple, Map<String, Set<Node>> varValues)
			throws JDOMException, IOException {
		
		Node s = triple.getSubject();
		Node o = triple.getObject();
		
		System.out.println(triple);
		
		Property prop = resultModel_.createProperty(
				triple.getPredicate().getURI());
		
		if(o.isVariable()) {
			Set<Node> setObject = new HashSet<Node>();
			
			// On recupère les activites qui ont la propriete recherchee
			Set<Triple> results = methode1(prop);
			Iterator<Triple> resIt = results.iterator();
			
			if(s.isVariable()) {
				Set<Node> setSubject = new HashSet<Node>();
				
				while(resIt.hasNext()) {
					Triple res = resIt.next();
					Node nextValue = res.getSubject();
					setSubject.add(nextValue);
					
					Node nodeObject = res.getObject();
					setObject.add(nodeObject);
				}
				
				if(varValues.containsKey(s.getName())) {
					Set<Node> oldValues = varValues.get(s.getName());
					Set<Node> newValues = new HashSet<Node>();
					
					for(Node value : oldValues) {
						if(setSubject.contains(value) == true) {
							newValues.add(value);
						}
					}
					
					varValues.put(s.getName(), newValues);
				}
				
				else {
					varValues.put(s.getName(), setSubject);
				}
			}
			
			if(varValues.containsKey(o.getName())) {
				Set<Node> oldValues = varValues.get(o.getName());
				Set<Node> newValues = new HashSet<Node>();
				
				for(Node value : oldValues) {
					if(setObject.contains(value) == true) {
						newValues.add(value);
					}
				}
				
				varValues.put(s.getName(), newValues);
			}
			
			else {
				varValues.put(o.getName(), setObject);
			}
		}
		
		else {
			// On recupère les activites qui ont la propriete recherchee à la bonne valeur
			ResIterator resIt = null;
			
			if(o.isLiteral()) {
				resIt = dataModel_.listResourcesWithProperty(prop, o.getLiteralValue());
			}
			
			else {
				resIt = dataModel_.listResourcesWithProperty(prop, o.getURI());
			}
			
			Set<Node> setSuject = new HashSet<Node>();
			
			while(resIt.hasNext()) {
				Resource res = resIt.next();
				setSuject.add(res.asNode());
			}
			
			if(varValues.containsKey(s.getName())) {
				Set<Node> oldValues = varValues.get(s.getName());
				Set<Node> newValues = new HashSet<Node>();
				
				for(Node value : oldValues) {
					if(setSuject.contains(value) == true) {
						newValues.add(value);
					}
				}
				
				varValues.put(s.getName(), newValues);
			}
			
			else {
				varValues.put(s.getName(), setSuject);
			}
		}
	}
	
	/**
	 * Retourne les elements des donnees XML
	 * qui possèdent la propriete passee en parametre.
	 * 
	 * @param prop
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 */
	private Set<Triple> methode1(Property prop) throws JDOMException, IOException {	// TODO Reprendre ici
		Set<Triple> results = new HashSet<Triple>();
		
		Model mod = ModelFactory.createDefaultModel();
		mod.setNsPrefixes(dataModel_.getNsPrefixMap());
		
		Document doc = this.getData();
		Element root = doc.getRootElement();

		List<Element> listeActi = root.getChild("data").getChildren("element");
		
		String truc = mapping.get(prop.getLocalName());
		String[] paths = truc.split(",");
		
		Iterator<Element> actiIt = listeActi.iterator();
		
		int cnt = 0;
		
		while(actiIt.hasNext()) {
			Element current = actiIt.next();
			
			for (int idxPath = 0; idxPath < paths.length; idxPath++) {
				String path = paths[idxPath];
				String[] elements = path.split("\\.");
				
				for (int idxElement = 0; idxElement < elements.length && current != null; idxElement++) {
					current = current.getChild(elements[idxElement]);
				}
				
				if(current != null) {
					Node s = Node.createURI(ONTO_URL + "activity" + (++cnt));
					Node p = Node.createURI(prop.getURI());
					Node o = Node.createLiteral(current.getValue());
					
					Triple triple = new Triple(s, p, o); 
					results.add(triple);
					System.out.println("DBG triple : " + triple);
				}
			}
		}
		
		return results;
	}
	
	public static void main(String[] args) {
		Wrapper w = new Wrapper();

		System.setProperty("http.proxyHost", "cache.sciences.univ-nantes.fr");
		System.setProperty("http.proxyPort", "3128");
		
		try {
			w.loadMappings();
			// Model m1 = w.query1("prefix onto: <http://example.org/> SELECT ?x ?ad ?pc ?town WHERE{ ?x onto:hasAddress ?ad; onto:hasPostalCode ?pc; onto:hasTown ?town.}");
			Model m2 = w.query2("prefix onto: <http://example.org/> "
					+ "SELECT ?x ?ad WHERE{ ?x onto:hasAddress ?ad ; onto:acceptVisualImpairment \"oui\"}");
		}
		
		catch(IOException e) {
			System.err.println("Erreur : IOException.");
			System.err.println(e.getMessage());
			System.err.println(e.getClass());
		}
		
		catch(JDOMException e) {
			System.err.println("Erreur : JDOMException.");
			System.err.println(e.getMessage());
		}
	}
}
