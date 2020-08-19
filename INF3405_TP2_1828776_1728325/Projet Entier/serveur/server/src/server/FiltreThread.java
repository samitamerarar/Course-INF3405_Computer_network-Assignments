package server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

public class FiltreThread extends Thread{
	protected Socket socket;
	
	public FiltreThread(Socket clientSocket) {
		this.socket = clientSocket;
	}
	
	public void run() {
		try {
			InputStream inputStream = socket.getInputStream();
			
			// lecture de l'image
			byte[] sizeArray = new byte[4];
			inputStream.read(sizeArray);
			byte[] imageArray = new byte[ByteBuffer.wrap(sizeArray).asIntBuffer().get()];
			inputStream.read(imageArray);
			ByteArrayInputStream imageByteArray = new ByteArrayInputStream(imageArray);
			BufferedImage image = ImageIO.read(imageByteArray);
			
			int charALire;
			// lecture nom de l'image
			StringBuilder stringBuilder = new StringBuilder();
			while ( !Character.toString((char)(charALire = inputStream.read())).equals(",") ) {
			    stringBuilder.append((char)charALire);
			}
			String nomImageSource = stringBuilder.toString();
			// lire identifiant du client
			stringBuilder = new StringBuilder();
			while ( !Character.toString((char)(charALire = inputStream.read())).equals(",") ) {
			    stringBuilder.append((char)charALire);
			}
			String identifiant = stringBuilder.toString();
			// lire mot de passe du client
			stringBuilder = new StringBuilder();
			while ( !Character.toString((char)(charALire = inputStream.read())).equals(",") ) {
				stringBuilder.append((char)charALire);
			}
			String motDePasse = stringBuilder.toString();
			
			// faire une recherche dans la base de donnee
			// si l'identifiant et mot de passe sont present,
			// envoyer une image au client
			// si l'identifiant et le mot de passe sont pas present,
			// les ajouter a la base de donne et envoyer l'image au client
			// si l'identifiant est present, mais le mot de passe incorrect
			// ne pas envoyer l'image au serveur
			OutputStream outputStream = socket.getOutputStream();
			if ((Authentification.rechercheUtilisateur(identifiant, motDePasse)) != null) {
				afficherInfoLogLorsTraitementDeImage(identifiant, socket.getRemoteSocketAddress().toString(), nomImageSource);
				// appliquer le filtre a l'image et la renvoyer
				BufferedImage imageAvecFiltreApplique = Sobel.process(image);
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				ImageIO.write(imageAvecFiltreApplique, "jpg", byteArrayOutputStream);
				byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
				outputStream.write(size);
				outputStream.write(byteArrayOutputStream.toByteArray());
				outputStream.flush();
				System.out.println("Image renvoyee au client");
			}
			else {
				// ne pas renvoyer l'image, mais un byte quelconque
				outputStream.write(1);
				outputStream.flush();
				System.out.println("Image pas renvoyee au client, mot de passe incorrect");
			}
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();			
		}
	}
	
	private static void afficherInfoLogLorsTraitementDeImage(String user, String adresseIPPortClient, String nomImage) {
		System.out.println("[" + user + " - " + adresseIPPortClient.substring(1) + " - " + obtenirDateEtTemps() + "] : Image " + nomImage + " recue pour traitement.");
	}
	
	private static String obtenirDateEtTemps() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd@HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}
}
