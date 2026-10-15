package Protocol.Message;

import Protocol.MessageType;

import java.util.Objects;

/**
 * Classe permettant au client de se présenter au serveur
 */
public class HelloMessage implements Message{
    /**
     * identifiant de la requête
     */
    private int requestId;
    /**
     * nom du joueur
     */
    private String playerName;
    /**
     * type de la requête, en const car le type ne changera jamais
     */
    private static final MessageType TYPE=MessageType.HELLO;

    /**
     * Constructeur de la classe
     * @param requestId identifiant de la requête
     * @param playerName nom du joueur
     */
    public HelloMessage(int requestId, String playerName) {

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
     * Permet d'avoir le nom du joueur
     * @return le nom du joueur
     */
    public String getPlayerName() {
        return playerName;
    }

    @Override
    public int getRequestId() {
        return requestId;
    }

    @Override
    public MessageType getType() {
        return TYPE;
    }

    /**
     * Permet de comparer deux HelloMessage
     * @param o l'objet à comparer
     * @return true si les HelloMessage sont identiques, false sinon
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        HelloMessage that = (HelloMessage) o;
        return requestId == that.requestId && Objects.equals(playerName, that.playerName);
    }

    /**
     * Permet d'hacher un HelloMessage
     * @return l'hash du HelloMessage
     */
    @Override
    public int hashCode() {
        return Objects.hash(requestId, playerName);
    }

    /**
     * Permet de convertir un HelloMessage en String
     * @return le HelloMessage en String
     */
    @Override
    public String toString() {
        return "HelloMessage{" +
                "requestId=" + requestId +
                ", playerName='" + playerName + '\'' +
                '}';
    }
}
