package view;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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

    private static final String CARD_CONNECT = "connect";
    private static final String CARD_GAME = "game";
    private static final int MAX_ATTEMPTS = 6;

    private final JFrame frame = new JFrame("Jeu du pendu");
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private final JTextField hostField = new JTextField("localhost", 12);
    private final JTextField portField = new JTextField("5000", 12);
    private final JTextField nameField = new JTextField(12);
    private final JButton connectButton = new JButton("Se connecter");
    private final JLabel connectStatus = new JLabel(" ");

    private final HangmanPanel hangmanPanel = new HangmanPanel(MAX_ATTEMPTS);
    private final JLabel wordLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel attemptsLabel = new JLabel("Essais restants : -");
    private final JLabel triedLabel = new JLabel("Lettres jouées : -");
    private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JButton newGameButton = new JButton("Nouvelle partie");
    private final JButton resumeButton = new JButton("Reprendre");
    private final JButton rawButton = new JButton("Message brut");
    private final JButton quitButton = new JButton("Quitter");
    private final Map<Character, JButton> keys = new HashMap<>();

    private ViewListener listener;
    private boolean playing;
    private List<Character> lastTried = List.of();

    public SwingView() {
        root.add(buildConnectPanel(), CARD_CONNECT);
        root.add(buildGamePanel(), CARD_GAME);
        frame.setContentPane(root);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(760, 640);
        frame.setLocationRelativeTo(null);
        resumeButton.setEnabled(false);
        newGameButton.setEnabled(false);
    }

    public void show() {
        frame.setVisible(true);
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
            attemptsLabel.setText("Essais restants : -");
            triedLabel.setText("Lettres jouées : -");
            hangmanPanel.setErrors(0);
            newGameButton.setEnabled(true);
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
            newGameButton.setEnabled(true);
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
            newGameButton.setEnabled(false);
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
        form.add(new JLabel("Serveur :"));
        form.add(hostField);
        form.add(new JLabel("Port :"));
        form.add(portField);
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
        return panel;
    }

    private JPanel buildGamePanel() {
        wordLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 40));
        wordLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        JPanel side = new JPanel(new GridLayout(0, 1, 5, 5));
        side.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        side.add(attemptsLabel);
        side.add(triedLabel);

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
        buttons.add(resumeButton);
        buttons.add(rawButton);
        buttons.add(quitButton);

        JPanel bottom = new JPanel(new GridLayout(2, 1));
        bottom.add(buttons);
        bottom.add(statusLabel);

        JPanel south = new JPanel(new BorderLayout());
        south.add(keyboard, BorderLayout.CENTER);
        south.add(bottom, BorderLayout.SOUTH);

        newGameButton.addActionListener(e -> {
            if (listener != null) {
                listener.onNewGame();
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
    }

    private void connectClicked() {
        String host = hostField.getText().trim();
        String name = nameField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            connectStatus.setForeground(Color.RED);
            connectStatus.setText("Port invalide");
            return;
        }
        if (host.isEmpty() || name.isEmpty() || port < 1 || port > 65535) {
            connectStatus.setForeground(Color.RED);
            connectStatus.setText("Hôte, port et pseudo sont obligatoires");
            return;
        }
        if (listener != null) {
            listener.onConnect(host, port, name);
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
