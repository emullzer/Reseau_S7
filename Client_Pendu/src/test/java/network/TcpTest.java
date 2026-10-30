package network;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TcpTest {

    private ServerSocket server;
    private TcpLineClient client;
    private final BlockingQueue<String> events = new LinkedBlockingQueue<>();

    private final LineListener listener = new LineListener() {
        @Override
        public void onLine(String line) {
            events.add("LINE:" + line);
        }

        @Override
        public void onDisconnected(Throwable cause) {
            events.add(cause == null ? "DISCONNECTED:clean" : "DISCONNECTED:error");
        }
    };

    @BeforeEach
    void setUp() throws IOException {
        server = new ServerSocket(0);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (client != null) {
            client.close();
        }
        server.close();
    }

    private Socket connectClient(int maxLineLength) throws IOException {
        client = new TcpLineClient("localhost", server.getLocalPort(), 2000, maxLineLength, listener);
        client.connect();
        return server.accept();
    }

    private String next() throws InterruptedException {
        String event = events.poll(2, TimeUnit.SECONDS);
        return event == null ? "TIMEOUT" : event;
    }

    @Test
    void sendLineArrivesTerminatedByNewline() throws Exception {
        Socket s = connectClient(1000);
        client.sendLine("hello");
        BufferedReader r = new BufferedReader(
                new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
        assertEquals("hello", r.readLine());
    }

    @Test
    void twoLinesInOneWriteGiveTwoEvents() throws Exception {
        Socket s = connectClient(1000);
        s.getOutputStream().write("a\nb\n".getBytes(StandardCharsets.UTF_8));
        s.getOutputStream().flush();
        assertEquals("LINE:a", next());
        assertEquals("LINE:b", next());
    }

    @Test
    void lineSplitInsideAnAccentedCharacterIsRebuilt() throws Exception {
        Socket s = connectClient(1000);
        byte[] bytes = "caf\u00e9\n".getBytes(StandardCharsets.UTF_8);
        OutputStream o = s.getOutputStream();
        o.write(bytes, 0, 4);
        o.flush();
        Thread.sleep(100);
        o.write(bytes, 4, bytes.length - 4);
        o.flush();
        assertEquals("LINE:caf\u00e9", next());
    }

    @Test
    void crlfIsAccepted() throws Exception {
        Socket s = connectClient(1000);
        s.getOutputStream().write("x\r\n".getBytes(StandardCharsets.UTF_8));
        s.getOutputStream().flush();
        assertEquals("LINE:x", next());
    }

    @Test
    void tooLongLineCausesDisconnectionWithError() throws Exception {
        Socket s = connectClient(10);
        s.getOutputStream().write("0123456789ABCDEF\n".getBytes(StandardCharsets.UTF_8));
        s.getOutputStream().flush();
        assertEquals("DISCONNECTED:error", next());
    }

    @Test
    void serverCloseCausesDisconnectionWithError() throws Exception {
        Socket s = connectClient(1000);
        s.close();
        assertEquals("DISCONNECTED:error", next());
    }

    @Test
    void partialLineThenCloseDiscardsFragment() throws Exception {
        Socket s = connectClient(1000);
        s.getOutputStream().write("abc".getBytes(StandardCharsets.UTF_8));
        s.getOutputStream().flush();
        s.close();
        assertEquals("DISCONNECTED:error", next());
    }

    @Test
    void clientCloseIsClean() throws Exception {
        connectClient(1000);
        client.close();
        assertEquals("DISCONNECTED:clean", next());
    }

    @Test
    void sendAfterCloseThrows() throws Exception {
        connectClient(1000);
        client.close();
        assertThrows(IOException.class, () -> client.sendLine("x"));
    }

    @Test
    void sendLineWithNewlineIsRejected() throws Exception {
        connectClient(1000);
        assertThrows(IllegalArgumentException.class, () -> client.sendLine("a\nb"));
    }

    @Test
    void connectionRefusedThrows() throws Exception {
        int port = server.getLocalPort();
        server.close();
        TcpLineClient c = new TcpLineClient("localhost", port, 1000, 1000, listener);
        assertThrows(IOException.class, c::connect);
    }
}
