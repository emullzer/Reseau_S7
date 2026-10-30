package network;

public interface LineListener {

    void onLine(String line);

    void onDisconnected(Throwable cause);
}
