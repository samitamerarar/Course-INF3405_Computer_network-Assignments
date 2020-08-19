package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Server {
	private static final int IP_NUMBER_BYTES = 4;
	private static final int MIN_PORT = 5000;
	private static final int MAX_PORT = 5050;
	
	private static Socket socket;
	private static ServerSocket serverSocket;
	
	private static String obtenirAdresseIPDuServeurACreer(Scanner scanner) {
		System.out.println("Entrer l'adresse IP du serveur a creer: ");
		String adresseIP;
		do {
			adresseIP = scanner.nextLine();
		} while (!adresseIPEntreParUtilisateurEstValide(adresseIP));
		return adresseIP;
	}
	
	private static int obtenirPortDuServeurACreer(Scanner scanner) {
		int port = 0;
		boolean portEstValide = false;
		
		do {
            System.out.print("Entrer maintenant son port: ");

            try {
                port = scanner.nextInt();
                if (port >= MIN_PORT && port <= MAX_PORT) {
                    portEstValide = true;
                } else {
                    System.out
                            .println("Pas entre l'ecart 5000-5050!");
                    scanner.nextLine();
                }
            } catch (InputMismatchException exception) {
                System.out
                        .println("Pas un nombre!");
                scanner.nextLine();
            }

        } while (!(portEstValide));
		return port;
	}
	
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
	
	private static boolean unNombreEstSurUnByte(String string) {
		// nombre est entre 0 - 255
		return string.matches("\\b(1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\b");
	}
	
	public static void main(String[] args) throws Exception {
		try {
			// preparation serveur
			Scanner scanner = new Scanner(System.in);
			String adresseIP = obtenirAdresseIPDuServeurACreer(scanner);
			InetAddress ip = InetAddress.getByName(adresseIP);
			int port = obtenirPortDuServeurACreer(scanner);

			serverSocket = new ServerSocket();
			serverSocket.setReuseAddress(true);
			serverSocket.bind(new InetSocketAddress(ip, port));
			System.out.println("Serveur en marche...");
		} catch (IOException e) {
			e.printStackTrace();
		}
		// gestion multi-thread
		while(true) {
			try {
				socket = serverSocket.accept();
			} catch (IOException e) {
				System.out.println(e);
	        }
			// execution d'un thread qui applique la requete du client (filtre sur l'image)
			new FiltreThread(socket).start();
		}
	}
	
}