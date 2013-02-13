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


public class ApiToRdf {
	
	public String api = "http://data.nantes.fr/api/publication/22440002800011_CG44_TOU_04820/restaurants_STBL/content?format=csv";
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
		m.setNsPrefix("geo", restaurantP);
		
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
		HashMap<Integer, Property> mapping = new HashMap<Integer, Property>();
		Properties map = new Properties();
		map.load(new FileReader(fichierMapping));
		for (Property p : propertyOntologieGlobale){
			for (Object o : map.keySet()){
				if (o.toString().equals(p.getLocalName())){
					mapping.put(Integer.parseInt(map.getProperty(o.toString()))-1, p);
				}
			}
		}
		
		/*FileInputStream file = new FileInputStream(api);
		Reader reader = new InputStreamReader(file, "utf-8");
		BufferedReader br = new BufferedReader(reader);*/
		
		URL url = new URL(api);
		InputStream file = url.openStream();
		Reader reader = new InputStreamReader(file, "utf-8");
		BufferedReader br = new BufferedReader(reader);
		
		// TODO Réfléchir à comment on va faire
		
		// Number of property in the file
		// TODO A voir si on en a encore besoin
		int nbProperty = 0;

		String row = null;
		String[] data = {};

		// Separator
		Pattern p = Pattern.compile(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

		// The first row with property
		if((row = br.readLine()) != null) {
			// TODO A voir si on garde le +1
			nbProperty = row.split(",").length+1;
		}

/*		int nbRow = 1;
		while ((row = br.readLine()) != null)
		{
			// We put the row in the tab
			data = p.split(row);
		
			// Create the resource correspond to the row
			Resource resource = m.createResource(restaurantP+data[0].substring(1, data[0].length()-1));
			Resource address = null;
			int i = 0;
			for (String val : data){
				// If the column is the [latitude, longitude]
				if(i % nbProperty == 10){
					// Create the resource coordinate
					Resource coord = m.createResource(geoP+nbRow);
					
					// We get the latitude and longitude
					val = val.split("]")[0].substring(2);
					// Add the property to the latitude
					m.add(coord, propertyGeoCoordinates.get(0), val.split(",")[0]);
					// Add the property to the longitude
					m.add(coord, propertyGeoCoordinates.get(1), val.split(",")[1]);
					
					// Add the property of coordinates
					m.add(resource, relation.get(i), coord);
				}
				// If the column is the payment accepted
				else if (i % nbProperty == 19){
					// TODO: revoir multiligne
					// TODO: A revoir pour le formattage des moyen de paiements car c'est du n'importe quoi !!! 
					// Carte Bancaire ou CB Majuscule ou non --> Pb Rspèce
					// Separateur --> Ils sont différents !!!!!
					for(String pa: val.substring(1, val.length()-1).replace(" ","").split("[.,\\-]")){
						m.add(resource, relation.get(i), pa.toUpperCase());
					}
				}
				else {
					// If the column is a property of address
					if ((i % nbProperty == 4) || (i % nbProperty == 6) || (i % nbProperty == 9)){
						if (address == null){
							address = m.createResource(postalAddressP+nbRow);
						}
						m.add(address, relation.get(i), val.substring(1, val.length()-1));
						if (i % nbProperty == 9){
							m.add(resource, propertyOntologieGlobale.get(4), address);
						}
					}
					// All the other property
					else {
						m.add(resource, relation.get(i), val.substring(1, val.length()-1));
					}
				}
				i++;
			}
			nbRow++;
		}
		br.close();*/
		m.write(System.out,"TURTLE");
	}
	
	
	public static void main(String[] args) {
		ApiToRdf v1 = new ApiToRdf("SELECT ?x ?ad ?pc ?town WHERE{ ?x onto:hasAdrress ?ad; onto:hasPostalCode ?pc; onto;hasTown ?town.}");
		try {
			v1.parsingFile();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
