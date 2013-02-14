import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.util.IteratorIterable;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;


public class DisponibiliteeParking {

	private String xmlFile;
	private String onthologyFile;
	
	private Model dispoParkingModel;
	
	private Map<String, Property> properties_available;
	private Map<String, Resource> mapStatus;
	
	
	//lire l'onthologie, en déduire l'entête, les propriétées à utiliser
	public void readOnthology(){
		//entête du fichier RDF Turtle
		String nsA = "http://example.org/";
		String nsB = "http://example.org/Parking_availability/";
		String nsC = "http://example.org/Parking_status/";
		String nsD = "http://example.org/Parking/";
		
		dispoParkingModel.setNsPrefix("schema", nsA);
		dispoParkingModel.setNsPrefix("parking_availability", nsB);
		dispoParkingModel.setNsPrefix("parking_status", nsC);
		dispoParkingModel.setNsPrefix("parking", nsD);
		
		//création des propriété pour les parkings
		properties_available.put("Grp_identifiant", dispoParkingModel.createProperty(nsB + "id"));
		properties_available.put("Grp_nom", dispoParkingModel.createProperty(nsB + "name"));
		properties_available.put("Grp_statut", dispoParkingModel.createProperty(nsB + "statut"));
		properties_available.put("Grp_pri_aut", dispoParkingModel.createProperty(nsB + "priority"));
		properties_available.put("Grp_disponible", dispoParkingModel.createProperty(nsB + "space"));
		properties_available.put("Grp_complet", dispoParkingModel.createProperty(nsB + "complete"));
		properties_available.put("Grp_exploitation", dispoParkingModel.createProperty(nsB + "exploitation"));
		properties_available.put("Grp_horodatage", dispoParkingModel.createProperty(nsB + "timestamp"));
		properties_available.put("IdObj", dispoParkingModel.createProperty(nsB + "IdObj"));
		
		mapStatus.put("0", dispoParkingModel.createResource(nsC + "0"));
		mapStatus.put("1", dispoParkingModel.createResource(nsC + "1"));
		mapStatus.put("2", dispoParkingModel.createResource(nsC + "2"));
		mapStatus.put("5", dispoParkingModel.createResource(nsC + "5"));
		
		dispoParkingModel.add(mapStatus.get("0"), dispoParkingModel.createProperty(nsC + "id"), "0");
		dispoParkingModel.add(mapStatus.get("0"), dispoParkingModel.createProperty(nsC + "description"), "Invalide (comptage hors service)");
		dispoParkingModel.add(mapStatus.get("0"), dispoParkingModel.createProperty(nsC + "pjdPrint"), "Neutre (affichage au noir)");
		
		dispoParkingModel.add(mapStatus.get("1"), dispoParkingModel.createProperty(nsC + "id"), "1");
		dispoParkingModel.add(mapStatus.get("1"), dispoParkingModel.createProperty(nsC + "description"), "Groupe parking fermé pour tous clients ");
		dispoParkingModel.add(mapStatus.get("1"), dispoParkingModel.createProperty(nsC + "pjdPrint"), "FERME");
		
		dispoParkingModel.add(mapStatus.get("2"), dispoParkingModel.createProperty(nsC + "id"), "2");
		dispoParkingModel.add(mapStatus.get("2"), dispoParkingModel.createProperty(nsC + "description"), "Groupe parking fermé au client horaires et ouvert pour les abonnés (exemple : un parking fermé aux clients horaires la nuit ou le dimanche)");
		dispoParkingModel.add(mapStatus.get("2"), dispoParkingModel.createProperty(nsC + "pjdPrint"), "ABONNES");
		
		dispoParkingModel.add(mapStatus.get("5"), dispoParkingModel.createProperty(nsC + "id"), "5");
		dispoParkingModel.add(mapStatus.get("5"), dispoParkingModel.createProperty(nsC + "description"), "Groupe parking ouvert à tous les clients. Le nombre de places correspond au nombre de places destinées aux clients horaires");
		dispoParkingModel.add(mapStatus.get("5"), dispoParkingModel.createProperty(nsC + "pjdPrint"), "#Nombre de places# ou COMPLET");
	}
	
	public void xmlToRDF(){

				//Parsing xml file
				
				SAXBuilder builder = new SAXBuilder();
				
				try {
					Document document = builder.build(xmlFile);
					Element rootNode = document.getRootElement();
					IteratorIterable<Element> grpParkingIterator = rootNode.getDescendants(new ElementFilter("Groupe_Parking"));
					Element encours = grpParkingIterator.next();
					while(grpParkingIterator.hasNext()){
						if(encours.getChild("Groupe_Parking") == null){
							Resource r = dispoParkingModel.createResource(dispoParkingModel.getNsPrefixURI("parking_availability") + encours.getChild("Grp_identifiant").getValue());
							
							dispoParkingModel.add(r, properties_available.get("Grp_identifiant"), encours.getChild("Grp_identifiant").getValue());
							dispoParkingModel.add(r, properties_available.get("Grp_nom"), encours.getChild("Grp_nom").getValue());
							dispoParkingModel.add(r, properties_available.get("Grp_identifiant"), encours.getChild("Grp_identifiant").getValue());
							
							dispoParkingModel.add(r, properties_available.get("Grp_statut"), mapStatus.get(encours.getChild("Grp_statut").getValue()));
							dispoParkingModel.add(r, properties_available.get("Grp_pri_aut"), encours.getChild("Grp_pri_aut").getValue());
							dispoParkingModel.add(r, properties_available.get("Grp_disponible"), encours.getChild("Grp_disponible").getValue());
							dispoParkingModel.add(r, properties_available.get("Grp_complet"), encours.getChild("Grp_complet").getValue());
							dispoParkingModel.add(r, properties_available.get("Grp_exploitation"), encours.getChild("Grp_exploitation").getValue());
							dispoParkingModel.add(r, properties_available.get("Grp_horodatage"), encours.getChild("Grp_horodatage").getValue());
							dispoParkingModel.add(r, properties_available.get("IdObj"), encours.getChild("IdObj").getValue());
						}
						encours = grpParkingIterator.next();
					}
					
				} catch (JDOMException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dispoParkingModel.write(System.out, "TURTLE");
	}
	
	public DisponibiliteeParking(String xmlFile, String onthologyFile){
		this.xmlFile = xmlFile;
		this.onthologyFile = onthologyFile;
		dispoParkingModel = ModelFactory.createDefaultModel();
		properties_available = new HashMap<String, Property>();
		mapStatus = new HashMap<String, Resource>();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//TODO: revoir algorithme pour prendre n balises pour cela lire l'onthologie pour récupérer les propriétées
		
		DisponibiliteeParking d = new DisponibiliteeParking("../../DataSets/disponibilite-dans-les-parkings-publics-de-nantes-metropole.xml", "../../Ontologie/Onthologie_DisponobiliteeParking.ttl");
		d.readOnthology();
		d.xmlToRDF();
	}
}
