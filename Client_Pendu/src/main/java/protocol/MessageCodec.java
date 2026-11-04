package protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import protocol.enumMessage.MessageError;
import protocol.enumMessage.MessageState;
import protocol.enumMessage.MessageType;
import protocol.message.ErrorMessage;
import protocol.message.HelloMessage;
import protocol.message.LetterMessage;
import protocol.message.Message;
import protocol.message.NewGameMessage;
import protocol.message.QuitMessage;
import protocol.message.ResumeMessage;
import protocol.message.StateMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageCodec {

    // objet mapper pour la conversion JSON en objet Java
    private final ObjectMapper mapper = new ObjectMapper();


    public String encode(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }

        ObjectNode root = mapper.createObjectNode();
        root.put("type", message.getType().name());
        root.put("request_id", message.getRequestId());

        switch (message.getType()) {
            case HELLO -> {
                HelloMessage m = (HelloMessage) message;
                root.putObject("payload").put("player_name", m.getPlayerName());
            }
            case NEW_GAME -> {
                NewGameMessage m = (NewGameMessage) message;
                root.putObject("payload").put("player_name", m.getPlayerName());
            }
            case LETTER -> {
                LetterMessage m = (LetterMessage) message;
                root.put("session_id", m.getSessionId());
                root.putObject("payload").put("letter", String.valueOf(m.getLetter()));
            }
            case QUIT -> {
                QuitMessage m = (QuitMessage) message;
                root.put("session_id", m.getSessionId());
                root.putObject("payload");
            }
            case RESUME -> {
                ResumeMessage m = (ResumeMessage) message;
                root.put("session_id", m.getSessionId());
                root.putObject("payload").put("player_name", m.getPlayerName());
            }
            case STATE -> {
                StateMessage m = (StateMessage) message;
                root.put("session_id", m.getSessionId());
                ObjectNode payload = root.putObject("payload");
                payload.put("status", m.getState().name());
                payload.put("word", m.getWord());
                ArrayNode letters = payload.putArray("tried_letters");
                for (char c : m.getTriedLetters()) {
                    letters.add(String.valueOf(c));
                }
                payload.put("remaining_attempts", m.getRemainingAttempts());
                if (m.getRevealedWord().isPresent()) {
                    payload.put("revealed_word", m.getRevealedWord().get());
                }
            }
            case ERROR -> {
                ErrorMessage m = (ErrorMessage) message;
                root.put("code", m.getCode().name());
                root.put("message", m.getMessage());
            }
        }
        return root.toString();
    }


    public Message decode(String line) throws ProtocolException {
        if (line == null || line.isBlank()) {
            throw new ProtocolException("Empty message");
        }

        JsonNode root;
        try {
            root = mapper.readTree(line);
        } catch (JsonProcessingException e) {
            throw new ProtocolException("Invalid JSON", e);
        }
        if (root == null || !root.isObject()) {
            throw new ProtocolException("Message must be a JSON object");
        }

        try {
            MessageType type = MessageType.valueOf(text(root, "type"));
            return switch (type) {
                case HELLO -> decodeHello(root);
                case NEW_GAME -> decodeNewGame(root);
                case LETTER -> decodeLetter(root);
                case QUIT -> decodeQuit(root);
                case RESUME -> decodeResume(root);
                case STATE -> decodeState(root);
                case ERROR -> decodeError(root);
            };
        } catch (IllegalArgumentException e) {
            throw new ProtocolException("Invalid message: " + e.getMessage(), e);
        }
    }

    private Message decodeHello(JsonNode root) throws ProtocolException {
        int requestId = integer(root, "request_id");
        String playerName = text(payload(root), "player_name");
        return new HelloMessage(requestId, playerName);
    }

    private Message decodeNewGame(JsonNode root) throws ProtocolException {
        int requestId = integer(root, "request_id");
        String playerName = text(payload(root), "player_name");
        return new NewGameMessage(requestId, playerName);
    }

    private Message decodeLetter(JsonNode root) throws ProtocolException {
        int requestId = integer(root, "request_id");
        String sessionId = text(root, "session_id");
        String letter = text(payload(root), "letter");
        if (letter.length() != 1) {
            throw new ProtocolException("letter must be a single character");
        }
        return new LetterMessage(requestId, sessionId, letter.charAt(0));
    }

    private Message decodeQuit(JsonNode root) throws ProtocolException {
        int requestId = integer(root, "request_id");
        String sessionId = text(root, "session_id");
        return new QuitMessage(requestId, sessionId);
    }

    private Message decodeResume(JsonNode root) throws ProtocolException {
        int requestId = integer(root, "request_id");
        String sessionId = text(root, "session_id");
        String playerName = text(payload(root), "player_name");
        return new ResumeMessage(requestId, sessionId, playerName);
    }

    private Message decodeState(JsonNode root) throws ProtocolException {
        int requestId = integer(root, "request_id");
        String sessionId = text(root, "session_id");
        JsonNode payload = payload(root);

        MessageState status = MessageState.valueOf(text(payload, "status"));
        String word = text(payload, "word");
        int remaining = integer(payload, "remaining_attempts");

        JsonNode lettersNode = payload.get("tried_letters");
        if (lettersNode == null || !lettersNode.isArray()) {
            throw new ProtocolException("tried_letters must be an array");
        }
        List<Character> triedLetters = new ArrayList<>();
        for (JsonNode item : lettersNode) {
            if (!item.isTextual() || item.textValue().length() != 1) {
                throw new ProtocolException("tried_letters must be a single character");
            }
            triedLetters.add(item.textValue().charAt(0));
        }

        String revealedWord = null;
        if (payload.hasNonNull("revealed_word")) {
            revealedWord = text(payload, "revealed_word");
        }

        return new StateMessage(requestId, sessionId, status, word,
                triedLetters, remaining, revealedWord);
    }

    private Message decodeError(JsonNode root) throws ProtocolException {
        int requestId = 0;
        if (root.hasNonNull("request_id")) {
            requestId = integer(root, "request_id");
        }
        JsonNode source;
        if (root.hasNonNull("code")) {
            source = root;
        } else {
            source = payload(root);
        }
        MessageError code = MessageError.valueOf(text(source, "code"));
        String message = text(source, "message");
        return new ErrorMessage(message, code, requestId);
    }


    private JsonNode payload(JsonNode root) throws ProtocolException {
        JsonNode payload = root.get("payload");
        if (payload == null || !payload.isObject()) {
            throw new ProtocolException("payload must be a JSON object");
        }
        return payload;
    }

    //permet de faire la conversion en text
    private String text(JsonNode node, String field) throws ProtocolException {
        JsonNode value = node.get(field);
        if (value == null || !value.isTextual()) {
            throw new ProtocolException(field + " must be a string");
        }
        return value.textValue();
    }

    //permet de faire la conversion en int
    private int integer(JsonNode node, String field) throws ProtocolException {
        JsonNode value = node.get(field);
        if (value == null || !value.isInt()) {
            throw new ProtocolException(field + " must be an integer");
        }
        return value.intValue();
    }
}
