package Protocol.Message;

import Protocol.MessageState;
import Protocol.MessageType;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

/**
 * Classe permettant au client de recevoir les informations du jeu
 */
public class StateMessage implements Message{
    /**
     * Identifiant de la requête
     */
    private int requestId;
    /**
     * Identifiant de la session
     */
    private int sessionId;
    /**
     * Statut du jeu (IN_PROGRESS, WIN, LOSE)
     */
    private MessageState state;
    /**
     * Tableau contenant les lettres essayées
     */
    private ArrayList<Character> triedLetters;
    /**
     * type du message
     */
    private static final MessageType TYPE=MessageType.STATE;
    /**
     * Nombre de tentatives restantes
     */
    private int remainingAttempts;
    /**
     * Le mot révélé
     */
    private Optional<String> revealedWord;

    /**
     * Constructeur de la classe StateMessage
     * @param requestId identifiant de la requête
     * @param sessionId identifiant de la session
     * @param state statut du jeu
     * @param tried_letters tableau contenant les lettres essayées
     * @param remaining_attempts nombre de tentatives restantes
     */
    public StateMessage(int requestId, int sessionId, MessageState state, ArrayList<Character> tried_letters, int remaining_attempts) {
        if(requestId<0||requestId==0){
            throw new IllegalArgumentException("requestId must be positive");
        }
        if(sessionId<0||sessionId==0){
            throw new IllegalArgumentException("sessionId must be positive");
        }
        if(tried_letters==null){
            throw new IllegalArgumentException("triedLetters must not be null");
        }
        if(remaining_attempts<0){
            throw new IllegalArgumentException("remainingAttempts must be positive");
        }
        if(state==null){
            throw new IllegalArgumentException("state must not be null");
        }
        this.requestId = requestId;
        this.sessionId = sessionId;
        this.state = state;
        this.triedLetters = tried_letters;
        this.remainingAttempts = remaining_attempts;
    }

    /**
     * Retourne le statut du jeu
     * @return le statut du jeu
     */
    public MessageState getState() {
        return state;
    }

    /**
     * Retourne le tableau contenant les lettres essayées
     * @return le tableau contenant les lettres essayées
     */
    public ArrayList<Character> getTriedLetters() {
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
     * @return la session
     */
    public int getSessionId() {
        return sessionId;
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
     * @param o   la référence objet
     * @return true si les StateMessage sont identiques, false sinon
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StateMessage that = (StateMessage) o;
        return requestId == that.requestId && sessionId == that.sessionId && remainingAttempts == that.remainingAttempts && Objects.equals(state, that.state) && Objects.equals(triedLetters, that.triedLetters);
    }

    /**
     * Permet d'hacher un StateMessage
     * @return l'hash du StateMessage
     */
    @Override
    public int hashCode() {
        return Objects.hash(requestId, sessionId, state, triedLetters, remainingAttempts);
    }

    /**
     * Permet de convertir un StateMessage en String
     * @return le StateMessage en String
     */
    @Override
    public String toString() {
        return "StateMessage{" +
                "requestId=" + requestId +
                ", sessionId=" + sessionId +
                ", state=" + state +
                ", triedLetters=" + triedLetters +
                ", remainingAttempts=" + remainingAttempts +
                '}';
    }
}
