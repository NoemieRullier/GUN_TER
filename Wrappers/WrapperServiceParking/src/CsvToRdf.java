import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
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
		propertyParking.add(m.createProperty(parkingP + "telephone" ));
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
		
		HashMap<Integer, Property> relation = new HashMap<Integer, Property>();
		relation.put(0, propertyParking.get(0));
		relation.put(1, propertyParking.get(1));
		relation.put(2, propertyParking.get(2));
		relation.put(3, propertyParking.get(3));
		relation.put(4, propertyAddressPostal.get(1));
		relation.put(5, propertyParking.get(5));
		relation.put(6, propertyAddressPostal.get(0));
		relation.put(7, propertyParking.get(6));
		relation.put(8, propertyParking.get(7));
		relation.put(9, propertyAddressPostal.get(2));
		relation.put(10, propertyParking.get(8));
		relation.put(11, propertyParking.get(9));
		relation.put(12, propertyParking.get(10));
		relation.put(13, propertyParking.get(11));
		relation.put(14, propertyParking.get(12));
		relation.put(15, propertyParking.get(13));
		relation.put(16, propertyParking.get(14));
		relation.put(17, propertyParking.get(15));
		relation.put(18, propertyParking.get(16));
		relation.put(19, propertyParking.get(17));
		relation.put(20, propertyParking.get(18));
		relation.put(21, propertyParking.get(19));
		
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

		int nbRow = 1;
		while ((row = br.readLine()) != null)
		{
			// We put the row in the tab
			data = p.split(row);
						
			// Create the resource correspond to the row
			Resource resource = m.createResource(parkingP+data[0].substring(1, data[0].length()-1));
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
							m.add(resource, propertyParking.get(4), address);
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
