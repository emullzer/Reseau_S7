package Protocol.Message;

import Protocol.MessageType;

public interface Message {
    MessageType getType();
    int getRequestId();
}
