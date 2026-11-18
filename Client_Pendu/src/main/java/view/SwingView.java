package view;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SwingView implements GameView {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 5001;

    private static final String CARD_CONNECT = "connect";
    private static final String CARD_GAME = "game";
    private static final int MAX_ATTEMPTS = 6;

    private final JFrame frame = new JFrame("Jeu du pendu");
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);


    private final JTextField nameField = new JTextField(15);
    private final JButton connectButton = new JButton("Se connecter");
    private final JLabel connectStatus = new JLabel(" ");

    private final HangmanPanel hangmanPanel = new HangmanPanel(MAX_ATTEMPTS);
    private final JLabel wordLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel roomLabel = new JLabel("Mode : -");
    private final JLabel attemptsLabel = new JLabel("Essais restants : -");
    private final JLabel triedLabel = new JLabel("Lettres jouées : -");
    private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JButton newGameButton = new JButton("Nouvelle partie (Solo)");
    private final JButton createRoomButton = new JButton("Créer une room");
    private final JButton joinRoomButton = new JButton("Rejoindre une room");
    private final JButton resumeButton = new JButton("Reprendre");
    private final JButton rawButton = new JButton("Message brut");
    private final JButton quitButton = new JButton("Quitter");
    private final Map<Character, JButton> keys = new HashMap<>();

    private final JTextArea chatArea = new JTextArea(12, 22);
    private final JTextField chatInput = new JTextField();
    private final JButton chatSendButton = new JButton("Envoyer");

    private ViewListener listener;
    private boolean playing;
    private List<Character> lastTried = List.of();

    public SwingView() {
        root.add(buildConnectPanel(), CARD_CONNECT);
        root.add(buildGamePanel(), CARD_GAME);
        frame.setContentPane(root);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(860, 640);
        frame.setLocationRelativeTo(null);
        resumeButton.setEnabled(false);
        newGameButton.setEnabled(false);
        createRoomButton.setEnabled(false);
        joinRoomButton.setEnabled(false);
        chatInput.setEnabled(false);
        chatSendButton.setEnabled(false);
    }

    public void show() {
        frame.setVisible(true);
    }

    public void setRoomInfo(String info) {
        SwingUtilities.invokeLater(() -> roomLabel.setText(info));
    }

    public void appendChat(String sender, String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(sender + " : " + message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    public void clearChat() {
        SwingUtilities.invokeLater(() -> chatArea.setText(""));
    }

    @Override
    public void setListener(ViewListener listener) {
        this.listener = listener;
    }

    @Override
    public void showConnecting() {
        SwingUtilities.invokeLater(() -> {
            connectButton.setEnabled(false);
            connectStatus.setForeground(Color.DARK_GRAY);
            connectStatus.setText("Connexion en cours...");
        });
    }

    @Override
    public void showConnected() {
        SwingUtilities.invokeLater(() -> {
            connectButton.setEnabled(true);
            connectStatus.setText(" ");
            playing = false;
            lastTried = List.of();
            refreshKeys();
            wordLabel.setText(" ");
            roomLabel.setText("Mode : -");
            attemptsLabel.setText("Essais restants : -");
            triedLabel.setText("Lettres jouées : -");
            hangmanPanel.setErrors(0);
            chatArea.setText("");
            chatInput.setEnabled(false);
            chatSendButton.setEnabled(false);
            newGameButton.setEnabled(true);
            createRoomButton.setEnabled(true);
            joinRoomButton.setEnabled(true);
            resumeButton.setEnabled(false);
            setStatus("Connecté", Color.DARK_GRAY);
            cards.show(root, CARD_GAME);
        });
    }

    @Override
    public void showState(String word, List<Character> triedLetters, int remainingAttempts) {
        List<Character> tried = List.copyOf(triedLetters);
        SwingUtilities.invokeLater(() -> {
            playing = true;
            lastTried = tried;
            wordLabel.setText(spaced(word));
            attemptsLabel.setText("Essais restants : " + remainingAttempts);
            triedLabel.setText("Lettres jouées : " + (tried.isEmpty() ? "-" : joined(tried)));
            hangmanPanel.setErrors(MAX_ATTEMPTS - remainingAttempts);
            chatInput.setEnabled(true);
            chatSendButton.setEnabled(true);
            newGameButton.setEnabled(true);
            createRoomButton.setEnabled(true);
            joinRoomButton.setEnabled(true);
            resumeButton.setEnabled(false);
            refreshKeys();
            setStatus(" ", Color.DARK_GRAY);
            cards.show(root, CARD_GAME);
        });
    }

    @Override
    public void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            connectButton.setEnabled(true);
            connectStatus.setForeground(Color.RED);
            connectStatus.setText(message);
            setStatus(message, Color.RED);
            refreshKeys();
        });
    }

    @Override
    public void showDisconnected(String reason) {
        SwingUtilities.invokeLater(() -> {
            playing = false;
            refreshKeys();
            connectButton.setEnabled(true);
            chatInput.setEnabled(false);
            chatSendButton.setEnabled(false);
            newGameButton.setEnabled(false);
            createRoomButton.setEnabled(false);
            joinRoomButton.setEnabled(false);
            resumeButton.setEnabled(true);
            setStatus("Connexion perdue : " + reason, Color.RED);
        });
    }

    @Override
    public void showGameOver(boolean won, String word) {
        SwingUtilities.invokeLater(() -> {
            playing = false;
            refreshKeys();
            if (word != null) {
                wordLabel.setText(spaced(word));
            }
            if (won) {
                setStatus("Gagné !", new Color(0, 130, 0));
            } else {
                setStatus("Perdu ! Le mot était : " + (word == null ? "?" : word), Color.RED);
            }
        });
    }

    private JPanel buildConnectPanel() {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Pseudo :"));
        form.add(nameField);
        form.add(new JLabel());
        form.add(connectButton);

        JPanel box = new JPanel(new BorderLayout(0, 10));
        JLabel title = new JLabel("Jeu du pendu", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        connectStatus.setHorizontalAlignment(SwingConstants.CENTER);
        box.add(title, BorderLayout.NORTH);
        box.add(form, BorderLayout.CENTER);
        box.add(connectStatus, BorderLayout.SOUTH);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(box);

        connectButton.addActionListener(e -> connectClicked());
        nameField.addActionListener(e -> connectClicked()); // Entrée valide aussi
        return panel;
    }

    private JPanel buildGamePanel() {
        wordLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 40));
        wordLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        roomLabel.setFont(roomLabel.getFont().deriveFont(Font.BOLD, 14f));
        infoPanel.add(roomLabel);
        infoPanel.add(attemptsLabel);
        infoPanel.add(triedLabel);

        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane chatScroll = new JScrollPane(chatArea);

        JPanel chatInputPanel = new JPanel(new BorderLayout(4, 0));
        chatInputPanel.add(chatInput, BorderLayout.CENTER);
        chatInputPanel.add(chatSendButton, BorderLayout.EAST);

        Runnable sendChatAction = () -> {
            String text = chatInput.getText().trim();
            if (!text.isEmpty() && listener != null) {
                listener.onChatMessage(text);
                chatInput.setText("");
            }
        };
        chatSendButton.addActionListener(e -> sendChatAction.run());
        chatInput.addActionListener(e -> sendChatAction.run());

        JPanel chatPanel = new JPanel(new BorderLayout(0, 5));
        chatPanel.setBorder(BorderFactory.createTitledBorder("Chat"));
        chatPanel.add(chatScroll, BorderLayout.CENTER);
        chatPanel.add(chatInputPanel, BorderLayout.SOUTH);

        JPanel side = new JPanel(new BorderLayout(0, 10));
        side.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        side.add(infoPanel, BorderLayout.NORTH);
        side.add(chatPanel, BorderLayout.CENTER);

        JPanel keyboard = new JPanel(new GridLayout(2, 13, 3, 3));
        keyboard.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        for (char c = 'A'; c <= 'Z'; c++) {
            final char letter = c;
            JButton button = new JButton(String.valueOf(c));
            button.setMargin(new Insets(4, 2, 4, 2));
            button.setEnabled(false);
            button.addActionListener(e -> letterClicked(letter));
            keys.put(c, button);
            keyboard.add(button);
        }

        JPanel buttons = new JPanel();
        buttons.add(newGameButton);
        buttons.add(createRoomButton);
        buttons.add(joinRoomButton);
        buttons.add(resumeButton);
        buttons.add(quitButton);

        JPanel bottom = new JPanel(new GridLayout(2, 1));
        bottom.add(buttons);
        bottom.add(statusLabel);

        JPanel south = new JPanel(new BorderLayout());
        south.add(keyboard, BorderLayout.CENTER);
        south.add(bottom, BorderLayout.SOUTH);

        newGameButton.addActionListener(e -> {
            if (listener != null) {
                clearChat();
                listener.onNewGame();
            }
        });
        createRoomButton.addActionListener(e -> {
            if (listener != null) {
                clearChat();
                listener.onCreateRoom();
            }
        });
        joinRoomButton.addActionListener(e -> {
            String code = JOptionPane.showInputDialog(frame, "Code de la room à rejoindre :");
            if (code != null && !code.trim().isEmpty() && listener != null) {
                clearChat();
                listener.onJoinRoom(code.trim().toUpperCase());
            }
        });
        resumeButton.addActionListener(e -> {
            if (listener != null) {
                listener.onResume();
            }
        });
        rawButton.addActionListener(e -> rawClicked());
        quitButton.addActionListener(e -> {
            if (listener != null) {
                listener.onQuit();
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(wordLabel, BorderLayout.NORTH);
        panel.add(hangmanPanel, BorderLayout.CENTER);
        panel.add(side, BorderLayout.EAST);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }//

    private void connectClicked() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            connectStatus.setForeground(Color.RED);
            connectStatus.setText("Le pseudo est obligatoire");
            return;
        }
        if (listener != null) {
            listener.onConnect(HOST, PORT, name);
        }
    }

    private void letterClicked(char letter) {
        for (JButton button : keys.values()) {
            button.setEnabled(false);
        }
        if (listener != null) {
            listener.onLetter(letter);
        }
    }

    private void rawClicked() {
        String line = JOptionPane.showInputDialog(frame, "Ligne à envoyer telle quelle :");
        if (line != null && listener != null) {
            listener.onRawMessage(line);
        }
    }

    private void refreshKeys() {
        for (Map.Entry<Character, JButton> entry : keys.entrySet()) {
            boolean enabled = playing && !lastTried.contains(entry.getKey());
            entry.getValue().setEnabled(enabled);
        }
    }

    private void setStatus(String text, Color color) {
        statusLabel.setForeground(color);
        statusLabel.setText(text);
    }

    private String spaced(String word) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(word.charAt(i));
        }
        return sb.toString();
    }

    private String joined(List<Character> letters) {
        StringBuilder sb = new StringBuilder();
        for (char c : letters) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(c);
        }
        return sb.toString();
    }
}