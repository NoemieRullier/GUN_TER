import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Pattern;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;


public class ApiToRdf {

	public String api = "http://data.nantes.fr/api/publication/22440002800011_CG44_TOU_04820/restaurants_STBL/content?format=csv";
	public String apiFile = "../../DataSets/22440002800011_CG44_TOU_04820_restaurants_STBL.csv";
	public String fichierMapping = "src/fichierMapping.txt";
	public Query reqVue;
	public String vue;

	public ApiToRdf(String v) {
		vue = v;
		reqVue = QueryFactory.create(vue);
	}

	public void parsingFile() throws MalformedURLException, IOException{

		// Create the model of RDF-Graph
		Model m = ModelFactory.createDefaultModel();
		
		// Create the URI
		String ontoP = "http://example.org/";
		String restaurantP = "http://example.org/Restaurant/";

		// Define prefix
		m.setNsPrefix("onto", ontoP);
		m.setNsPrefix("res", restaurantP);

		// Define property
		ArrayList<Property> propertyOntologieGlobale = new ArrayList<Property>();
		propertyOntologieGlobale.add(m.createProperty(ontoP + "hasName" ));
		propertyOntologieGlobale.add(m.createProperty(ontoP + "hasAddress" ));
		propertyOntologieGlobale.add(m.createProperty(ontoP + "hasPostalCode" ));
		propertyOntologieGlobale.add(m.createProperty(ontoP + "hasTown" ));
		propertyOntologieGlobale.add(m.createProperty(ontoP + "hasWebSite" ));
		propertyOntologieGlobale.add(m.createProperty(ontoP + "hasMail" ));
		propertyOntologieGlobale.add(m.createProperty(ontoP + "acceptVisualImpairment" ));
		propertyOntologieGlobale.add(m.createProperty(ontoP + "acceptHearingImpairment" ));

		// Loading of mapping

		Properties map = new Properties();
		map.load(new FileReader(fichierMapping));
		HashMap<Property, Integer> mapping = new HashMap<Property, Integer>();
		HashMap<Integer, Property> mappingI = new HashMap<Integer, Property>();
		for (Property p : propertyOntologieGlobale){
			for (Object o : map.keySet()){
				if (o.toString().equals(p.getLocalName())){
					mapping.put(p, Integer.parseInt(map.getProperty(o.toString()))-1);
					mappingI.put(Integer.parseInt(map.getProperty(o.toString()))-1, p);
				}
			}
		}

//		FileInputStream file = new FileInputStream(apiFile);

		URL url = new URL(api);
		InputStream file = url.openStream();
		Reader reader = new InputStreamReader(file, "utf-8");
		BufferedReader br = new BufferedReader(reader);

		// Loading the columns we need and the value they must respect
		HashMap<Integer, String> req = new HashMap<Integer, String>();
		// Pas tr�s propre --> A revoir si on peut pas r�cup�rer les propri�t�s de la query un peu mieux
		for(String s: reqVue.getQueryPattern().toString().replace("{", " ").replace("}", "").replace("\n", "").split(" \\.")){
			req.put(mapping.get(m.getProperty(s.split(" ")[3].replace("<","").replace(">", ""))), s.split(" ")[4]);
		}

		// Ligne interressante de la Query
		//System.out.println(reqVue.getPrefixMapping());
		//System.out.println(reqVue.getProjectVars());
		//System.out.println(reqVue.getResultVars());
		//System.out.println(reqVue.getQueryPattern());

		String row = null;
		String[] data = {};

		// Separator
		Pattern p = Pattern.compile(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

		row = br.readLine();
		while ((row = br.readLine()) != null)
		{
			// We put the row in the tab
			data = p.split(row);

			// Create the resource correspond to the row
			Resource resource = null;

			// Add the different traitement of the special column --> Mais �a marche pour le moment

			// The number of the column
			boolean ajouter = true;
			for(Integer i : req.keySet()){
				if ((req.get(i).contains("?") && data[i].equals("\"\"")) || (data[i] == req.get(i)) ){
					ajouter = false;
				}
			}
			for(Integer i : req.keySet()){
				if (ajouter){
					resource = m.createResource(restaurantP+data[0].substring(1, data[0].length()-1));
					m.add(resource, mappingI.get(i), data[i].substring(1, data[i].length()-1));
				}
			}
		}

		// If the column is the [latitude, longitude]
		/*if(i == 33){

					// We get the latitude and longitude
					val = val.split("]")[0].substring(2);
					// Add the property to the latitude
					m.add(coord, propertyGeoCoordinates.get(0), val.split(",")[0]);
					// Add the property to the longitude
					m.add(coord, propertyGeoCoordinates.get(1), val.split(",")[1]);

					// Add the property of coordinates
					m.add(resource, relation.get(i), coord);
				}*/
		// If the column is the payment accepted
		/*else if (i % nbProperty == 19){
					// TODO: revoir multiligne
					// TODO: A revoir pour le formattage des moyen de paiements car c'est du n'importe quoi !!! 
					// Carte Bancaire ou CB Majuscule ou non --> Pb Rsp�ce
					// Separateur --> Ils sont diff�rents !!!!!
					for(String pa: val.substring(1, val.length()-1).replace(" ","").split("[.,\\-]")){
						m.add(resource, relation.get(i), pa.toUpperCase());
					}
				}*/
		br.close();
		m.write(System.out,"N-TRIPLE");
	}

	public static void main(String[] args) {
		ApiToRdf v1 = new ApiToRdf("prefix onto: <http://example.org/> SELECT ?x ?ad ?pc ?town WHERE{ ?x onto:hasAddress ?ad; onto:hasPostalCode ?pc; onto:hasTown ?town.}");
		ApiToRdf v2 = new ApiToRdf("prefix onto: <http://example.org/> SELECT ?x ?mail ?ws WHERE{ ?x onto:hasMail ?mail; onto:hasWebSite ?ws.}");
		ApiToRdf v3 = new ApiToRdf("prefix onto: <http://example.org/> SELECT ?x ?vi ?hi ?town WHERE{ ?x onto:acceptVisualImpairment ?vi; onto:acceptHearingImpairment ?hi.}");
		try {
			v1.parsingFile();
//			v2.parsingFile();
//			v3.parsingFile();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
