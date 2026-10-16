package protocol.message;

import protocol.enumMessage.MessageType;

import java.util.Objects;

/**
 * Classe permettant au client de reprendre un jeu non terminé
 */
//TODO voir si on change pas le session_id plutôt par un Sxx plutôt que xx et voir si player_name suffit ?
public class ResumeMessage implements Message{
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
    private static final MessageType TYPE=MessageType.RESUME;
    /**
     * Nom du joueur
     */
    String playerName;

    /**
     * Constructeur de la classe ResumeMessage
     * @param requestId l'identifiant de la requête
     * @param sessionId l'identifiant de la session
     * @param playerName le nom du joueur
     */
    public ResumeMessage(int requestId, int sessionId, String playerName) {
        if(requestId<0||requestId==0){
            throw new IllegalArgumentException("requestId must be positive");
        }
        if(sessionId<0||sessionId==0){
            throw new IllegalArgumentException("sessionId must be positive");
        }
        if(playerName==null||playerName.isEmpty()){
            throw new IllegalArgumentException("playerName must not be null or empty");
        }
        this.requestId = requestId;
        this.sessionId = sessionId;
        this.playerName = playerName;
    }

    /**
     * Permet de récupérer l'identifiant de la session
     * @return l'identifiant de la session
     */
    public int getSessionId() {
        return sessionId;
    }

    /**
     * Permet de récupérer le nom du joueur
     * @return le nom du joueur
     */
    public String getPlayerName() {
        return playerName;
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
     * Compare deux ResumeMessage
     * @param o   la référence objet
     * @return true si les ResumeMessage sont identiques, false sinon
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ResumeMessage that = (ResumeMessage) o;
        return requestId == that.requestId && sessionId == that.sessionId && Objects.equals(playerName, that.playerName);
    }

    /**
     * Permet de convertir un ResumeMessage en String
     * @return le ResumeMessage en String
     */
    @Override
    public String toString() {
        return "ResumeMessage{" +
                "requestId=" + requestId +
                ", sessionId=" + sessionId +
                ", playerName='" + playerName + '\'' +
                '}';
    }

    /**
     * Permet d'hacher un ResumeMessage
     * @return l'hash du ResumeMessage
     */
    @Override
    public int hashCode() {
        return Objects.hash(requestId, sessionId, playerName);
    }
}
