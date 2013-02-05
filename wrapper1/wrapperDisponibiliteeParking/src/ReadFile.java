
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Class qui permet d'ajouter un attribut geo:geometry Point(long lat)
 * si attribut geo:long et geo:lat rencontré
 * @author guillaume
 *
 */
public class ReadFile {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String chaine="";
		String fichier ="../../DataSets/LISTE_SERVICES_PKGS_PUB_NANTES_LISTE_SERVICES_PKGS_PUB_NM_STBL.csv";
		
		//lecture du fichier texte	
		try{
			InputStream ips=new FileInputStream(fichier); 
			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br=new BufferedReader(ipsr);
			String ligne;
			while ((ligne=br.readLine())!=null){
				System.out.println(ligne);
				chaine+=ligne+"\n";
			}
			br.close(); 
		}		
		catch (Exception e){
			System.out.println(e.toString());
		}
	
		//création ou ajout dans le fichier texte
//		try {
//			FileWriter fw = new FileWriter (fichier);
//			BufferedWriter bw = new BufferedWriter (fw);
//			PrintWriter fichierSortie = new PrintWriter (bw); 
//				fichierSortie.println (chaine+"\n test de lecture et écriture !!"); 
//			fichierSortie.close();
//			System.out.println("Le fichier " + fichier + " a été créé!"); 
//		}
//		catch (Exception e){
//			System.out.println(e.toString());
//		}		
	}
}
