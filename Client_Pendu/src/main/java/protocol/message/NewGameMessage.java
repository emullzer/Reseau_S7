package protocol.message;

import protocol.enumMessage.MessageType;

import java.util.Objects;

/**
 * Classe permettant au client de créer un nouveau jeu
 */
public class NewGameMessage implements Message{
    //identifiant de la requête
    private int requestId;
    //nom du joueur
    private String playerName;
    //type de la requête, en const car le type ne changera jamais
    private static final MessageType TYPE=MessageType.NEW_GAME;

    /**
     * Constructeur de la classe NewGameMessage
     * @param requestId l'identifiant de la requête
     * @param playerName le nom du joueur
     */
    public NewGameMessage(int requestId, String playerName) {
        if(requestId<0||requestId==0){
            throw new IllegalArgumentException("requestId must be positive");
        }
        if(playerName==null||playerName.isEmpty()){
            throw new IllegalArgumentException("playerName must not be null or empty");
        }
        this.requestId = requestId;
        this.playerName = playerName;
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
     * Permet de comparer deux NewGameMessage
     * @param o l'objet à comparer
     * @return true si les NewGameMessage sont identiques, false sinon
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NewGameMessage that = (NewGameMessage) o;
        return requestId == that.requestId && Objects.equals(playerName, that.playerName);
    }

    /**
     * Permet d'hacher un NewGameMessage
     * @return l'hash du NewGameMessage
     */
    @Override
    public int hashCode() {
        return Objects.hash(requestId, playerName);
    }

    /**
     * Permet de convertir un NewGameMessage en String
     * @return le NewGameMessage en String
     */
    @Override
    public String toString() {
        return "NewGameMessage{" +
                "requestId=" + requestId +
                ", playerName='" + playerName + '\'' +
                '}';
    }
}
