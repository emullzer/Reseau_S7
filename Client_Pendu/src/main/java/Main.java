import network.LineListener;
import network.TcpClient;
import protocol.MessageCodec;
import protocol.message.ChatMessage;
import protocol.message.CreateRoomMessage;
import protocol.message.ErrorMessage;
import protocol.message.HelloMessage;
import protocol.message.JoinRoomMessage;
import protocol.message.LetterMessage;
import protocol.message.Message;
import protocol.message.NewGameMessage;
import protocol.message.QuitMessage;
import protocol.message.ResumeMessage;
import protocol.message.StateMessage;
import view.SwingView;
import view.ViewListener;

import javax.swing.SwingUtilities;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SwingView view = new SwingView();
            MessageCodec codec = new MessageCodec();
            AtomicInteger requestCounter = new AtomicInteger(1);
            AtomicReference<TcpClient> clientRef = new AtomicReference<>();
            AtomicReference<String> playerNameRef = new AtomicReference<>("");
            AtomicReference<String> sessionIdRef = new AtomicReference<>();

            LineListener listener = new LineListener() {
                @Override
                public void onLine(String line) {
                    try {
                        Message msg = codec.decode(line);
                        if (msg instanceof StateMessage state) {
                            String sid = state.getSessionId();
                            sessionIdRef.set(sid);
                            if (sid.startsWith("S")) {
                                view.setRoomInfo("Mode : Solo (" + sid + ")");
                            } else {
                                view.setRoomInfo("Code Room : " + sid);
                            }
                            switch (state.getState()) {
                                case IN_PROGRESS -> view.showState(
                                        state.getWord(),
                                        state.getTriedLetters(),
                                        state.getRemainingAttempts()
                                );
                                case WIN -> view.showGameOver(true, state.getWord());
                                case LOSE -> view.showGameOver(
                                        false,
                                        state.getRevealedWord().orElse(state.getWord())
                                );
                                case ABORTED -> view.showDisconnected("Partie interrompue");
                            }
                        } else if (msg instanceof ChatMessage chat) {
                            view.appendChat(chat.getPlayerName(), chat.getText());
                        } else if (msg instanceof ErrorMessage error) {
                            view.showError(error.getCode() + " : " + error.getMessage());
                        }
                    } catch (Exception e) {
                        view.showError("Erreur protocole : " + e.getMessage());
                    }
                }

                @Override
                public void onDisconnected(Throwable cause) {
                    String reason = (cause != null && cause.getMessage() != null)
                            ? cause.getMessage()
                            : "Serveur déconnecté";
                    view.showDisconnected(reason);
                }
            };

            view.setListener(new ViewListener() {
                @Override
                public void onConnect(String host, int port, String playerName) {
                    playerNameRef.set(playerName);
                    view.showConnecting();

                    TcpClient previous = clientRef.getAndSet(null);
                    if (previous != null) {
                        previous.close();
                    }

                    TcpClient client = new TcpClient(host, port, listener);
                    new Thread(() -> {
                        try {
                            client.connect();
                            clientRef.set(client);
                            view.showConnected();
                            int reqId = requestCounter.getAndIncrement();
                            client.sendLine(codec.encode(new HelloMessage(reqId, playerName)));
                        } catch (Exception e) {
                            view.showError("Connexion impossible : " + e.getMessage());
                        }
                    }).start();
                }

                @Override
                public void onNewGame() {
                    TcpClient client = clientRef.get();
                    String playerName = playerNameRef.get();
                    if (client != null && client.isConnected() && !playerName.isEmpty()) {
                        int reqId = requestCounter.getAndIncrement();
                        client.sendLine(codec.encode(new NewGameMessage(reqId, playerName)));
                    }
                }

                @Override
                public void onCreateRoom() {
                    TcpClient client = clientRef.get();
                    String playerName = playerNameRef.get();
                    if (client != null && client.isConnected() && !playerName.isEmpty()) {
                        int reqId = requestCounter.getAndIncrement();
                        client.sendLine(codec.encode(new CreateRoomMessage(reqId, playerName)));
                    }
                }

                @Override
                public void onJoinRoom(String roomCode) {
                    TcpClient client = clientRef.get();
                    String playerName = playerNameRef.get();
                    if (client != null && client.isConnected() && !playerName.isEmpty()) {
                        int reqId = requestCounter.getAndIncrement();
                        client.sendLine(codec.encode(new JoinRoomMessage(reqId, roomCode, playerName)));
                    }
                }

                @Override
                public void onChatMessage(String text) {
                    TcpClient client = clientRef.get();
                    String sessionId = sessionIdRef.get();
                    String playerName = playerNameRef.get();
                    if (client != null && client.isConnected() && sessionId != null && !playerName.isEmpty()) {
                        int reqId = requestCounter.getAndIncrement();
                        client.sendLine(codec.encode(new ChatMessage(reqId, sessionId, playerName, text)));
                    }
                }

                @Override
                public void onLetter(char letter) {
                    TcpClient client = clientRef.get();
                    String sessionId = sessionIdRef.get();
                    if (client != null && client.isConnected() && sessionId != null) {
                        int reqId = requestCounter.getAndIncrement();
                        client.sendLine(codec.encode(new LetterMessage(reqId, sessionId, letter)));
                    }
                }

                @Override
                public void onResume() {
                    String playerName = playerNameRef.get();
                    String sessionId = sessionIdRef.get();
                    if (playerName.isEmpty() || sessionId == null) {
                        view.showError("Aucune session à reprendre");
                        return;
                    }

                    TcpClient client = clientRef.get();
                    if (client != null && client.isConnected()) {
                        int reqId = requestCounter.getAndIncrement();
                        client.sendLine(codec.encode(new ResumeMessage(reqId, sessionId, playerName)));
                    } else {
                        view.showConnecting();
                        TcpClient newClient = new TcpClient("127.0.0.1", 5001, listener);
                        new Thread(() -> {
                            try {
                                newClient.connect();
                                clientRef.set(newClient);
                                int reqId = requestCounter.getAndIncrement();
                                newClient.sendLine(codec.encode(new ResumeMessage(reqId, sessionId, playerName)));
                            } catch (Exception e) {
                                view.showError("Reprise impossible : " + e.getMessage());
                            }
                        }).start();
                    }
                }

                @Override
                public void onQuit() {
                    TcpClient client = clientRef.getAndSet(null);
                    String sessionId = sessionIdRef.get();
                    if (client != null) {
                        if (client.isConnected() && sessionId != null) {
                            int reqId = requestCounter.getAndIncrement();
                            client.sendLine(codec.encode(new QuitMessage(reqId, sessionId)));
                        }
                        client.close();
                    }
                    System.exit(0);
                }

                @Override
                public void onRawMessage(String rawLine) {
                    TcpClient client = clientRef.get();
                    if (client != null && client.isConnected()) {
                        client.sendLine(rawLine);
                    }
                }
            });

            view.show();
        });
    }
}