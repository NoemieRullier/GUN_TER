package principal;

import java.io.IOException;

import org.jdom2.JDOMException;

public class Principal {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		/*String ligne;
	       Reader r;
		try {
			r = new FileReader(NOM_FICHIER_IN);
	       BufferedReader br =   new BufferedReader(r);
	       //fichier de sortie
	       
	       FileWriter fw = new FileWriter(NOM_FICHIER_OUT, true);
	       BufferedWriter output = new BufferedWriter(fw);
	       ligne = br.readLine(); //on saute la premiere ligne
	       output.write("@prefix ex: <http://lalalalala>.\n");
	       output.write("@prefix sc: <http://lalalalala>.\n");
	       output.write("\n");
	       while ( (ligne = br.readLine()) != null)
	       {
	          //traitement de la ligne
	    	   
	    	   output.write(ecrireLigne(ligne+"\n"));
		       output.flush();
	    	   
	       }
	       output.close();
	       r.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Le fichier n'a pas ete trouve.");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Erreur I/O.");
			e.printStackTrace();
		}
		*/
		
		HoteltoRDF h = new HoteltoRDF();
		try {
			h.loadMapping();
			h.toRdf();
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
