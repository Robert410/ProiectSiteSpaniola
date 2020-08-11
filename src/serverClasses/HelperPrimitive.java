package serverClasses;

import javax.websocket.EncodeException;
import javax.websocket.Session;
import java.io.IOException;

public interface HelperPrimitive {
    boolean isCalled(String str);
    void open(Session session) throws IOException, EncodeException;
    void close(Session session) throws IOException, EncodeException;
    void handleMessage(String message, Session session) throws IOException, EncodeException;
}
