package Protocol.Message;

import Protocol.MessageType;

import java.util.Objects;

/**
 * Classe permettant de représenter un message d'erreur
 */
public class ErrorMessage implements Message{
    /**
     * message d'erreur
     */
    private String message;
    /**
     * code d'erreur
     */
    private int code;
    /**
     * identifiant de la requête
     */
    private int requestId;
    /**
     * type du message
     */
    private static final MessageType TYPE=MessageType.ERROR;

    /**
     * Constructeur de la classe ErrorMessage
     * @param message message d'erreur
     * @param code code d'erreur
     * @param requestId identifiant de la requête
     */
    public ErrorMessage(String message, int code, int requestId) {
        this.message = message;
        this.code = code;
        this.requestId = requestId;
    }

    /**
     * Retourne le code d'erreur
     * @return le code d'erreur
     */
    public int getCode() {
        return code;
    }

    /**
     * Retourne le message d'erreur
     * @return le message d'erreur
     */
    public String getMessage() {
        return message;
    }

    /**
     * Permet de comparer deux ErrorMessage
     * @param o   la référence objet
     * @return true si les ErrorMessage sont identiques, false sinon
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ErrorMessage that = (ErrorMessage) o;
        return code == that.code && requestId == that.requestId && Objects.equals(message, that.message);
    }

    /**
     * Permet d'hacher un ErrorMessage
     * @return l'hash du ErrorMessage
     */
    @Override
    public int hashCode() {
        return Objects.hash(message, code, requestId);
    }

    /**
     * Permet de convertir un ErrorMessage en String
     * @return le ErrorMessage en String
     */
    @Override
    public String toString() {
        return "ErrorMessage{" +
                "message='" + message + '\'' +
                ", code=" + code +
                ", requestId=" + requestId +
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
