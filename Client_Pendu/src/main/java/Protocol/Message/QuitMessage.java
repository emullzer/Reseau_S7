package Protocol.Message;

import Protocol.MessageType;

import java.util.Objects;

/**
 * Classe permettant au client de quitter le jeu
 */
public class QuitMessage implements Message{
    /**
     * Identifiant de la requête
     */
    private int requestId;
    /**
     * Identifiant de la session
     */
    private int sessionId;
    /**
     * Type du message
     */
    private static final MessageType TYPE=MessageType.QUIT;

    /**
     * Constructeur de la classe QuitMessage
     * @param requestId l'identifiant de la requête
     * @param sessionId l'identifiant de la session
     */
    public QuitMessage(int requestId, int sessionId) {
        this.requestId = requestId;
        this.sessionId = sessionId;
    }

    /**
     * Permet de comparer deux QuitMessage
     * @param o   la référence objet
     * @return true si les QuitMessage sont identiques, false sinon
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        QuitMessage that = (QuitMessage) o;
        return requestId == that.requestId && sessionId == that.sessionId;
    }

    /**
     * Permet d'hacher un QuitMessage
     * @return l'hash du QuitMessage
     */
    @Override
    public int hashCode() {
        return Objects.hash(requestId, sessionId);
    }

    /**
     * Permet de convertir un QuitMessage en String
     * @return le QuitMessage en String
     */
    @Override
    public String toString() {
        return "QuitMessage{" +
                "requestId=" + requestId +
                ", sessionId=" + sessionId +
                '}';
    }

    @Override
    public MessageType getType() {
        return null;
    }

    @Override
    public int getRequestId() {
        return 0;
    }
}
