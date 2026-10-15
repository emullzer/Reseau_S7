package Protocol.Message;

import Protocol.MessageType;

/**
 * Interface permettant de représenter un message
 */
public interface Message {

    /**
     * Permet d'avoir le type du message
     * @return le type du message
     */
    MessageType getType();

    /**
     * Permet d'avoir l'identifiant de la requête
     * @return l'identifiant de la requête
     */
    int getRequestId();
}
