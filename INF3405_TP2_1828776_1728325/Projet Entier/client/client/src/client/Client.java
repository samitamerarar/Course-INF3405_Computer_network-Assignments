package client;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class Client {
	private static final int MIN_PORT = 5000;
	private static final int MAX_PORT = 5050;
	private static final int IP_NUMBER_BYTES = 4;

	private static Socket clientSocket;
	
	private static boolean adresseIPEntreParUtilisateurEstValide(String ipAddress) {
		int tokensRequis = IP_NUMBER_BYTES;
		// on separe l'adresse en tokens separes par des '.'
		String[] tokens = ipAddress.split("\\.");
		// s'il y a plus que 4 tokens, ce n'est pas une adresse ip
		if (tokens.length != tokensRequis) {
			return false;
		}
		// il faut que chaque token soit un nombre entre 0 et 255
		int nombreDeTokens = 0;
		for (String token : tokens) {
			nombreDeTokens++;
			if (!unNombreEstSurUnByte(token)) {
				return false;
			}
			int tokenEnInteger = Integer.parseInt(token);
			if (tokenEnInteger >= 0 && tokenEnInteger <= 255 && nombreDeTokens == tokensRequis) {
				return true;
			}
		}
		return false;
	}
	
	// verifier si le port est un nombre delimite par minRange et maxRange
	private static boolean portEntreParUtilisateurEstValide(String port, int minRange, int maxRange) {
		if(estUnNombre(port)) {
			long portEnInteger;
			try {
				portEnInteger = Long.parseLong(port);
			} catch (NumberFormatException e){
				return false;
			}
			if (portEnInteger >= minRange && portEnInteger <= maxRange) {
					return true;
				}
			
		}
		return false;
	}
	
	private static boolean unNombreEstSurUnByte(String string) {
		// nombre est entre 0 - 255
		return string.matches("\\b(1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\b");
	}
	
	private static boolean estUnNombre(String string) {
		// un nombre positif
		return string.matches("\\d+");
	}
	
	private static boolean imageTrouve(String cheminImage) {
		// regarde si l'image source existe sur le disque dur
		try {
			BufferedImage image = ImageIO.read(new File(cheminImage));
			if (image != null) {
				return true;
			}
		} catch (IOException e) {
			return false;
		}
		return false;
	}
	
	private static void afficherCheminVersImageGenereAvecFiltre(String nomImageRecu) {
		Path cheminDuRepertoireEnCours = Paths.get("");
		String path = cheminDuRepertoireEnCours.toAbsolutePath().toString();
		System.out.println("Image recue avec le filtre! Sa destination: " 
				+ path + "\\" + nomImageRecu + ".jpg");
	}
	
	private static boolean connexionAuServeur() throws NumberFormatException, IOException {
		String status = ""; // pour utilite d'erreur de connexion
		String adresseServeur = "";
		try {
			while (!adresseIPEntreParUtilisateurEstValide(adresseServeur)) {
				adresseServeur = JOptionPane.showInputDialog(status + "Entrer Adresse IP:");
			}
		} catch (Exception e) {
			System.exit(0);
		}

		String port = "";
		try {
			while (!portEntreParUtilisateurEstValide(port, MIN_PORT, MAX_PORT)) {
				port = JOptionPane.showInputDialog("Entrer Port (5000 - 5050):");
			}
		} catch (Exception e) {
			System.exit(0);
		}
		
		try {
			InetAddress ip = InetAddress.getByName(adresseServeur);
			clientSocket = new Socket(ip, Integer.parseInt(port));
			return true;
		} catch (ConnectException e) {
			status = "(Erreur Connexion) ";
			return false;
		}
	}
	
	public static void main(String[] args) {
		try {
			while(!connexionAuServeur());
			String identifiant = JOptionPane.showInputDialog(null, "Entrer l'identifiant (nouvel ou existant)");
			String motDePasse = JOptionPane.showInputDialog(null, "Entrer le mot de passe");
			String nomImageSource = "";
			try {
				while (!imageTrouve(nomImageSource)) {
					nomImageSource = JOptionPane.showInputDialog("Nom de l'image source (avec extension e.g., .jpg)");
				}
			} catch (Exception e) {
				System.exit(0);
			}
			
			String nomImageRecu = "";
			try {
				// lettres et chiffres seulement
				while (!nomImageRecu.matches("^[a-zA-Z0-9]+$")) {
					nomImageRecu = JOptionPane.showInputDialog("Nom de l'image de destination souhaité");
				}
			} catch (Exception e) {
				System.exit(0);
			}

			// preparation envoi au serveur
			OutputStream outputStream = clientSocket.getOutputStream();
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			// lire le fichier jpeg
			BufferedImage image = ImageIO.read(new File(nomImageSource));
			ImageIO.write(image, "jpg", byteArrayOutputStream);
			// ajouter l'image
			byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
			outputStream.write(size);
			outputStream.write(byteArrayOutputStream.toByteArray());
			// ajouter le nom de l'image
			String nomImageSourceAEnvoyer = nomImageSource + ",";
			outputStream.write(nomImageSourceAEnvoyer.getBytes(Charset.forName("UTF-8")));
			// ajouter l'identifiant et mot de passe
			String identifiantAEnvoyer = identifiant + ",";
			outputStream.write(identifiantAEnvoyer.getBytes(Charset.forName("UTF-8")));
			String motDePasseAEnvoyer = motDePasse + ",";
			outputStream.write(motDePasseAEnvoyer.getBytes(Charset.forName("UTF-8")));
			// envoyer l'image + info de login
			outputStream.flush();
			System.out.println("Image envoyee au serveur!");
	
			// recevoir l'image du serveur et la lire
			
			DataInputStream inputStream =  new DataInputStream(clientSocket.getInputStream());
			byte[] sizeArray = new byte[4];
			inputStream.readFully(sizeArray);
			//byte[] sizeArray = new byte[4];
			//inputStream.read(sizeArray);
			byte[] imageArray = new byte[ByteBuffer.wrap(sizeArray).asIntBuffer().get()];
			inputStream.readFully(imageArray);
			ByteArrayInputStream imageByteArray = new ByteArrayInputStream(imageArray);
			BufferedImage imageRecu = ImageIO.read(imageByteArray);
			// si ce n'est pas une image
			if (imageRecu == null) {
				System.out.println("Erreur dans la saisie du mot de passe");
				System.exit(0);
			} // si c'est une image
			else {
				// enregistrer sur le disque dur l'image recu du serveur
				ImageIO.write(imageRecu, "jpg", new File(nomImageRecu + ".jpg"));
				afficherCheminVersImageGenereAvecFiltre(nomImageRecu);
			}

			clientSocket.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}