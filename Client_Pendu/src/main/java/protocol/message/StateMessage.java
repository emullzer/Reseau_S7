package protocol.message;

import protocol.enumMessage.MessageState;
import protocol.enumMessage.MessageType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Classe permettant au client de recevoir les informations du jeu
 */
public class StateMessage implements Message {
    /**
     * type du message
     */
    private static final MessageType TYPE = MessageType.STATE;

    /**
     * Identifiant de la requête
     */
    private final int requestId;
    /**
     * Identifiant de la session
     */
    private final int sessionId;
    /**
     * Statut du jeu (IN_PROGRESS, WIN, LOSE, ABORTED)
     */
    private final MessageState state;
    /**
     * Mot masqué (par exemple "_ _ a _")
     */
    private final String word;
    /**
     * Lettres déjà essayées
     */
    private final List<Character> triedLetters;
    /**
     * Nombre de tentatives restantes
     */
    private final int remainingAttempts;
    /**
     * Mot révélé, présent uniquement si la partie est perdue (peut être null)
     */
    private final String revealedWord;

    /**
     * Constructeur complet de StateMessage
     * @param requestId identifiant de la requête
     * @param sessionId identifiant de la session
     * @param state statut du jeu
     * @param word mot masqué
     * @param triedLetters lettres déjà essayées
     * @param remainingAttempts nombre de tentatives restantes
     * @param revealedWord mot révélé, ou null s'il est absent
     */
    public StateMessage(int requestId, int sessionId, MessageState state, String word,
                        List<Character> triedLetters, int remainingAttempts, String revealedWord) {
        if (requestId <= 0) {
            throw new IllegalArgumentException("requestId must be positive");
        }
        if (sessionId <= 0) {
            throw new IllegalArgumentException("sessionId must be positive");
        }
        if (state == null) {
            throw new IllegalArgumentException("state must not be null");
        }
        if (word == null || word.isBlank()) {
            throw new IllegalArgumentException("word must not be null or blank");
        }
        if (triedLetters == null) {
            throw new IllegalArgumentException("triedLetters must not be null");
        }
        if (remainingAttempts < 0) {
            throw new IllegalArgumentException("remainingAttempts must not be negative");
        }
        if (revealedWord != null && revealedWord.isBlank()) {
            throw new IllegalArgumentException("revealedWord must not be blank");
        }
        this.requestId = requestId;
        this.sessionId = sessionId;
        this.state = state;
        this.word = word;
        this.triedLetters = List.copyOf(triedLetters);
        this.remainingAttempts = remainingAttempts;
        this.revealedWord = revealedWord;
    }

    /**
     * Constructeur sans mot révélé
     * @param requestId identifiant de la requête
     * @param sessionId identifiant de la session
     * @param state statut du jeu
     * @param word mot masqué
     * @param triedLetters lettres déjà essayées
     * @param remainingAttempts nombre de tentatives restantes
     */
    public StateMessage(int requestId, int sessionId, MessageState state, String word,
                        List<Character> triedLetters, int remainingAttempts) {
        this(requestId, sessionId, state, word, triedLetters, remainingAttempts, null);
    }

    /**
     * Retourne le statut du jeu
     * @return le statut du jeu
     */
    public MessageState getState() {
        return state;
    }

    /**
     * Retourne le mot masqué
     * @return le mot masqué
     */
    public String getWord() {
        return word;
    }

    /**
     * Retourne les lettres essayées (liste non modifiable)
     * @return les lettres essayées
     */
    public List<Character> getTriedLetters() {
        return triedLetters;
    }

    /**
     * Retourne le nombre de tentatives restantes
     * @return le nombre de tentatives restantes
     */
    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    /**
     * Retourne l'identifiant de la session
     * @return l'identifiant de la session
     */
    public int getSessionId() {
        return sessionId;
    }

    /**
     * Retourne le mot révélé, présent uniquement si la partie est perdue
     * @return le mot révélé, ou Optional.empty() s'il est absent
     */
    public Optional<String> getRevealedWord() {
        return Optional.ofNullable(revealedWord);
    }

    @Override
    public MessageType getType() {
        return TYPE;
    }

    @Override
    public int getRequestId() {
        return requestId;
    }

    /**
     * Compare deux StateMessage
     * @param o l'objet à comparer
     * @return true si les StateMessage sont identiques, false sinon
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StateMessage that = (StateMessage) o;
        return requestId == that.requestId
                && sessionId == that.sessionId
                && remainingAttempts == that.remainingAttempts
                && state == that.state
                && Objects.equals(word, that.word)
                && Objects.equals(triedLetters, that.triedLetters)
                && Objects.equals(revealedWord, that.revealedWord);
    }

    /**
     * Calcule le hash d'un StateMessage
     * @return le hash du StateMessage
     */
    @Override
    public int hashCode() {
        return Objects.hash(requestId, sessionId, state, word, triedLetters,
                remainingAttempts, revealedWord);
    }

    /**
     * Convertit un StateMessage en String
     * @return le StateMessage en String
     */
    @Override
    public String toString() {
        return "StateMessage{" +
                "requestId=" + requestId +
                ", sessionId=" + sessionId +
                ", state=" + state +
                ", word='" + word + '\'' +
                ", triedLetters=" + triedLetters +
                ", remainingAttempts=" + remainingAttempts +
                ", revealedWord=" + revealedWord +
                '}';
    }
}
