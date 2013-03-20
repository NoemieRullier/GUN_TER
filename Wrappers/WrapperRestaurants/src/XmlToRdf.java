//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URL;
//import java.net.URLConnection;
//import java.net.UnknownHostException;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//import java.util.Set;
//
//import org.jdom2.Document;
//import org.jdom2.Element;
//import org.jdom2.JDOMException;
//import org.jdom2.input.SAXBuilder;
//
//import com.hp.hpl.jena.graph.Node;
//import com.hp.hpl.jena.graph.Triple;
//import com.hp.hpl.jena.query.Query;
//import com.hp.hpl.jena.query.QueryFactory;
//import com.hp.hpl.jena.rdf.model.Model;
//import com.hp.hpl.jena.rdf.model.ModelFactory;
//import com.hp.hpl.jena.rdf.model.Property;
//import com.hp.hpl.jena.rdf.model.Resource;
//import com.hp.hpl.jena.shared.PrefixMapping;
//import com.hp.hpl.jena.sparql.core.TriplePath;
//import com.hp.hpl.jena.sparql.syntax.ElementGroup;
//import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
//
//
//public class XmlToRdf {
//	
//public class UnknownProperty extends Exception {
//		
//		private static final long serialVersionUID = -2154805128624019618L;
//
//		public UnknownProperty(String name) {
//			System.err.println("Unknown property (" + name + ")");
//		}
//	}
//	
//	/**
//	 * Our namespace prefix.
//	 */
//	private static final String ONTO_PREFIX = "onto";
//
//	/**
//	 * Our namespace.
//	 */
//	private static final String ONTO_URL = "http://example.org/";
//
//	/**
//	 * API URL.
//	 */
//	private static final String API_URL = "https://data.nantes.fr/api/publication/"
//			+ "22440002800011_CG44_TOU_04812/"
//			+ "activites_tourisme_et_handicap_STBL/content";
//
//	/**
//	 * Ontology / XML data mapping file path.
//	 */
//	private static final String MAPPING_FILE = "res/mapping.properties";
//
//	/**
//	 * Ontology / API mapping file path.
//	 */
//	private static final String MAPPING_API_FILE = "res/mapping_api.properties";
//
//	/**
//	 * Ontology / XML data mapping.
//	 */
//	private HashMap<String, String> mapping_ = new HashMap<String, String>();
//
//	/**
//	 * Ontology / API mapping.
//	 */
//	private HashMap<String, String> mappingAPI_ = new HashMap<String, String>();
//
//	/**
//	 * Filters to add to API call
//	 */
//	private HashMap<String, String> filters_ = new HashMap<String, String>();
//	
//	/**
//	 * Query triples which do not contain literal.
//	 * 
//	 * <p>Triples that will be evaluated on the data returned by the API.</p> 
//	 */
//	private LinkedList<Triple> triples_ = new LinkedList<Triple>();
//
//	/**
//	 * HTTP Request to the API
//	 */
//	private String apiCall_ = new String(API_URL);
//
//	/**
//	 * XML data returned by the API.
//	 */
//	private Document apiData_;
//
//	private Model resModel_ = ModelFactory.createDefaultModel();
//
//	/**
//	 * Loads mappings files.
//	 * 
//	 * @throws FileNotFoundException
//	 *             Whether a mapping file is missing
//	 * @throws IOException
//	 *             Whether reading files fails
//	 */
//	private void loadMappings() throws FileNotFoundException, IOException {
//		Properties prop = new Properties();
//		prop.load(new FileReader(MAPPING_FILE));
//
//		for (Object key : prop.keySet()) {
//			this.mapping_.put(key.toString(), prop.getProperty(key.toString()));
//		}
//
//		prop = new Properties();
//		prop.load(new FileReader(MAPPING_API_FILE));
//
//		for (Object key : prop.keySet()) {
//			this.mappingAPI_.put(key.toString(),
//					prop.getProperty(key.toString()));
//		}
//	}
//
//	/**
//	 * Extracts triples from query.
//	 * 
//	 * @param qString
//	 *            A conjunctive SPARQL query
//	 */
//	private void loadQuery(String qString) {
//		Query query = QueryFactory.create(qString);
//
//		PrefixMapping pm = query.getPrefixMapping();
//		Map<String, String> truc = pm.getNsPrefixMap();
//
//		for (String key : truc.keySet()) {
//			this.resModel_.setNsPrefix(key, truc.get(key));
//		}
//
//		ElementGroup eltGroup = (ElementGroup) query.getQueryPattern();
//		ElementPathBlock pathBlock = (ElementPathBlock) eltGroup.getElements()
//				.get(0);
//
//		Iterator<TriplePath> triplesIt = pathBlock.patternElts();
//
//		while (triplesIt.hasNext()) {
//			TriplePath tp = triplesIt.next();
//			Node object = tp.getObject();
//
//			if (object.isLiteral()) {
//				Node property = tp.getPredicate();
//
//				this.filters_.put(property.getLocalName(), object.getLiteral()
//						.toString());
//			}
//
//			else {
//				this.triples_.push(tp.asTriple());
//			}
//		}
//	}
//
//	private void buildApiCall() {
//		Boolean filter = false;
//
//		if (this.filters_.isEmpty() == false) {
//			filter = true;
//			this.apiCall_ += "?filter={"; // Filter start
//
//			Set<String> keys = this.filters_.keySet();
//
//			if (keys.size() > 1) {
//				this.apiCall_ += "\"$and\":[";
//
//				Boolean first = true;
//
//				for (String key : keys) {
//					String apiField = this.mappingAPI_.get(key);
//					String value = this.filters_.get(key);
//
//					if (first == false) {
//						this.apiCall_ += ",";
//					}
//
//					else {
//						first = false;
//					}
//
//					this.apiCall_ += "{\"" + apiField + "\":{";
//					this.apiCall_ += "\"$eq\":\"" + value + "\"";
//					this.apiCall_ += "}}";
//				}
//
//				this.apiCall_ += "]";
//			}
//
//			else {
//				String key = keys.iterator().next();
//				String apiField = mappingAPI_.get(key);
//				String value = filters_.get(key);
//
//				this.apiCall_ += "\"" + apiField + "\":{";
//				this.apiCall_ += "\"$eq\":\"" + value + "\"";
//				this.apiCall_ += "}";
//			}
//
//			this.apiCall_ += "}"; // Filter end
//		}
//
//		if (filter == true) {
//			this.apiCall_ += "&format=xml";
//		}
//
//		else {
//			this.apiCall_ += "?format=xml";
//		}
//
//		System.out.println("DBG API call : " + this.apiCall_);
//	}
//
//	private void getData() throws IOException, JDOMException {
//		InputStream stream = null;
//		URL url = new URL(this.apiCall_);
//		URLConnection connection = url.openConnection();
//
//		stream = connection.getInputStream();
//
//		this.apiData_ = new SAXBuilder().build(stream);
//	}
//
//	private void processQuery()
//			throws IOException, JDOMException, UnknownProperty {
//		
//		this.getData();
//
//		for (Triple triple : this.triples_) {
//			Node s = triple.getSubject();
//			Node p = triple.getPredicate();
//			Node o = triple.getObject();
//			
//			Element root = this.apiData_.getRootElement();
//			List<Element> listeActi = root.getChild("data").getChildren("element");
//
//			String truc = mapping_.get(p.getLocalName());
//			
//			if(truc == null) {
//				throw new UnknownProperty(p.getLocalName() + ")");
//			}
//			
//			String[] paths = truc.split(",");
//
//			Iterator<Element> actiIt = listeActi.iterator();
//
//			int cnt = 0;
//
//			while (actiIt.hasNext()) {
//				Element current = actiIt.next();
//				cnt++;
//				
//				for (int idxPath = 0; idxPath < paths.length; idxPath++) {
//					String path = paths[idxPath];
//					String[] elements = path.split("\\.");
//
//					for (int idxElement = 0; idxElement < elements.length
//							&& current != null; idxElement++) {
//						current = current.getChild(elements[idxElement]);
//					}
//
//					if ((current != null)
//							&& (current.getValue().equals("null") == false)) {
//						
//						Resource res = resModel_.createResource(
//										ONTO_URL + "activityLocation" + cnt);
//						Property prop = resModel_.createProperty(p.getURI());
//						
//						res.addProperty(prop, current.getValue());
//					}
//				}
//			}
//		}
//	}
//	
//	public void query(String qString)
//			throws IOException, JDOMException, UnknownProperty {
//		
//		this.loadQuery(qString);
//		this.buildApiCall();
//		this.processQuery();
//	}
//	
//	public static void main(String[] args) {
//		System.setProperty("http.proxyHost", "cache.sciences.univ-nantes.fr");
//		System.setProperty("http.proxyPort", "3128");
//
//		WrapperV2 w2 = new WrapperV2();
//
//		try {
//			w2.loadMappings();
//		}
//
//		catch (FileNotFoundException e) {
//			System.err.println("Error : mapping file not found.");
//		}
//
//		catch (IOException e) {
//			System.err.println("Error : reading mapping file failed.");
//		}
//
//		try {
//			w2.query("prefix onto: <http://example.org/> SELECT ?x ?ad ?pc ?to "
//					+ "WHERE{ ?x onto:hasAddress ?ad ; "
//					+ "onto:hasPostalCode ?pc ; "
//					+ "onto:hasTown ?to }");
//			
////			w2.query("prefix onto: <http://example.org/> SELECT ?x ?mail ?web "
////					+ "WHERE{ ?x onto:hasMail ?mail ; "
////					+ "onto:hasWebsite ?web }");
//			
////			w2.query("prefix onto: <http://example.org/> SELECT ?x ?vi ?hi "
////					+ "WHERE{ ?x onto:acceptVisualImpairment ?vi ; "
////					+ "onto:acceptHearingImpairment ?hi }");
//			
//			System.out.println("\nDBG result :");
//			w2.resModel_.write(System.out);
//		}
//		
//		catch(UnknownHostException e) {
//			System.err.println("Erreur : connection au webservice echouee. "
//					+ "Nom d'hote inconnu. Veuillez verifier "
//					+ "votre connexion.");
//		}
//		
//		catch (IOException e) {}
//
//		catch (JDOMException e) {}
//		
//		catch (UnknownProperty e) {}
//	}
//	
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//
//	}
//
//}
