package app;

import view.SwingView;
import view.ViewListener;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

public class GuiTest {

    private static final String SECRET = "JAVA";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SwingView view = new SwingView();
            view.setListener(new FakeListener(view));
            view.show();
        });
    }

    private static class FakeListener implements ViewListener {

        private final SwingView view;
        private final List<Character> tried = new ArrayList<>();
        private int remaining = 6;

        FakeListener(SwingView view) {
            this.view = view;
        }

        @Override
        public void onConnect(String host, int port, String playerName) {
            System.out.println("connect " + host + ":" + port + " as " + playerName);
            view.showConnecting();
            new Thread(() -> {
                try {
                    Thread.sleep(800);
                } catch (InterruptedException ignored) {
                }
                reset();
                view.showConnected();
                view.showState(masked(), tried, remaining);
            }).start();
        }

        @Override
        public void onLetter(char letter) {
            char c = Character.toUpperCase(letter);
            if (tried.contains(c)) {
                view.showError("Lettre déjà jouée");
                return;
            }
            tried.add(c);
            if (SECRET.indexOf(c) < 0) {
                remaining--;
            }
            view.showState(masked(), tried, remaining);
            if (masked().equals(SECRET)) {
                view.showGameOver(true, SECRET);
            } else if (remaining == 0) {
                view.showGameOver(false, SECRET);
            }
        }

        @Override
        public void onNewGame() {
            reset();
            view.showState(masked(), tried, remaining);
        }

        @Override
        public void onResume() {
            view.showState(masked(), tried, remaining);
        }

        @Override
        public void onQuit() {
            view.showDisconnected("partie quittée");
        }

        @Override
        public void onRawMessage(String rawLine) {
            System.out.println("raw: " + rawLine);
            view.showError("INVALID_MESSAGE (simulé)");
        }

        private void reset() {
            tried.clear();
            remaining = 6;
        }

        private String masked() {
            StringBuilder sb = new StringBuilder();
            for (char c : SECRET.toCharArray()) {
                sb.append(tried.contains(c) ? c : '_');
            }
            return sb.toString();
        }
    }
}
