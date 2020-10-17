package serverClasses;

import javax.websocket.EncodeException;
import javax.websocket.Session;
import java.io.IOException;

public interface HelperPrimitive {
    //  checks if control should be given to this helper
    boolean isCalled(String str);
    //  initializes the session data
    void open(Session session) throws IOException, EncodeException;
    //  does task required on session removal for each helper
    void close(Session session) throws IOException, EncodeException;
    //  function which assumes control for analyzing the websocket client message
    void handleMessage(String message, Session session) throws IOException, EncodeException;
}
