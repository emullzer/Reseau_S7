package protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import protocol.enumMessage.MessageError;
import protocol.enumMessage.MessageState;
import protocol.message.ErrorMessage;
import protocol.message.HelloMessage;
import protocol.message.LetterMessage;
import protocol.message.Message;
import protocol.message.NewGameMessage;
import protocol.message.QuitMessage;
import protocol.message.ResumeMessage;
import protocol.message.StateMessage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessageCodecTest {
    private final MessageCodec codec = new MessageCodec();
    private final ObjectMapper mapper = new ObjectMapper();

    private void assertRoundTrip(Message m) throws ProtocolException {
        assertEquals(m, codec.decode(codec.encode(m)));
    }

    private void assertRejected(String json) {
        assertThrows(ProtocolException.class, () -> codec.decode(json));
    }

    @Test
    void helloAllerRetour() throws ProtocolException {
        assertRoundTrip(new HelloMessage(1, "Emeric"));
    }

    @Test
    void newGameAllerRetour() throws ProtocolException {
        assertRoundTrip(new NewGameMessage(2, "Emeric"));
    }

    @Test
    void letterAllerRetour() throws ProtocolException {
        assertRoundTrip(new LetterMessage(3, "S42", 'e'));
        assertRoundTrip(new LetterMessage(3, "S42", 'é'));
    }

    @Test
    void quitAllerRetour() throws ProtocolException {
        assertRoundTrip(new QuitMessage(4, "S42"));
    }

    @Test
    void resumeAllerRetour() throws ProtocolException {
        assertRoundTrip(new ResumeMessage(5, "S42", "Emeric"));
    }

    @Test
    void stateAllerRetour() throws ProtocolException {
        assertRoundTrip(new StateMessage(6, "S42", MessageState.IN_PROGRESS, "_ e _",
                List.of('e', 'a'), 4));
    }

    @Test
    void stateListeVideAllerRetour() throws ProtocolException {
        assertRoundTrip(new StateMessage(1, "S1", MessageState.IN_PROGRESS, "_ _ _",
                List.of(), 6));
    }

    @Test
    void stateAvecMotRevelеAllerRetour() throws ProtocolException {
        assertRoundTrip(new StateMessage(7, "S42", MessageState.LOSE, "_ e _",
                List.of('e', 'z'), 0, "chat"));
    }

    @Test
    void errorAllerRetour() throws ProtocolException {
        assertRoundTrip(new ErrorMessage("Une seule lettre est attendue.",
                MessageError.INVALID_MESSAGE, 8));
    }

    @Test
    void helloFormeJson() throws Exception {
        JsonNode node = mapper.readTree(codec.encode(new HelloMessage(1, "Emeric")));
        assertEquals("HELLO", node.get("type").textValue());
        assertEquals(1, node.get("request_id").intValue());
        assertEquals("Emeric", node.get("payload").get("player_name").textValue());
    }

    @Test
    void letterFormeJson() throws Exception {
        JsonNode node = mapper.readTree(codec.encode(new LetterMessage(17, "S42", 'e')));
        assertEquals("LETTER", node.get("type").textValue());
        assertEquals(17, node.get("request_id").intValue());
        assertEquals("S42", node.get("session_id").textValue());
        assertEquals("e", node.get("payload").get("letter").textValue());
    }

    @Test
    void quitFormeJson() throws Exception {
        JsonNode node = mapper.readTree(codec.encode(new QuitMessage(4, "S42")));
        assertEquals("QUIT", node.get("type").textValue());
        assertEquals("S42", node.get("session_id").textValue());
        assertTrue(node.get("payload").isObject());
    }

    @Test
    void stateFormeJson() throws Exception {
        JsonNode node = mapper.readTree(codec.encode(new StateMessage(6, "S42",
                MessageState.IN_PROGRESS, "_ e _", List.of('e', 'a'), 4)));
        JsonNode payload = node.get("payload");
        assertEquals("STATE", node.get("type").textValue());
        assertEquals("S42", node.get("session_id").textValue());
        assertEquals("IN_PROGRESS", payload.get("status").textValue());
        assertEquals("_ e _", payload.get("word").textValue());
        assertEquals(2, payload.get("tried_letters").size());
        assertEquals("e", payload.get("tried_letters").get(0).textValue());
        assertEquals(4, payload.get("remaining_attempts").intValue());
        assertFalse(payload.has("revealed_word"));
    }

    @Test
    void stateAvecMotRevelеFormeJson() throws Exception {
        JsonNode node = mapper.readTree(codec.encode(new StateMessage(7, "S42",
                MessageState.LOSE, "_ _", List.of(), 0, "chat")));
        assertEquals("chat", node.get("payload").get("revealed_word").textValue());
    }

    @Test
    void errorFormeJson() throws Exception {
        JsonNode node = mapper.readTree(codec.encode(new ErrorMessage("msg",
                MessageError.INVALID_STATE, 9)));
        assertEquals("ERROR", node.get("type").textValue());
        assertEquals(9, node.get("request_id").intValue());
        assertEquals("INVALID_STATE", node.get("code").textValue());
        assertEquals("msg", node.get("message").textValue());
    }

    @Test
    void jsonSansRetourALaLigne() {
        assertFalse(codec.encode(new HelloMessage(1, "Emeric")).contains("\n"));
        assertFalse(codec.encode(new StateMessage(1, "S1", MessageState.IN_PROGRESS,
                "_ _", List.of('a'), 5)).contains("\n"));
    }

    @Test
    void encodeNull() {
        assertThrows(IllegalArgumentException.class, () -> codec.encode(null));
    }

    @Test
    void jsonMalForme() {
        assertRejected("{pas du json");
    }

    @Test
    void racineNonObjet() {
        assertRejected("42");
        assertRejected("[]");
    }

    @Test
    void ligneVideOuNull() {
        assertRejected("");
        assertRejected("   ");
        assertRejected(null);
    }

    @Test
    void typeAbsentOuInconnu() {
        assertRejected("{\"request_id\":1,\"payload\":{\"player_name\":\"Emeric\"}}");
        assertRejected("{\"type\":\"BIDON\",\"request_id\":1,\"payload\":{}}");
        assertRejected("{\"type\":42,\"request_id\":1,\"payload\":{}}");
    }

    @Test
    void helloChampsInvalides() {
        assertRejected("{\"type\":\"HELLO\",\"request_id\":1,\"payload\":{}}");
        assertRejected("{\"type\":\"HELLO\",\"request_id\":1}");
        assertRejected("{\"type\":\"HELLO\",\"request_id\":\"1\",\"payload\":{\"player_name\":\"E\"}}");
        assertRejected("{\"type\":\"HELLO\",\"request_id\":0,\"payload\":{\"player_name\":\"E\"}}");
        assertRejected("{\"type\":\"HELLO\",\"request_id\":1,\"payload\":{\"player_name\":\"\"}}");
    }

    @Test
    void letterChampsInvalides() {
        assertRejected("{\"type\":\"LETTER\",\"request_id\":1,\"session_id\":42,\"payload\":{\"letter\":\"a\"}}");
        assertRejected("{\"type\":\"LETTER\",\"request_id\":1,\"payload\":{\"letter\":\"a\"}}");
        assertRejected("{\"type\":\"LETTER\",\"request_id\":1,\"session_id\":\"S1\",\"payload\":{\"letter\":\"ab\"}}");
        assertRejected("{\"type\":\"LETTER\",\"request_id\":1,\"session_id\":\"S1\",\"payload\":{\"letter\":\"\"}}");
        assertRejected("{\"type\":\"LETTER\",\"request_id\":1,\"session_id\":\"S1\",\"payload\":{\"letter\":\"1\"}}");
    }

    @Test
    void quitSansSession() {
        assertRejected("{\"type\":\"QUIT\",\"request_id\":1}");
    }

    @Test
    void quitSansPayloadAccepte() throws ProtocolException {
        assertEquals(new QuitMessage(1, "S1"),
                codec.decode("{\"type\":\"QUIT\",\"request_id\":1,\"session_id\":\"S1\"}"));
    }

    @Test
    void stateChampsInvalides() {
        String debut = "{\"type\":\"STATE\",\"request_id\":1,\"session_id\":\"S1\",\"payload\":";
        assertRejected(debut + "{\"status\":\"BIDON\",\"word\":\"_\",\"tried_letters\":[],\"remaining_attempts\":6}}");
        assertRejected(debut + "{\"status\":\"IN_PROGRESS\",\"tried_letters\":[],\"remaining_attempts\":6}}");
        assertRejected(debut + "{\"status\":\"IN_PROGRESS\",\"word\":\"_\",\"remaining_attempts\":6}}");
        assertRejected(debut + "{\"status\":\"IN_PROGRESS\",\"word\":\"_\",\"tried_letters\":[\"ab\"],\"remaining_attempts\":6}}");
        assertRejected(debut + "{\"status\":\"IN_PROGRESS\",\"word\":\"_\",\"tried_letters\":[1],\"remaining_attempts\":6}}");
        assertRejected(debut + "{\"status\":\"IN_PROGRESS\",\"word\":\"_\",\"tried_letters\":[]}}");
        assertRejected(debut + "{\"status\":\"IN_PROGRESS\",\"word\":\"_\",\"tried_letters\":[],\"remaining_attempts\":-1}}");
        assertRejected(debut + "{\"status\":\"LOSE\",\"word\":\"_\",\"tried_letters\":[],\"remaining_attempts\":0,\"revealed_word\":42}}");
    }

    @Test
    void stateRevealedWordNullAccepte() throws ProtocolException {
        Message m = codec.decode("{\"type\":\"STATE\",\"request_id\":1,\"session_id\":\"S1\","
                + "\"payload\":{\"status\":\"IN_PROGRESS\",\"word\":\"_ _\",\"tried_letters\":[],"
                + "\"remaining_attempts\":6,\"revealed_word\":null}}");
        assertTrue(((StateMessage) m).getRevealedWord().isEmpty());
    }

    @Test
    void errorSansRequestIdAccepte() throws ProtocolException {
        Message m = codec.decode("{\"type\":\"ERROR\",\"code\":\"INVALID_MESSAGE\",\"message\":\"JSON invalide\"}");
        assertEquals(new ErrorMessage("JSON invalide", MessageError.INVALID_MESSAGE, 0), m);
    }

    @Test
    void errorAvecPayloadAccepte() throws ProtocolException {
        Message m = codec.decode("{\"type\":\"ERROR\",\"request_id\":3,"
                + "\"payload\":{\"code\":\"INVALID_STATE\",\"message\":\"Partie terminée\"}}");
        assertEquals(new ErrorMessage("Partie terminée", MessageError.INVALID_STATE, 3), m);
    }

    @Test
    void errorChampsInvalides() {
        assertRejected("{\"type\":\"ERROR\",\"request_id\":1,\"code\":\"BIDON\",\"message\":\"x\"}");
        assertRejected("{\"type\":\"ERROR\",\"request_id\":1,\"code\":\"INVALID_STATE\"}");
        assertRejected("{\"type\":\"ERROR\",\"request_id\":1,\"code\":1,\"message\":\"x\"}");
    }
}
