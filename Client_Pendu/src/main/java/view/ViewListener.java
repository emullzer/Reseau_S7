package view;

public interface ViewListener {

    void onConnect(String host, int port, String playerName);

    void onLetter(char letter);

    void onNewGame();

    void onCreateRoom();

    void onJoinRoom(String roomCode);

    void onChatMessage(String text);

    void onResume();

    void onQuit();

    void onRawMessage(String rawLine);
}