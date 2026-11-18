package protocol.message;

import protocol.enumMessage.MessageType;

import java.util.Objects;

public class ChatMessage implements Message {
    private final int requestId;
    private final String sessionId;
    private final String playerName;
    private final String text;
    private static final MessageType TYPE = MessageType.CHAT;

    public ChatMessage(int requestId, String sessionId, String playerName, String text) {
        if (requestId <= 0) {
            throw new IllegalArgumentException("requestId must be positive");
        }
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId mustn't be null or empty");
        }
        if (playerName == null || playerName.isEmpty()) {
            throw new IllegalArgumentException("playerName must not be null or empty");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text must not be null or blank");
        }
        this.requestId = requestId;
        this.sessionId = sessionId;
        this.playerName = playerName;
        this.text = text;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getText() {
        return text;
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
        ChatMessage that = (ChatMessage) o;
        return requestId == that.requestId
                && Objects.equals(sessionId, that.sessionId)
                && Objects.equals(playerName, that.playerName)
                && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, sessionId, playerName, text);
    }
}