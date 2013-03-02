package principal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class Principal {

	/**
	 * @param args
	 */
	public static final String NOM_FICHIER_IN = "hotel.csv";
	public static final String NOM_FICHIER_OUT = "sortie.ttl";
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String ligne;
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
		
	}
	
	public static String lirefichier(String chemin){
		String retour="";
		Reader r;
		String ligne;
		try {
			r = new FileReader(chemin);
	       BufferedReader br =   new BufferedReader(r);
	       while ( (ligne = br.readLine()) != null)
	       {
	          //traitement de la ligne
	    	  retour += ligne;	   
	       }
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
		return retour;
	}
	
	public static String ecrireLigne(String ligne){
		//fonction qui crée les triples qui vont bien
		String[] liste = ligne.split(";");
		String s = "";
		s += "ex:"+liste[0]+" sc:name \""+liste[1]+"\".\n";
		s += "ex:"+liste[0]+" sc:postalCode \""+liste[4]+"\".\n";
		s += "ex:"+liste[0]+" sc:address \""+liste[5]+"\".\n";
		s += "ex:"+liste[0]+" sc:telephone \""+liste[7]+"\".\n";
		if(liste[38].length() != 0) s += "ex:"+liste[0]+" sc:isAnimalAccepted \""+liste[38]+"\".\n";
		System.out.println(liste[38].length());
		return s;
	}

	public String sourceVersOntologie(int i){
		List<String> ontologie = new ArrayList<String>();
			ontologie.add("sc:Name");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("sc:postalCode");
			ontologie.add("sc:address");
			ontologie.add("");
			ontologie.add("sc:telephone");
			ontologie.add("sc:telephone");
			ontologie.add("sc:faxNumber");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("priceRange");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("sc:isAnimalAccepted");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("");
			ontologie.add("sc:geo");
		return ontologie.get(i);
	}
}
