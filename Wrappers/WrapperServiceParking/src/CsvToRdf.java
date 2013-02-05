import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;


public class CsvToRdf {

	public String fileCsv  = "../../DataSets/LISTE_SERVICES_PKGS_PUB_NANTES_LISTE_SERVICES_PKGS_PUB_NM_STBL.csv";
	
	public void parsingFile() throws MalformedURLException, IOException{

		// Create the model of RDF-Graph
		Model m = ModelFactory.createDefaultModel();
		
		// Create the URI
		String schemaP = "http://example.org/";
		String geoP = "http://example.org/GeoCoordinates/";
		String postalAddressP = "http://example.org/PostalAddress/";
		String parkingP = "http://example.org/Parking/";
		
		// Define prefix
		m.setNsPrefix("schema", schemaP);
		m.setNsPrefix("geo", geoP);
		m.setNsPrefix("postalAddress", postalAddressP);
		m.setNsPrefix("parking", parkingP);
		
		// Define property
		ArrayList<Property> propertyParking = new ArrayList<Property>();
		propertyParking.add(m.createProperty(parkingP + "id" ));
		propertyParking.add(m.createProperty(parkingP + "name" ));
		propertyParking.add(m.createProperty(parkingP + "libCategorie" ));
		propertyParking.add(m.createProperty(parkingP + "libType" ));
		propertyParking.add(m.createProperty(parkingP + "address" ));
		propertyParking.add(m.createProperty(parkingP + "townCode" ));
		propertyParking.add(m.createProperty(parkingP + "phone" ));
		propertyParking.add(m.createProperty(parkingP + "webSite" ));
		propertyParking.add(m.createProperty(parkingP + "geo" ));
		propertyParking.add(m.createProperty(parkingP + "presentation" ));
		propertyParking.add(m.createProperty(parkingP + "capacityCar" ));
		propertyParking.add(m.createProperty(parkingP + "capacityPmr" ));
		propertyParking.add(m.createProperty(parkingP + "capacityElectricCar" ));
		propertyParking.add(m.createProperty(parkingP + "capacityBike" ));
		propertyParking.add(m.createProperty(parkingP + "serviceBike" ));
		propertyParking.add(m.createProperty(parkingP + "otherMobilityProximityService" ));
		propertyParking.add(m.createProperty(parkingP + "services" ));
		propertyParking.add(m.createProperty(parkingP + "paymentAccepted" ));
		propertyParking.add(m.createProperty(parkingP + "accessConditions" ));
		propertyParking.add(m.createProperty(parkingP + "exploiting" ));
		
		ArrayList<Property> propertyAddressPostal = new ArrayList<Property>();
		propertyAddressPostal.add(m.createProperty(postalAddressP + "streetAddress" ));
		propertyAddressPostal.add(m.createProperty(postalAddressP + "addressLocality" ));
		propertyAddressPostal.add(m.createProperty(postalAddressP + "postalCode" ));
		
		ArrayList<Property> propertyGeoCoordinates = new ArrayList<Property>();
		propertyGeoCoordinates.add(m.createProperty(geoP + "latitude" ));
		propertyGeoCoordinates.add(m.createProperty(geoP + "longitude" ));
		
		// Number of property in the file
		int nbProperty = 0;

		FileInputStream file = new FileInputStream(fileCsv);
		Reader reader = new InputStreamReader(file, "utf-8");
		BufferedReader br = new BufferedReader(reader);

		String row = null;
		String[] data = {};

		// Separator
		Pattern p = Pattern.compile(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

		// The first row with property
		if((row = br.readLine()) != null) {
			nbProperty = row.split(",").length+1;
		}

		// TODO: A revoir si on laisse ou pas!!!
		String[] donnees = new String[nbProperty];
		int nbRow = 1;
		while ((row = br.readLine()) != null)
		{
			// We put the row in the tab
			data = p.split(row);
						
			// Create the resource correspond to the row
			Resource resource = m.createResource(parkingP+data[0].substring(1, data[0].length()-1));
			
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
					m.add(resource, propertyParking.get(i), coord);
				}
				// If the column is the payment accepted
				if (i % nbProperty == 19){
					// TODO: Ici faire autant de t-uple qu'on a de moyen de paiement
					donnees[i] = val.substring(1, val.length()-1);		
					String payment[] = donnees[i].replace(" ","").split("[.,\\-]");
				}
				else {
					// TODO: ici ajouter les autres propriétés mais faire attention pour l'address
					donnees[i] = val.substring(1, val.length()-1);
					//m.add(resource, propertyParking.get(i), donnees[j]);
				}
				i++;
			}
			
			// Ajouter dans le RDF-Graph
			for (int j=0; j<donnees.length; j++){
				System.out.println(donnees[j]);
			}
			nbRow++;
		}
		br.close();
		m.write(System.out,"TURTLE");
	}

	public static void main(String[] args) {
		CsvToRdf p = new CsvToRdf();
		try {
			p.parsingFile();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}