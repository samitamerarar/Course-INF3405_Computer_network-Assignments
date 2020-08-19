package server;

public class Utilisateur {
	private String identifiant;
	private String motDePasse;
	
	public Utilisateur(String identifiant, String motDePasse) {
		this.identifiant = identifiant;
		this.motDePasse = motDePasse;
	}
	
	public String getUsername() {
		return this.identifiant;
	}
	
	public String getPassword() {
		return this.motDePasse;
	}
}
