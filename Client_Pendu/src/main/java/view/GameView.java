package view;

import java.util.List;

public interface GameView {

    void setListener(ViewListener listener);

    void showConnecting();

    void showConnected();

    void showState(String word, List<Character> triedLetters, int remainingAttempts);

    void showError(String message);

    void showDisconnected(String reason);

    void showGameOver(boolean won, String word);
}
