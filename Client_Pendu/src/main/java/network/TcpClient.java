package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TcpClient {

    private final String host;
    private final int port;
    //pour prévenir l'objet que ligne arrive o que la connexion s'arrete
    private final LineListener listener;

    private Socket socket;
    //pour écrire du texte vers le serveur
    private PrintWriter out;

    public TcpClient(String host, int port, LineListener listener) {
        this.host = host;
        this.port = port;
        this.listener = listener;
    }

    //pour se connecter au serveur
    public void connect() throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

        Thread reader = new Thread(this::readLoop);
        reader.setDaemon(true); //pour marquer reader comlme thread d'arrière plan
        reader.start();
    }

//pour envoyer du texte au serveur
    public synchronized void sendLine(String line) {
        out.println(line);
    }

    //pour savoir si on est connecté
    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }

    //pour fermer la connexion
    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    //pour lire les lignes du serveur
    private void readLoop() {
        Throwable cause = null;
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                listener.onLine(line);
            }
        } catch (IOException e) {
            cause = e;
        }
        listener.onDisconnected(cause);
    }
}