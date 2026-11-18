package protocol.message;

import protocol.enumMessage.MessageType;

import java.util.Objects;

public class JoinRoomMessage implements Message {
    private final int requestId;
    private final String sessionId;
    private final String playerName;
    private static final MessageType TYPE = MessageType.JOIN_ROOM;

    public JoinRoomMessage(int requestId, String sessionId, String playerName) {
        if (requestId <= 0) {
            throw new IllegalArgumentException("requestId must be positive");
        }
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId mustn't be null or empty");
        }
        if (playerName == null || playerName.isEmpty()) {
            throw new IllegalArgumentException("playerName must not be null or empty");
        }
        this.requestId = requestId;
        this.sessionId = sessionId;
        this.playerName = playerName;
    }

    public String getSessionId() {
        return sessionId;
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
        JoinRoomMessage that = (JoinRoomMessage) o;
        return requestId == that.requestId && Objects.equals(sessionId, that.sessionId) && Objects.equals(playerName, that.playerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, sessionId, playerName);
    }
}