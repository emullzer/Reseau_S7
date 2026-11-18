package protocol.message;

import protocol.enumMessage.MessageType;

import java.util.Objects;

public class CreateRoomMessage implements Message {
    private final int requestId;
    private final String playerName;
    private static final MessageType TYPE = MessageType.CREATE_ROOM;

    public CreateRoomMessage(int requestId, String playerName) {
        if (requestId <= 0) {
            throw new IllegalArgumentException("requestId must be positive");
        }
        if (playerName == null || playerName.isEmpty()) {
            throw new IllegalArgumentException("playerName must not be null or empty");
        }
        this.requestId = requestId;
        this.playerName = playerName;
    }

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CreateRoomMessage that = (CreateRoomMessage) o;
        return requestId == that.requestId && Objects.equals(playerName, that.playerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, playerName);
    }
}