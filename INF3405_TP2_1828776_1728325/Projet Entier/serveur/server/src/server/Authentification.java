package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Authentification {
	private static ArrayList<Utilisateur> utilisateurs;

	public static String rechercheUtilisateur(String identifiant, String motDePasse) throws FileNotFoundException {
		createUsersList();
		boolean stream = utilisateurs.stream().filter(user -> user.getUsername().equals(identifiant)).filter(user -> user.getPassword().equals(motDePasse)).findFirst().isPresent();
		if (stream) {
			// utilisateur trouve
			return identifiant;
		}
			
		else if	(utilisateurs.stream().filter(user -> user.getUsername().equals(identifiant)).findFirst().isPresent()) {
			// mot de passe incorrect
			return null;
		}
		
		else {
			creerUnNouvelIdentifiantEtMotDePasseDansBaseDeDonnee(identifiant, motDePasse);
			return identifiant + " (nouveau utilisateur)";
		}
	}
	
	public static void creerUnNouvelIdentifiantEtMotDePasseDansBaseDeDonnee(String identifiant, String motDePasse) {
		Utilisateur utilisateur = new Utilisateur(identifiant, motDePasse);
		
		FileWriter fichier;
		try {
			fichier = new FileWriter("data.csv", true);
			BufferedWriter bufferedWriter = new BufferedWriter(fichier);
			bufferedWriter.write(utilisateur.getUsername() + "," + utilisateur.getPassword());
			bufferedWriter.newLine();
			bufferedWriter.close();
		} catch (IOException e) {
			System.out.println("Erreur: " + e.getMessage());
		}
	}
	
	public static synchronized void createUsersList() throws FileNotFoundException {
		utilisateurs = new ArrayList<Utilisateur>();
		Scanner scan = new Scanner(new File("data.csv"));
		String ligne;
		while (scan.hasNextLine()) {
			ligne = scan.nextLine();
			String[] tokens = ligne.split(",");
			utilisateurs.add(new Utilisateur(tokens[0], tokens[1]));
		}
		scan.close();
	}
}
