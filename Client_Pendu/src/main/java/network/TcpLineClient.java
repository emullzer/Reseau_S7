package network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class TcpLineClient {

    private final String host;
    private final int port;
    private final int connectTimeoutMs;
    private final int maxLineLength;
    private final LineListener listener;

    private final Object writeLock = new Object();
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final AtomicBoolean notified = new AtomicBoolean(false);

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    public TcpLineClient(String host, int port, int connectTimeoutMs,
                         int maxLineLength, LineListener listener) {
        this.host = host;
        this.port = port;
        this.connectTimeoutMs = connectTimeoutMs;
        this.maxLineLength = maxLineLength;
        this.listener = listener;
    }

    public void connect() throws IOException {
        socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), connectTimeoutMs);
            socket.setTcpNoDelay(true);
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            closeSocketQuietly();
            throw e;
        }
        Thread reader = new Thread(this::readLoop, "network-reader");
        reader.setDaemon(true);
        reader.start();
    }

    public void sendLine(String line) throws IOException {
        if (line == null || line.indexOf('\n') >= 0 || line.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("line must not be null or contain line breaks");
        }
        synchronized (writeLock) {
            if (closed.get()) {
                throw new IOException("Connection closed");
            }
            out.write(line);
            out.write('\n');
            out.flush();
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !closed.get();
    }

    public void close() {
        closed.set(true);
        closeSocketQuietly();
    }

    private void readLoop() {
        Throwable cause = null;
        try {
            String line;
            while ((line = readBoundedLine()) != null) {
                listener.onLine(line);
            }
            if (!closed.get()) {
                cause = new EOFException("Connection closed by server");
            }
        } catch (IOException e) {
            if (!closed.get()) {
                cause = e;
            }
        } catch (RuntimeException e) {
            cause = e;
        } finally {
            closed.set(true);
            closeSocketQuietly();
            if (notified.compareAndSet(false, true)) {
                listener.onDisconnected(cause);
            }
        }
    }

    private String readBoundedLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            if (c == '\n') {
                int last = sb.length() - 1;
                if (last >= 0 && sb.charAt(last) == '\r') {
                    sb.setLength(last);
                }
                return sb.toString();
            }
            if (sb.length() >= maxLineLength) {
                throw new IOException("Line too long (max " + maxLineLength + " chars)");
            }
            sb.append((char) c);
        }
        return null;
    }

    private void closeSocketQuietly() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }
}
