import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
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


public class WrapperRestaurantApiToRdf {

	public String api = "http://data.nantes.fr/api/publication/22440002800011_CG44_TOU_04820/restaurants_STBL/content?format=csv";
	public String apiFile = "../../DataSets/22440002800011_CG44_TOU_04820_restaurants_STBL.csv";
	public String fichierMapping = "fileMappingRestaurant.txt";
	public String pathFileResult;
	public Query reqVue;
	public String vue = "";

	public WrapperRestaurantApiToRdf(String pathFileView, String pathFileResult){
		System.setProperty("http.proxyHost", "cache.etu.univ-nantes.fr");
		System.setProperty("http.proxyPort", "3128");
		InputStream file;
		try {
			file = new FileInputStream(pathFileView);
			Reader reader = new InputStreamReader(file, "utf-8");
			BufferedReader readView = new BufferedReader(reader);
			String ligne;
			while ((ligne=readView.readLine())!=null){
				System.out.println(ligne);
				vue+=ligne/*+"\n"*/;
			}
			reader.close(); 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reqVue = QueryFactory.create(vue);
		this.pathFileResult = pathFileResult;  
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
		map.load(getClass().getResourceAsStream(fichierMapping));
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
		// Pas tres propre --> A revoir si on peut pas recuperer les proprietes de la query un peu mieux
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
		m.write(new FileOutputStream(pathFileResult),"N-TRIPLE");
	}

	public static void main(String[] args) {
		if (args.length == 2){
			WrapperRestaurantApiToRdf v = new WrapperRestaurantApiToRdf(args[0], args[1]);
			//			WrapperRestaurantApiToRdf v1 = new WrapperRestaurantApiToRdf("src/view1.sparql","src/view1.n3");
			//			WrapperRestaurantApiToRdf v2 = new WrapperRestaurantApiToRdf("src/view2.sparql","src/view2.n3");
			//			WrapperRestaurantApiToRdf v3 = new WrapperRestaurantApiToRdf("src/view3.sparql","src/view3.n3");
			//			WrapperRestaurantApiToRdf v4 = new WrapperRestaurantApiToRdf("src/view4.sparql","src/view4.n3");

			try {
				v.parsingFile();
				//				v1.parsingFile();
				//				v2.parsingFile();
				//				v3.parsingFile();
				//				v4.parsingFile();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			System.out.println("Incorrect number of parameters");
		}
	}
}
