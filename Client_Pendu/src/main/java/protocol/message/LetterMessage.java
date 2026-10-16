package protocol.message;

import protocol.enumMessage.MessageType;

import java.util.Objects;

/**
 * Classe permettant au client d'envoyer une lettre
 */
public class LetterMessage implements Message{
    /**
     * l'identifiant de la requête
     */
    private int requestId;
    /**
     * l'identifiant de la session
     */
    private int sessionId;
    /**
     * type du message
     */
    private static final MessageType TYPE=MessageType.LETTER;
    /**
     * la lettre à envoyer
     */
    private char letter;

    /**
     * Constructeur de la classe LetterMessage
     * @param requestId l'identifiant de la requête
     * @param sessionId l'identifiant de la session
     * @param letter la lettre à envoyer
     */
    public LetterMessage(int requestId, int sessionId, char letter) {
        if(requestId<0||requestId==0){
            throw new IllegalArgumentException("requestId must be positive");
        }
        if(sessionId<0||sessionId==0){
            throw new IllegalArgumentException("sessionId must be positive");
        }
        if(Character.isDigit(letter)){
            throw new IllegalArgumentException("letter must not be a digit");
        }
        if(Character.isWhitespace(letter)){
            throw new IllegalArgumentException("letter must not be a whitespace");
        }
        if(!Character.isLetter(letter)){
            throw new IllegalArgumentException("letter must be a letter");
        }
        this.requestId = requestId;
        this.sessionId = sessionId;
        this.letter = letter;
    }

    @Override
    public MessageType getType() {
        return TYPE;
    }

    @Override
    public int getRequestId() {
        return requestId;
    }

    public char getLetter() {
        return letter;
    }

    public int getSessionId() {
        return sessionId;
    }

    /**
     * Permet de comparer deux LetterMessage
     * @param o   la référence objet
     * @return true si les LetterMessage sont identiques, false sinon
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LetterMessage that = (LetterMessage) o;
        return requestId == that.requestId && sessionId == that.sessionId && letter == that.letter;
    }

    /**
     * Permet d'hacher un LetterMessage
     * @return l'hash du LetterMessage
     */
    @Override
    public int hashCode() {
        return Objects.hash(requestId, sessionId, letter);
    }

    /**
     * Permet de convertir un LetterMessage en String
     * @return le LetterMessage en String
     */
    @Override
    public String toString() {
        return "LetterMessage{" +
                "requestId=" + requestId +
                ", sessionId=" + sessionId +
                ", letter=" + letter +
                '}';
    }
}
