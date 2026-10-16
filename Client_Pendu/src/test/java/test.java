import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import protocol.enumMessage.MessageError;
import protocol.enumMessage.MessageState;
import protocol.enumMessage.MessageType;
import protocol.message.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessagesTest {

    @Nested
    class Hello {
        @Test
        void typeEtRequestId() {
            HelloMessage m = new HelloMessage(1, "Emeric");
            assertEquals(MessageType.HELLO, m.getType());
            assertEquals(1, m.getRequestId());
            assertEquals("Emeric", m.getPlayerName());
        }

        @Test
        void requestIdInvalide() {
            assertThrows(IllegalArgumentException.class, () -> new HelloMessage(0, "Emeric"));
            assertThrows(IllegalArgumentException.class, () -> new HelloMessage(-1, "Emeric"));
        }

        @Test
        void playerNameInvalide() {
            assertThrows(IllegalArgumentException.class, () -> new HelloMessage(1, null));
            assertThrows(IllegalArgumentException.class, () -> new HelloMessage(1, ""));
        }

        @Test
        void equalsEtHashCode() {
            HelloMessage a = new HelloMessage(1, "Emeric");
            HelloMessage b = new HelloMessage(1, "Emeric");
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
            assertNotEquals(a, new HelloMessage(2, "Emeric"));
            assertNotEquals(a, new HelloMessage(1, "Autre"));
        }
    }

    @Nested
    class NewGame {
        @Test
        void typeEtRequestId() {
            NewGameMessage m = new NewGameMessage(3, "Emeric");
            assertEquals(MessageType.NEW_GAME, m.getType());
            assertEquals(3, m.getRequestId());
            assertEquals("Emeric", m.getPlayerName());
        }

        @Test
        void valeursInvalides() {
            assertThrows(IllegalArgumentException.class, () -> new NewGameMessage(0, "Emeric"));
            assertThrows(IllegalArgumentException.class, () -> new NewGameMessage(1, null));
            assertThrows(IllegalArgumentException.class, () -> new NewGameMessage(1, ""));
        }

        @Test
        void equalsEtHashCode() {
            NewGameMessage a = new NewGameMessage(1, "Emeric");
            NewGameMessage b = new NewGameMessage(1, "Emeric");
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
            assertNotEquals(a, new NewGameMessage(1, "Autre"));
        }
    }

    @Nested
    class Letter {
        @Test
        void typeEtRequestId() {
            LetterMessage m = new LetterMessage(4, 42, 'a');
            assertEquals(MessageType.LETTER, m.getType());
            assertEquals(4, m.getRequestId());
            assertEquals(42, m.getSessionId());
            assertEquals('a', m.getLetter());
        }

        @Test
        void identifiantsInvalides() {
            assertThrows(IllegalArgumentException.class, () -> new LetterMessage(0, 42, 'a'));
            assertThrows(IllegalArgumentException.class, () -> new LetterMessage(1, 0, 'a'));
        }

        @Test
        void caracteresInvalides() {
            assertThrows(IllegalArgumentException.class, () -> new LetterMessage(1, 1, '1'));
            assertThrows(IllegalArgumentException.class, () -> new LetterMessage(1, 1, ' '));
            assertThrows(IllegalArgumentException.class, () -> new LetterMessage(1, 1, '#'));
        }

        @Test
        void lettreAccentueeAcceptee() {
            assertDoesNotThrow(() -> new LetterMessage(1, 1, 'é'));
        }

        @Test
        void equalsEtHashCode() {
            LetterMessage a = new LetterMessage(1, 1, 'a');
            LetterMessage b = new LetterMessage(1, 1, 'a');
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
            assertNotEquals(a, new LetterMessage(1, 1, 'b'));
            assertNotEquals(a, new LetterMessage(1, 2, 'a'));
        }
    }

    @Nested
    class Quit {
        @Test
        void typeEtRequestId() {
            QuitMessage m = new QuitMessage(5, 42);
            assertEquals(MessageType.QUIT, m.getType());
            assertEquals(5, m.getRequestId());
        }

        @Test
        void valeursInvalides() {
            assertThrows(IllegalArgumentException.class, () -> new QuitMessage(0, 42));
            assertThrows(IllegalArgumentException.class, () -> new QuitMessage(1, 0));
        }

        @Test
        void equalsEtHashCode() {
            QuitMessage a = new QuitMessage(1, 42);
            QuitMessage b = new QuitMessage(1, 42);
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
            assertNotEquals(a, new QuitMessage(2, 42));
        }
    }

    @Nested
    class Resume {
        @Test
        void typeEtRequestId() {
            ResumeMessage m = new ResumeMessage(6, 42, "Emeric");
            assertEquals(MessageType.RESUME, m.getType());
            assertEquals(6, m.getRequestId());
            assertEquals(42, m.getSessionId());
            assertEquals("Emeric", m.getPlayerName());
        }

        @Test
        void valeursInvalides() {
            assertThrows(IllegalArgumentException.class, () -> new ResumeMessage(0, 42, "Emeric"));
            assertThrows(IllegalArgumentException.class, () -> new ResumeMessage(1, 0, "Emeric"));
            assertThrows(IllegalArgumentException.class, () -> new ResumeMessage(1, 42, null));
            assertThrows(IllegalArgumentException.class, () -> new ResumeMessage(1, 42, ""));
        }

        @Test
        void equalsEtHashCode() {
            ResumeMessage a = new ResumeMessage(1, 42, "Emeric");
            ResumeMessage b = new ResumeMessage(1, 42, "Emeric");
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
            assertNotEquals(a, new ResumeMessage(1, 42, "Autre"));
        }
    }

    @Nested
    class Error {
        @Test
        void typeEtRequestId() {
            ErrorMessage m = new ErrorMessage("INVALID_MESSAGE", MessageError.INVALID_MESSAGE, 7);
            assertEquals(MessageType.ERROR, m.getType());
            assertEquals(7, m.getRequestId());
            assertEquals(MessageError.INVALID_MESSAGE, m.getCode());
            assertEquals("INVALID_MESSAGE", m.getMessage());
        }

        @Test
        void messageInvalide() {
            assertThrows(IllegalArgumentException.class, () -> new ErrorMessage(null, MessageError.INVALID_MESSAGE, 7));
        }

        @Test
        void equalsEtHashCode() {
            ErrorMessage a = new ErrorMessage("X", MessageError.INVALID_MESSAGE, 7);
            ErrorMessage b = new ErrorMessage("X", MessageError.INVALID_MESSAGE, 7);
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
            assertNotEquals(a, new ErrorMessage("Y", MessageError.INVALID_MESSAGE, 7));
        }
    }

    @Nested
    class State {
        private List<Character> lettres() {
            return new ArrayList<>(List.of('a', 'b'));
        }

        @Test
        void typeEtGetters() {
            StateMessage m = new StateMessage(8, 42, MessageState.IN_PROGRESS, "_ a _",
                    lettres(), 5);
            assertEquals(MessageType.STATE, m.getType());
            assertEquals(8, m.getRequestId());
            assertEquals(42, m.getSessionId());
            assertEquals(MessageState.IN_PROGRESS, m.getState());
            assertEquals("_ a _", m.getWord());
            assertEquals(List.of('a', 'b'), m.getTriedLetters());
            assertEquals(5, m.getRemainingAttempts());
        }

        @Test
        void listeVideAcceptee() {
            assertDoesNotThrow(() -> new StateMessage(1, 1, MessageState.IN_PROGRESS, "_ _",
                    new ArrayList<>(), 6));
        }

        @Test
        void valeursInvalides() {
            assertThrows(IllegalArgumentException.class,
                    () -> new StateMessage(0, 1, MessageState.IN_PROGRESS, "_ _", new ArrayList<>(), 6));
            assertThrows(IllegalArgumentException.class,
                    () -> new StateMessage(1, 0, MessageState.IN_PROGRESS, "_ _", new ArrayList<>(), 6));
            assertThrows(IllegalArgumentException.class,
                    () -> new StateMessage(1, 1, null, "_ _", new ArrayList<>(), 6));
            assertThrows(IllegalArgumentException.class,
                    () -> new StateMessage(1, 1, MessageState.IN_PROGRESS, null, new ArrayList<>(), 6));
            assertThrows(IllegalArgumentException.class,
                    () -> new StateMessage(1, 1, MessageState.IN_PROGRESS, "", new ArrayList<>(), 6));
            assertThrows(IllegalArgumentException.class,
                    () -> new StateMessage(1, 1, MessageState.IN_PROGRESS, "_ _", null, 6));
            assertThrows(IllegalArgumentException.class,
                    () -> new StateMessage(1, 1, MessageState.IN_PROGRESS, "_ _", new ArrayList<>(), -1));
        }

        @Test
        void copieDefensiveALEntree() {
            List<Character> source = lettres();
            StateMessage m = new StateMessage(1, 1, MessageState.IN_PROGRESS, "_ _", source, 5);
            source.add('z');
            assertEquals(2, m.getTriedLetters().size());
        }

        @Test
        void listeNonModifiableALaSortie() {
            StateMessage m = new StateMessage(1, 1, MessageState.IN_PROGRESS, "_ _", lettres(), 5);
            assertThrows(UnsupportedOperationException.class,
                    () -> m.getTriedLetters().add('z'));
        }

        @Test
        void revealedWordAbsent() {
            StateMessage m = new StateMessage(1, 1, MessageState.IN_PROGRESS, "_ _", new ArrayList<>(), 6);
            assertTrue(m.getRevealedWord().isEmpty());
        }

        @Test
        void revealedWordPresent() {
            StateMessage m = new StateMessage(1, 1, MessageState.LOSE, "_ _", new ArrayList<>(), 0, "chat");
            assertEquals("chat", m.getRevealedWord().orElseThrow());
        }

        @Test
        void revealedWordBlancInvalide() {
            assertThrows(IllegalArgumentException.class,
                    () -> new StateMessage(1, 1, MessageState.LOSE, "_ _", new ArrayList<>(), 0, " "));
        }

        @Test
        void equalsEtHashCode() {
            StateMessage a = new StateMessage(1, 1, MessageState.IN_PROGRESS, "_ _", lettres(), 5);
            StateMessage b = new StateMessage(1, 1, MessageState.IN_PROGRESS, "_ _", lettres(), 5);
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
            assertNotEquals(a, new StateMessage(1, 1, MessageState.WIN, "_ _", lettres(), 5));
            assertNotEquals(a, new StateMessage(1, 1, MessageState.IN_PROGRESS, "_ _", lettres(), 4));
            assertNotEquals(a, new StateMessage(1, 1, MessageState.IN_PROGRESS, "a _", lettres(), 5));
        }

        @Test
        void equalsDistingueRevealedWord() {
            StateMessage a = new StateMessage(1, 1, MessageState.LOSE, "_ _", new ArrayList<>(), 0, "chat");
            StateMessage b = new StateMessage(1, 1, MessageState.LOSE, "_ _", new ArrayList<>(), 0, "chien");
            assertNotEquals(a, b);
        }
    }
}
