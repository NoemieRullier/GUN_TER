import java.io.IOException;
import java.util.List;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;


public class DisponibiliteeParking {

	private static final String file = "../../DataSets/disponibilite-dans-les-parkings-publics-de-nantes-metropole.xml";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		SAXBuilder builder = new SAXBuilder();
		
		try {
			Document document = builder.build(file);
			Element rootNode = document.getRootElement();
			for(Element e : rootNode.getChildren("Groupe_Parking")){
				System.out.println(e.getName());
				for(Element f : e.getChildren()){
					System.out.println(f.getName() + " : " + f.getText());
				}
			}
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}